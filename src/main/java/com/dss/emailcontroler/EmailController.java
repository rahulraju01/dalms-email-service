package com.dss.emailcontroler;

import com.dss.emailservice.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class EmailController {
    @Autowired
    private EmailService emailService;

    // Trigger email sending
    @GetMapping("/triggerEmail")
    public String triggerEmail() {
        emailService.generateBirthdayEmailContent();
        emailService.generateWorkAnniversaryEmailContent();
        return "Birthday and Anniversary emails sent!";
    }
}
