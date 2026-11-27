[CmdletBinding()]
param(
    [string]$DbUrl,
    [string]$DbUser,
    [SecureString]$DbPassword,
    [string]$JwtIssuer,
    [string]$JwtTtl,
    [ValidateSet('HS256', 'HS384', 'HS512')]
    [string]$JwtAlgorithm,
    [switch]$VkOnly,
    [switch]$Persist
)

$ErrorActionPreference = 'Stop'

$settings = if ($VkOnly) {
    [ordered]@{
        VK_CLIENT_ID     = '54927655'
        VK_REDIRECT_URI  = 'http://localhost'
        VK_SERVICE_TOKEN = $null
    }
} else {
    [ordered]@{
        DB_URL             = 'jdbc:postgresql://localhost:5432/matchflow'
        DB_USER            = 'postgres'
        DB_PASSWORD        = $null
        AUTH_JWT_SECRET    = $null
        AUTH_JWT_ISSUER    = 'matchflow'
        AUTH_JWT_TTL       = '1h'
        AUTH_JWT_ALGORITHM = 'HS256'
    }
}

foreach ($name in @($settings.Keys)) {
    $value = [Environment]::GetEnvironmentVariable($name, 'Process')
    if ($Persist -and ($null -eq $value -or ($VkOnly -and [string]::IsNullOrWhiteSpace($value)))) {
        $value = [Environment]::GetEnvironmentVariable($name, 'User')
    }
    if ($VkOnly -and [string]::IsNullOrWhiteSpace($value)) {
        continue
    }
    if ($null -ne $value) {
        $settings[$name] = $value
    }
}

if ($VkOnly) {
    if ([string]::IsNullOrWhiteSpace($settings.VK_SERVICE_TOKEN)) {
        $token = Read-Host 'VK ID service access token' -AsSecureString
        try {
            $settings.VK_SERVICE_TOKEN = [Net.NetworkCredential]::new('', $token).Password
        } finally {
            $token.Dispose()
        }
        if ([string]::IsNullOrWhiteSpace($settings.VK_SERVICE_TOKEN)) {
            throw 'VK_SERVICE_TOKEN is required for VK ID.'
        }
    }
} else {
    if ($PSBoundParameters.ContainsKey('DbUrl')) { $settings.DB_URL = $DbUrl }
    if ($PSBoundParameters.ContainsKey('DbUser')) { $settings.DB_USER = $DbUser }
    if ($PSBoundParameters.ContainsKey('JwtIssuer')) { $settings.AUTH_JWT_ISSUER = $JwtIssuer }
    if ($PSBoundParameters.ContainsKey('JwtTtl')) { $settings.AUTH_JWT_TTL = $JwtTtl }
    if ($PSBoundParameters.ContainsKey('JwtAlgorithm')) { $settings.AUTH_JWT_ALGORITHM = $JwtAlgorithm }

    $keySizes = @{ HS256 = 32; HS384 = 48; HS512 = 64 }
    $algorithm = $settings.AUTH_JWT_ALGORITHM
    if (!$keySizes.ContainsKey($algorithm)) {
        throw 'AUTH_JWT_ALGORITHM must be HS256, HS384 or HS512.'
    }

    if ([string]::IsNullOrWhiteSpace($settings.AUTH_JWT_SECRET)) {
        $bytes = [byte[]]::new(64)
        $random = [Security.Cryptography.RandomNumberGenerator]::Create()
        try {
            $random.GetBytes($bytes)
            $settings.AUTH_JWT_SECRET = [Convert]::ToBase64String($bytes)
        } finally {
            $random.Dispose()
        }
    } else {
        try {
            $bytes = [Convert]::FromBase64String($settings.AUTH_JWT_SECRET)
        } catch [FormatException] {
            throw 'AUTH_JWT_SECRET must contain a Base64-encoded key.'
        }
        if ($bytes.Length -lt $keySizes[$algorithm]) {
            throw "AUTH_JWT_SECRET is too short for $algorithm."
        }
    }

    if ($null -eq $DbPassword -and $null -eq $settings.DB_PASSWORD) {
        $DbPassword = Read-Host "PostgreSQL password for $($settings.DB_USER)" -AsSecureString
    }
    if ($null -ne $DbPassword) {
        $settings.DB_PASSWORD = [Net.NetworkCredential]::new('', $DbPassword).Password
    }
}

foreach ($name in $settings.Keys) {
    if ($Persist) {
        [Environment]::SetEnvironmentVariable($name, $settings[$name], 'User')
    }
    [Environment]::SetEnvironmentVariable($name, $settings[$name], 'Process')
}

if ($Persist) {
    Write-Host 'MatchFlow variables saved for the Windows user. Restart IntelliJ IDEA to pick them up.'
} else {
    Write-Host 'MatchFlow variables set for the current PowerShell session.'
}
