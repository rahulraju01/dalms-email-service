package com.dss.config;

import com.dss.emailservice.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class StartupRunner implements CommandLineRunner {
    private final EmailService emailService;

    public StartupRunner(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void run(String... args) {
        log.info("-- Triggering Email --");
        CompletableFuture<Void> birthdayEmails = emailService.generateBirthdayEmailContent();
        CompletableFuture<Void> workAnniversaryEmails = emailService.generateWorkAnniversaryEmailContent();
//        CompletableFuture<Void> whizzibleEmails = emailService.generateWhizzibleContent();

        CompletableFuture.allOf(birthdayEmails, workAnniversaryEmails)
                .join();

        log.info("-- All Emails Sent --");
        log.info("-- Shutting Down Application --");
        System.exit(0);
    }
}
