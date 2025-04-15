package com.dss.config;

import com.dss.emailservice.EmailService;
import com.dss.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class StartupRunner implements CommandLineRunner {
    private final EmailService emailService;
    private final EmployeeRepository employeeRepository;

    public StartupRunner(EmailService emailService, EmployeeRepository employeeRepository) {
        this.emailService = emailService;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void run(String... args) {
        log.info("-- Triggering Email --");
        CompletableFuture<Void> birthdayEmails = emailService.generateBirthdayEmailContent();
        CompletableFuture<Void> workAnniversaryEmails = emailService.generateWorkAnniversaryEmailContent();
        CompletableFuture<Void> whizzibleEmails = emailService.generateWhizzibleContent();

        CompletableFuture.allOf(birthdayEmails, workAnniversaryEmails, whizzibleEmails).join();
        log.info("-- All Emails Sent --");
        log.info("-- Shutting Down Application --");
        System.exit(0);
    }
}
