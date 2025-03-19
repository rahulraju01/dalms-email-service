package com.dss.config;

import com.dss.emailservice.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartupRunner implements CommandLineRunner {
    private final EmailService emailService;

    public StartupRunner(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("-- Triggering Email --");
        emailService.generateBirthdayEmailContent();
        emailService.generateWorkAnniversaryEmailContent();
    }
}
