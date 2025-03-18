package com.dss.emailservice;

import com.dss.dto.EmployeeDTO;
import com.dss.repository.EmployeeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ImageLoader imageLoader;

    // Method to send birthday emails with background image and logo
    public void generateBirthdayEmailContent() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();
        List<Object[]> result = employeeRepository.findByBirthday(month, day);
        List<EmployeeDTO> employees = new ArrayList<>();
        for (Object[] row : result) {
            String employeeName = (String) row[0];
            String email = (String) row[1];
            LocalDate doj = ((java.sql.Date) row[2]).toLocalDate();
            LocalDate dob = ((java.sql.Date) row[3]).toLocalDate();
            String departmentName = (String) row[4];
            String gender = (String) row[5];

            EmployeeDTO employeeDTO = new EmployeeDTO(employeeName, email, doj, dob, departmentName, gender);
            employees.add(employeeDTO);
        }

        log.info("== Birthday Employees found with size: {}", employees.size());

        for (EmployeeDTO employee : employees) {
            try {
                sendBirthdayEmail(employee, "Happy Birthday Wishes!", buildBirthdayMessage(employee));
                log.info("-- Email send successfully for Employee: {}", employee.getName());
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to send anniversary emails with HTML format
    public void generateWorkAnniversaryEmailContent() {
        LocalDate currentDate = LocalDate.now();
        LocalDate oneYearAgo = currentDate.minusYears(1);
        int month = currentDate.getMonthValue();
        int day = currentDate.getDayOfMonth();
        List<Object[]> result = employeeRepository.findEmployeesWithAtLeastOneYearOfService(oneYearAgo, month, day);
        List<EmployeeDTO> employees = new ArrayList<>();
        for (Object[] row : result) {
            String employeeName = Objects.nonNull(row[0]) ? (String) row[0] : null;
            String email = (String) row[1];
            LocalDate doj = Objects.nonNull(row[2]) ? ((java.sql.Date) row[2]).toLocalDate() : null;
            LocalDate dob = Objects.nonNull(row[3]) ? ((java.sql.Date) row[3]).toLocalDate() : null;
            String departmentName = Objects.nonNull(row[4]) ? (String) row[4] : null;
            String gender = Objects.nonNull(row[5]) ? (String) row[5] : null;

            EmployeeDTO employeeDTO = new EmployeeDTO(employeeName, email, doj, dob, departmentName, gender);
            employees.add(employeeDTO);
        }
        log.info("== Anniversary Employees found with size: {}", employees.size());

        for (EmployeeDTO employee : employees) {
            try {
                sendWorkAnniversaryEmail(employee, "Happy Work Anniversary!", buildAnniversaryMessage(employee));
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String buildBirthdayMessage(EmployeeDTO employee) {
        String genderPronoun = StringUtils.equalsIgnoreCase(employee.getGender(), "M") ? "he" : "she";
        String genderTeam = StringUtils.equalsIgnoreCase(employee.getGender(), "M") ? "his" : "her";
        return "<html>" +
                "<head>" +
                "<style>" +
                "body {" +
                "   margin: 0;" +
                "   padding: 0;" +
                "   width: 100%;" +
                "   height: 100%;" +
                "   font-family: Arial, sans-serif;" +
                "   color: black;" +
                "   text-align: center;" +
                "   background-color: #f0f0f0;" +
                "}" +
                " .content {" +
                "   padding: 30px;" +
                "   background: rgba(255, 255, 255, 0.9);" +
                "   border-radius: 10px;" +
                "   font-size: 20px;" +
                "   font-weight: normal;" +
                "   line-height: 1.5;" +
                "}" +
                " .image {" +
                "   max-width: 100%;" +
                "   height: auto;" +
                "   margin-top: 30px;" +
                "}" +
                " .logo {" +
                "   position: absolute;" +
                "   bottom: 20px;" +
                "   right: 20px;" +
                "   width: 120px;" +
                "   height: auto;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='content'>" +
                "<h2>Please join me in wishing <strong>" + employee.getName() + "</strong> from <strong>" + employee.getDepartmentName() + "</strong> Team as " + genderPronoun + " celebrates " + genderTeam + " Birthday today on " +
                employee.getDateOfBirth().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + employee.getDateOfBirth().getDayOfMonth() + "th.</h2>" +
                "<p>Wishing you <strong>" + employee.getName() + "</strong>,<br/>" +
                "Many Many Happy Returns of the Day!!!</p>" +
                "<img src='cid:birthdayImage' class='image' alt='Birthday Image'/>" +
                "<p>Regards,<br>HRD</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }


    private String buildAnniversaryMessage(EmployeeDTO employee) {
        // Get the total years the employee has worked
        int yearsWorked = calculateWorkAnniversary(employee);  // Assuming you have a method to get years worked

        // Determine the appropriate suffix for the anniversary year (e.g., 1st, 2nd, 3rd, 4th, etc.)
        String suffix = getAnniversarySuffix(yearsWorked);

        // Build the HTML message
        return "<html>" +
                "<head>" +
                "<style>" +
                "body {" +
                "   margin: 0;" +
                "   padding: 0;" +
                "   width: 100%;" +
                "   height: 100%;" +
                "   font-family: Arial, sans-serif;" +
                "   color: black;" +
                "   text-align: center;" +
                "   background-color: #f0f0f0;" +
                "}" +
                " .content {" +
                "   padding: 30px;" +
                "   background: rgba(255, 255, 255, 0.9);" +
                "   border-radius: 10px;" +
                "   font-size: 20px;" +
                "   font-weight: normal;" +
                "   line-height: 1.5;" +
                "}" +
                " .image {" +
                "   max-width: 100%;" +
                "   height: auto;" +
                "   margin-top: 30px;" +
                "}" +
                " .logo {" +
                "   position: absolute;" +
                "   bottom: 20px;" +
                "   right: 20px;" +
                "   width: 120px;" +
                "   height: auto;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='content'>" +
                "<h2>Congratulations to <strong>" + employee.getName() + "</strong> on their " + yearsWorked + suffix + " Work Anniversary at <strong>" + employee.getDepartmentName() + "</strong> Team!</h2>" +
                "<p>We thank you for your hard work, dedication, and commitment over the " + yearsWorked + " years.</p>" +
                "<p>Wishing you many more successful years ahead!</p>" +
                "<img src='cid:anniversaryImage' class='image' alt='Anniversary Image'/>" +
                "<p>Regards,<br/>HRD</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public int calculateWorkAnniversary(EmployeeDTO employee) {
        if (employee == null || employee.getDateOfJoining() == null) {
            throw new IllegalArgumentException("Employee or start date is null.");
        }

        // Get the current date
        LocalDate currentDate = LocalDate.now();
        // Get the start date from employee
        LocalDate startDate = employee.getDateOfJoining();

        // Calculate the period between the start date and today
        Period period = Period.between(startDate, currentDate);

        // Return the total years of experience (work anniversary)
        return period.getYears();
    }

    // Helper method to get the appropriate suffix for the anniversary (1st, 2nd, 3rd, etc.)
    private String getAnniversarySuffix(int year) {
        if (year % 10 == 1 && year != 11) {
            return "st";
        } else if (year % 10 == 2 && year != 12) {
            return "nd";
        } else if (year % 10 == 3 && year != 13) {
            return "rd";
        } else {
            return "th";
        }
    }


    // Send the email with HTML content, background image and logo
    private void sendBirthdayEmail(EmployeeDTO employee, String subject, String message) throws MessagingException, IOException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        String[] recipients = {employee.getEmail()};
        helper.setTo(recipients);
        helper.setSubject(subject);
        helper.setText(message, true); // true to send HTML email

        // Attach the birthday image as the background
//        ClassPathResource birthdayImage = new ClassPathResource("static/birthday_image.jpg"); // Put image in src/main/resources/static
        helper.addInline("birthdayImage", imageLoader.getRandomBirthdayTemplate());

        mailSender.send(mimeMessage);
        log.info("Birthday email sent to {}", employee.getEmail());
    }

    private void sendWorkAnniversaryEmail(EmployeeDTO employee, String subject, String message) throws MessagingException, IOException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setTo(employee.getEmail());
        helper.setSubject(subject);
        helper.setText(message, true); // true to send HTML email

        // Attach the birthday image as the background
//        ClassPathResource birthdayImage = new ClassPathResource("static/birthday_image.jpg"); // Put image in src/main/resources/static
        helper.addInline("anniversaryImage", imageLoader.getRandomWorkAnniversaryTemplate());

        mailSender.send(mimeMessage);
        log.info("Work Anniversary email sent to {}", employee.getEmail());
    }
}
