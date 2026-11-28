package ru.matchflow.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;
import ru.matchflow.config.EmailVerificationProperties;
import ru.matchflow.events.EmailVerificationRequested;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    private final EmailVerificationProperties properties;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendVerification(EmailVerificationRequested event) {
        String link = UriComponentsBuilder.fromUri(properties.verificationUrl())
                .queryParam("token", event.token())
                .build(true)
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(event.email());
        message.setSubject("Confirm your MatchFlow email");
        message.setText("Confirm your email address by opening this link:\n" + link);

        try {
            mailSender.send(message);
        } catch (MailException failure) {
            log.warn("Could not send the email verification message ({})", failure.getClass().getSimpleName());
        }
    }
}
