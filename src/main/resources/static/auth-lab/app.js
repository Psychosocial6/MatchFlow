const { createElement: h, Fragment, useCallback, useEffect, useRef, useState } = window.React || {};
const mailpitUrl = "http://localhost:8025/";
const vkSdkUrl = "https://cdn.jsdelivr.net/npm/@vkid/sdk@2.6.1/dist-sdk/umd/index.js";
let sdkLoad;

async function api(path, { body, token } = {}) {
    const headers = { Accept: "application/json, application/problem+json" };
    if (body) headers["Content-Type"] = "application/json";
    if (token) headers.Authorization = "Bearer " + token;
    const response = await fetch(path, {
        method: body ? "POST" : "GET",
        headers,
        body: body ? JSON.stringify(body) : undefined,
        cache: "no-store"
    });
    const text = await response.text();
    const result = text && response.headers.get("content-type")?.includes("json") ? JSON.parse(text) : null;
    if (!response.ok) {
        const error = new Error(result?.detail || result?.title || "Ошибка HTTP " + response.status);
        error.status = response.status;
        throw error;
    }
    return result;
}

function loadVkSdk() {
    if (!sdkLoad) {
        sdkLoad = new Promise((resolve, reject) => {
            const script = document.createElement("script");
            script.src = vkSdkUrl;
            script.onload = () => {
                if (window.VKIDSDK?.OneTap) {
                    resolve(window.VKIDSDK);
                } else {
                    script.remove();
                    reject(new Error("VK ID SDK загружен, но One Tap недоступен."));
                }
            };
            script.onerror = () => {
                script.remove();
                reject(new Error("Не удалось загрузить VK ID SDK. Проверь подключение к интернету."));
            };
            document.head.append(script);
        }).catch(error => {
            sdkLoad = undefined;
            throw error;
        });
    }
    return sdkLoad;
}

function base64Url(bytes) {
    return btoa(String.fromCharCode(...bytes)).replaceAll("+", "-").replaceAll("/", "_").replaceAll("=", "");
}

async function createPkce() {
    if (!window.crypto?.subtle) {
        throw new Error("Для VK ID открой страницу на localhost или по HTTPS.");
    }
    const state = base64Url(crypto.getRandomValues(new Uint8Array(32)));
    const codeVerifier = base64Url(crypto.getRandomValues(new Uint8Array(32)));
    const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(codeVerifier));
    return { state, codeVerifier, codeChallenge: base64Url(new Uint8Array(digest)) };
}

function Field({ label, ...input }) {
    return h("label", null, label, h("input", { required: true, ...input }));
}

function Note({ notice }) {
    if (!notice) return null;
    return h("p", {
        className: "status " + (notice.kind || ""),
        role: notice.kind === "error" ? "alert" : "status"
    }, notice.message);
}

function EmailPanel({ onLogin, onVerification, suggestedEmail }) {
    const [register, setRegister] = useState(true);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState(null);
    const [resending, setResending] = useState(false);
    const [resendNotice, setResendNotice] = useState(null);

    async function submit(event) {
        event.preventDefault();
        const fields = new FormData(event.currentTarget);
        const body = { username: fields.get("username").trim(), password: fields.get("password") };
        if (register) body.email = fields.get("email").trim();
        setBusy(true);
        setError(null);
        try {
            if (register) {
                const account = await api("/api/auth/register", { body });
                onVerification(account.email, "Проверь письмо в Mailpit. После подтверждения войди по имени и паролю.");
                setRegister(false);
            } else {
                const login = await api("/api/auth/login", { body });
                await onLogin(login, "Вход по имени и паролю выполнен.");
            }
        } catch (failure) {
            setError(register || failure.status !== 401 ? failure.message : "Проверь имя, пароль и подтверждение email.");
        } finally {
            setBusy(false);
        }
    }

    async function resend(event) {
        event.preventDefault();
        const email = new FormData(event.currentTarget).get("email").trim();
        setResending(true);
        try {
            await api("/api/auth/resend-verification", { body: { email } });
            setResendNotice({ message: "Запрос принят. Проверь Mailpit. Повторная отправка доступна через минуту." });
        } catch (failure) {
            setResendNotice({ kind: "error", message: failure.message });
        } finally {
            setResending(false);
        }
    }

    return h("section", { className: "panel", "aria-labelledby": "email-title" },
        h("h2", { id: "email-title" }, "Email и пароль"),
        h("div", { className: "tabs", "aria-label": "Режим входа по email" },
            h("button", {
                type: "button", disabled: busy, "aria-pressed": register,
                onClick: () => { setRegister(true); setError(null); }
            }, "Регистрация"),
            h("button", {
                type: "button", disabled: busy, "aria-pressed": !register,
                onClick: () => { setRegister(false); setError(null); }
            }, "Вход")),
        h("form", { className: "stack", key: String(register), onSubmit: submit },
            h(Field, { label: "Имя пользователя", name: "username", autoComplete: "username", maxLength: 255 }),
            register && h(Field, { label: "Email", name: "email", type: "email", autoComplete: "email", maxLength: 255 }),
            h(Field, {
                label: "Пароль", name: "password", type: "password", maxLength: 72,
                autoComplete: register ? "new-password" : "current-password"
            }),
            h("button", { className: "action full", disabled: busy }, register ? "Зарегистрироваться" : "Войти"),
            h(Note, { notice: error && { kind: "error", message: error } })),
        h("details", null,
            h("summary", null, "Отправить письмо повторно"),
            h("form", { className: "stack across", onSubmit: resend },
                h(Field, {
                    label: "Email", name: "email", type: "email", autoComplete: "email",
                    maxLength: 255, key: suggestedEmail, defaultValue: suggestedEmail
                }),
                h("button", { className: "action secondary", disabled: resending }, "Отправить"),
                h(Note, { notice: resendNotice }))));
}

function VkRegistrationForm({ ticket, onRegistered, onCancel }) {
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState(null);

    async function submit(event) {
        event.preventDefault();
        const fields = new FormData(event.currentTarget);
        setBusy(true);
        setError(null);
        try {
            const login = await api("/api/auth/vk/register", {
                body: {
                    registrationTicket: ticket,
                    username: fields.get("username").trim()
                }
            });
            await onRegistered(login);
        } catch (failure) {
            setError(failure.status === 409
                ? "Имя уже занято или VK ID привязан. Выбери другое имя или начни вход заново."
                : failure.message);
        } finally {
            setBusy(false);
        }
    }

    return h("form", { className: "stack", onSubmit: submit },
        h("p", { className: "muted" }, "VK ID подтверждён. Выбери имя пользователя для MatchFlow."),
        h(Field, { label: "Имя пользователя", name: "username", autoComplete: "username", maxLength: 255 }),
        h("div", { className: "actions" },
            h("button", { className: "action", disabled: busy }, "Создать аккаунт"),
            h("button", { type: "button", className: "action secondary", disabled: busy, onClick: onCancel }, "Отмена")),
        h(Note, { notice: error && { kind: "error", message: error } }));
}

function VkPanel({ session, onLogin, onLinked }) {
    const [config, setConfig] = useState(null);
    const [ticket, setTicket] = useState(null);
    const [retry, setRetry] = useState(0);
    const [note, setNote] = useState(null);
    const widgetHost = useRef(null);
    const token = session?.token;
    const linked = session?.account.vkLinked;

    useEffect(() => {
        let active = true;
        api("/api/auth/vk/config").then(value => {
            if (active) setConfig(value);
        }).catch(error => {
            if (active) setNote({ kind: "error", message: error.message });
        });
        return () => { active = false; };
    }, []);

    useEffect(() => {
        if (!config?.configured || linked || ticket) return;
        let active = true;
        let exchanging = false;
        let widget;
        const container = widgetHost.current;
        setNote({ message: "Загружаем One Tap…" });

        async function render() {
            if (new URL(config.redirectUri).origin !== window.location.origin) {
                throw new Error("Открой страницу на том же домене и порту, что указаны в VK_REDIRECT_URI.");
            }
            const pkce = await createPkce();
            const sdk = await loadVkSdk();
            if (!active) return;
            await api("/api/auth/vk/attempts", {
                token,
                body: { state: pkce.state, codeChallenge: pkce.codeChallenge, link: Boolean(token) }
            });
            if (!active) return;
            sdk.Config.init({
                app: config.clientId,
                redirectUrl: config.redirectUri,
                state: pkce.state,
                codeChallenge: pkce.codeChallenge,
                mode: sdk.ConfigAuthMode.InNewTab,
                responseMode: sdk.ConfigResponseMode.Callback
            });
            widget = new sdk.OneTap();
            widget.on(sdk.WidgetEvents.ERROR, error => {
                if (active) setNote({ kind: "error", message: error?.text || "Ошибка One Tap. Запусти вход заново." });
            });
            widget.on(sdk.OneTapInternalEvents.LOGIN_SUCCESS, async payload => {
                if (!active || exchanging) return;
                exchanging = true;
                setNote({ message: "Проверяем ответ VK…" });
                try {
                    if (payload?.state !== pkce.state) throw new Error("Попытка входа устарела. Начни заново.");
                    const result = await api("/api/auth/vk/login", {
                        token,
                        body: {
                            code: payload.code,
                            deviceId: payload.device_id,
                            state: payload.state,
                            codeVerifier: pkce.codeVerifier
                        }
                    });
                    if (!active) return;
                    container.replaceChildren();
                    setNote(null);
                    if (result.status === "SIGNED_IN") {
                        await onLogin(result.login, "Вход через VK ID выполнен.");
                    } else if (result.status === "LINKED") {
                        await onLinked(token);
                    } else if (result.status === "REGISTRATION_REQUIRED") {
                        setTicket(result.registrationTicket);
                    }
                } catch (error) {
                    if (active) setNote({ kind: "error", message: error.message });
                }
            });
            widget.render({
                container,
                fastAuthEnabled: window.location.protocol === "https:",
                styles: { height: 44, borderRadius: 8 }
            });
            setNote(null);
        }

        render().catch(error => {
            if (active) setNote({ kind: "error", message: error.message });
        });
        return () => {
            active = false;
            widget?.close();
            container?.replaceChildren();
        };
    }, [config, linked, ticket, token, retry, onLogin, onLinked]);

    function restart() {
        setTicket(null);
        setRetry(value => value + 1);
    }

    return h("section", { className: "panel", "aria-labelledby": "vk-title" },
        h("h2", { id: "vk-title" }, "VK ID"),
        config && !config.configured && h("div", { className: "status" },
            "Для One Tap задай ", h("code", null, "VK_CLIENT_ID"), ", ",
            h("code", null, "VK_SERVICE_TOKEN"), " и ", h("code", null, "VK_REDIRECT_URI"),
            " при запуске Spring."),
        config?.configured && linked && h("p", { className: "status success" },
            "VK ID привязан. Выйди из MatchFlow, чтобы проверить вход через One Tap."),
        config?.configured && !linked && !ticket && h(Fragment, null,
            h("p", { className: "muted" }, token
                ? "Нажми One Tap, чтобы привязать VK к текущему аккаунту."
                : "Вход или регистрация через One Tap."),
            h("div", { ref: widgetHost, className: "vk-widget" }),
            note?.kind === "error" && h("button", { className: "action secondary", onClick: restart }, "Начать заново")),
        ticket && h(VkRegistrationForm, {
            ticket,
            onCancel: restart,
            onRegistered: async login => {
                await onLogin(login, "Аккаунт VK создан. Вход выполнен.");
                setTicket(null);
            }
        }),
        h(Note, { notice: note }));
}

function AccountPanel({ session, onLogout, onNotice }) {
    const { account, token } = session;
    const fields = [
        ["Пользователь", account.username + " · #" + account.id],
        ["Email", account.email ?? "—"],
        ["Роль", account.role],
        ["VK ID", account.vkLinked ? "Привязан" : "Доступна привязка"]
    ];

    async function copyToken() {
        try {
            await navigator.clipboard.writeText(token);
            onNotice({ kind: "success", message: "JWT MatchFlow скопирован." });
        } catch {
            onNotice({ kind: "error", message: "Браузер не разрешил копирование." });
        }
    }

    return h("section", { className: "panel account across" },
        h("h2", null, "Текущий аккаунт"),
        h("dl", null, fields.map(([label, value]) => h("div", { key: label },
            h("dt", null, label), h("dd", null, value)))),
        h("div", { className: "actions" },
            h("button", { className: "action secondary", onClick: copyToken }, "Скопировать JWT"),
            h("button", { className: "action", onClick: onLogout }, "Выйти")));
}

function App() {
    const [session, setSession] = useState(null);
    const [notice, setNotice] = useState(null);
    const [email, setEmail] = useState("");

    const onLogin = useCallback(async (login, message) => {
        const token = login.accessToken;
        const account = await api("/api/auth/me", { token });
        setSession({ token, account });
        setNotice({ kind: "success", message });
    }, []);

    const onLinked = useCallback(async token => {
        const account = await api("/api/auth/me", { token });
        setSession(current => current?.token === token ? { token, account } : current);
        setNotice({ kind: "success", message: "VK ID привязан к аккаунту MatchFlow." });
    }, []);

    const onVerification = useCallback((address, message) => {
        setEmail(address);
        setNotice({ message });
    }, []);

    return h(Fragment, null,
        h("header", { className: "topbar" },
            h("span", { className: "brand" }, "MatchFlow"),
            h("a", { href: mailpitUrl, target: "_blank", rel: "noreferrer" }, "Открыть Mailpit ↗")),
        h("main", null,
            h("div", { className: "heading" },
                h("h1", null, "Проверка входа"),
                h("p", null, "Регистрация и вход через email или VK ID.")),
            notice && h("div", { className: "across" }, h(Note, { notice })),
            h("div", { className: "grid" },
                h(EmailPanel, { onLogin, onVerification, suggestedEmail: email }),
                h(VkPanel, { session, onLogin, onLinked })),
            session && h(AccountPanel, {
                session,
                onNotice: setNotice,
                onLogout: () => { setSession(null); setNotice(null); }
            })));
}

const root = document.getElementById("root");
if (window.React && window.ReactDOM) {
    window.ReactDOM.createRoot(root).render(h(App));
} else {
    root.textContent = "Не удалось загрузить React. Проверь подключение к интернету и обнови страницу.";
}
