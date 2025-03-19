package com.dss.emailservice;

import com.dss.dto.EmployeeDTO;
import com.dss.repository.EmployeeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ImageLoader imageLoader;

    @Autowired
    private TaskExecutor taskExecutor;

    @Value("${email-cc}")
    private String cc;

    // Method to send birthday emails with background image and logo
    @Async("taskExecutor")
    public CompletableFuture<Void> generateBirthdayEmailContent() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        List<EmployeeDTO> employees = getBirthdayEmployees(month, day);

        log.info("== Birthday Employees found with size: {}", employees.size());

        employees.stream()
                .filter(r -> r.getIsDeleted().toPlainString().equals("0"))
                .map(employee -> CompletableFuture.runAsync(() -> sendBirthdayEmail(employee), taskExecutor))
                .forEach(CompletableFuture::join);

        return CompletableFuture.completedFuture(null);
    }

    // Method to send anniversary emails with HTML format
    @Async("taskExecutor")
    public CompletableFuture<Void> generateWorkAnniversaryEmailContent() {
        LocalDate currentDate = LocalDate.now();
        LocalDate oneYearAgo = currentDate.minusYears(1);
        int month = currentDate.getMonthValue();
        int day = currentDate.getDayOfMonth();

        List<EmployeeDTO> employees = getAnniversaryEmployees(oneYearAgo, month, day);

        log.info("== Anniversary Employees found with size: {}", employees.size());

        employees.stream()
                .filter(r -> r.getIsDeleted().toPlainString().equals("0"))
                .map(employee -> CompletableFuture.runAsync(() -> sendWorkAnniversaryEmail(employee), taskExecutor))
                .forEach(CompletableFuture::join);

        return CompletableFuture.completedFuture(null);
    }

    // Helper method to get the list of birthday employees
    private List<EmployeeDTO> getBirthdayEmployees(int month, int day) {
        return employeeRepository.findByBirthday(month, day).stream()
                .map(row -> mapToEmployeeDTO(row))
                .toList();
    }

    // Helper method to get the list of work anniversary employees
    private List<EmployeeDTO> getAnniversaryEmployees(LocalDate oneYearAgo, int month, int day) {
        return employeeRepository.findEmployeesWithAtLeastOneYearOfService(oneYearAgo, month, day).stream()
                .map(row -> mapToEmployeeDTO(row))
                .toList();
    }

    // Helper method to map raw data to EmployeeDTO
    private EmployeeDTO mapToEmployeeDTO(Object[] row) {
        String employeeName = Optional.ofNullable(row[0]).map(String.class::cast).orElse("");
        String email = Optional.ofNullable(row[1]).map(String.class::cast).orElse("");
        LocalDate dateOfJoining = Optional.ofNullable(row[2]).map(value -> ((java.sql.Date) value).toLocalDate()).orElse(null);
        LocalDate dateOfBirth = Optional.ofNullable(row[3]).map(value -> ((java.sql.Date) value).toLocalDate()).orElse(null);
        String departmentName = Optional.ofNullable(row[4]).map(String.class::cast).orElse("");
        String gender = Optional.ofNullable(row[5]).map(String.class::cast).orElse("");
        BigDecimal isDeleted = Optional.ofNullable(row[6]).map(BigDecimal.class::cast).orElse(null);
        return new EmployeeDTO(employeeName, email, dateOfJoining, dateOfBirth, departmentName, gender, isDeleted);
    }

    // Method to build the birthday message HTML
    private String buildBirthdayMessage(EmployeeDTO employee) {
        String genderPronoun = StringUtils.equalsIgnoreCase(employee.getGender(), "M") ? "he" : "she";
        String genderTeam = StringUtils.equalsIgnoreCase(employee.getGender(), "M") ? "his" : "her";

        return "<html xmlns:v='urn:schemas-microsoft-com:vml' xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns:m='http://schemas.microsoft.com/office/2004/12/omml' xmlns='http://www.w3.org/TR/REC-html40'>" +
                "<head>" +
                "<meta http-equiv='Content-Type' content='text/html; charset=us-ascii'>" +
                "<meta name='Generator' content='Microsoft Word 15 (filtered medium)'>" +
                "<style>" +
                "body {" +
                "   background-color: #FBE4D5;" +
                "   font-family: 'Monotype Corsiva', sans-serif;" +
                "   font-size: 22pt;" +
                "   text-align: center;" +
                "   margin: 0;" +
                "   padding: 0;" +
                "}" +
                ".content {" +
                "   padding: 5px;" +
                "   border-radius: 10px;" +
                "   display: inline-block;" +
                "   width: 95%;" +
                "}" +
                ".image {" +
                "   max-width: 100%;" +
                "   height: auto;" +
                "   margin-top: 30px;" +
                "}" +
                "h2, p {" +
                "   margin: 10px 0;" +
                "   font-size: 22pt;" +
                "   font-family: 'Monotype Corsiva', serif;" +
                "}" +
                ".logo {" +
                "   position: absolute;" +
                "   bottom: 20px;" +
                "   right: 20px;" +
                "   width: 120px;" +
                "   height: auto;" +
                "}" +
                "#footer {" +
                "   display: flex;" +
                "   flex-direction: column;" +
                "   align-items: flex-start;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='content'>" +
                "<p style='color: #002060; display: flex;'><strong>Dear All,</strong></p>" +
                "<p style='color: #993300; margin-right: 100px;'><strong>Please join me in wishing <span style='color: #002060;'>" + employee.getName() + "</span> from <span style='color: #002060;'>" + employee.getDepartmentName() + "</span> Team as " + genderPronoun + " celebrates " + genderTeam + " Birthday today on " +
                "<span style='color: #002060;'>" + employee.getDateOfBirth().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + employee.getDateOfBirth().getDayOfMonth() + "th</span>.</strong></p>" +
                "<p style='color: #993300;display: flex; align-items: flex-start;' class='greeting'><strong>Wishing you </strong><span style='color: #002060;'><strong>" + employee.getName() + ",</strong></span></p>" +
                "<p style='color: #002060;' class='greeting' style='font-size: 26pt;'><strong><u>Happy Birthday!!</u></strong></p>" +
                "<img src='cid:birthdayImage' class='image' alt='Birthday Image'/>" +
                "<p id='footer' style='color: #993300;' class='greeting'><strong>Regards</strong>,<br><span style='color: #002060;'><strong>HRD</strong></span></p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }


    // Method to send the birthday email asynchronously
    private void sendBirthdayEmail(EmployeeDTO employee) {
        try {
            String message = buildBirthdayMessage(employee);
            sendEmail(employee.getEmail(), "Happy Birthday Wishes!", message, "birthdayImage", imageLoader.getRandomBirthdayTemplate());
            log.info("-- Email sent successfully for Employee: {}", employee.getName());
        } catch (MessagingException | IOException e) {
            log.error("Error sending birthday email: {} for Employee: {}", employee.getEmail(), employee.getName(), e);
        }
    }

    // Method to send work anniversary email asynchronously
    private void sendWorkAnniversaryEmail(EmployeeDTO employee) {
        try {
            String message = buildAnniversaryMessage(employee);
            sendEmail(employee.getEmail(), "Happy Work Anniversary!", message, "anniversaryImage", imageLoader.getRandomWorkAnniversaryTemplate());
            log.info("-- Email sent successfully for Employee: {}", employee.getName());
        } catch (MessagingException | IOException e) {
            log.error("Error sending work anniversary email {} for Employee: {}", employee.getEmail(),employee.getName(), e);
        }
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

    // Common method to send email with inline image
    private void sendEmail(String to, String subject, String message, String imageCid, Resource imageContent) throws MessagingException, IOException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(to);
        helper.setCc(cc);
        helper.setSubject(subject);
        helper.setText(message, true); // true to send HTML email
        helper.addInline(imageCid, imageContent);
        mailSender.send(mimeMessage);
        log.info("Email sent to {}", to);
    }
}
