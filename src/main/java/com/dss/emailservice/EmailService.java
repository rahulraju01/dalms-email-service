package com.dss.emailservice;

import com.dss.dto.EmployeeDTO;
import com.dss.repository.EmployeeRepository;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.annotation.PostConstruct;
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
import java.io.StringWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailService {

    private Set<String> designationList = new HashSet<>();

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ImageLoader imageLoader;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private Configuration freemarkerConfig; // Freemarker configuration

    @Value("${EMAIL_CC}")
    private String cc;

    @Value("${spring.mail.username}")
    private String from;

    @PostConstruct
    public void init(){
        designationList = employeeRepository.fetchDesignationList();
    }

    private Set<String> getDesignationList(){
        if(designationList.isEmpty()){
            throw new IllegalStateException("Designation list found empty");
        }
        return designationList;
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> generateBirthdayEmailContent() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        List<EmployeeDTO> employees = getBirthdayEmployees(month, day);

        log.info("== Birthday Employees found with size: {}", employees.size());

        employees.stream()
                .map(employee -> CompletableFuture.runAsync(() ->
                        {
                            try {
                                sendBirthdayEmail(employee, getDesignationList().contains(employee.getDesignation()));
                            } catch (Exception ex) {
                                log.error("Unexpected error during async email sending", ex);
                            }
                        }
                        , taskExecutor))
                .forEach(CompletableFuture::join);

        return CompletableFuture.completedFuture(null);
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> generateWhizzibleContent() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        if (today == DayOfWeek.FRIDAY) {
            sendWhizzibleEmail();
        }
        return CompletableFuture.completedFuture(null);
    }

    // Method to send work anniversary emails with Freemarker template
    @Async("taskExecutor")
    public CompletableFuture<Void> generateWorkAnniversaryEmailContent() {
        LocalDate currentDate = LocalDate.now();
        LocalDate oneYearAgo = currentDate.minusYears(1);
        int month = currentDate.getMonthValue();
        int day = currentDate.getDayOfMonth();

        List<EmployeeDTO> employees = getAnniversaryEmployees(oneYearAgo, month, day);

        log.info("== Anniversary Employees found with size: {}", employees.size());

        employees.stream()
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

    // Helper method to send birthday email asynchronously
    private void sendBirthdayEmail(EmployeeDTO employee, boolean isManager) {
        try {
            String message = isManager ? generateFreemarkerManagerBirthdayTemplate(employee) : generateFreemarkerBirthdayTemplate(employee);
            String subject = String.format("Heartiest Birthday Wishes................%s!", employee.getName());
            Resource resource = isManager ? imageLoader.getRandomManagerBirthdayTemplate() : imageLoader.getRandomEmployeeBirthdayTemplate();
            sendEmail(employee.getEmail(), null, subject, message, "birthdayImage", resource);

            log.info("-- Email sent successfully for {}: {}", isManager ? "Manager" : "Employee", employee.getName());
        } catch (MessagingException | IOException | TemplateException e) {
            log.error("Error sending birthday email: {} for {}: {}", employee.getEmail(),isManager ? "manager": "employee", employee.getName(), e);
        }
    }

    // Helper method to send birthday email asynchronously
    private void sendWhizzibleEmail() {
        String employeeName = "r.raju@direction.biz";
        try {
            String message = generateFreemarkerWhizzibleTemplate();
            sendEmail(employeeName, null, "Whizible Entries for the week", message, "whizzibleLogo", imageLoader.getIconResourceByName("whizzible-logo"));
            log.info("-- Whizzible Reminder email sent to employee: {}", employeeName);
        } catch (Exception e) {
            log.error("Error sending birthday email: {} for Employee: {}", employeeName, employeeName, e);
        }
    }

    // Method to generate email content using Freemarker template
    private String generateFreemarkerWhizzibleTemplate() throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate("whizzible-template.ftl"); // Load the template file
        Map<String, Object> model = new HashMap<>();
        StringWriter stringWriter = new StringWriter();
        template.process(model, stringWriter);
        return stringWriter.toString();
    }

    // Method to generate email content using Freemarker template
    private String generateFreemarkerBirthdayTemplate(EmployeeDTO employee) throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate("birthday-employee.ftl"); // Load the template file

        // Prepare data for the template
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("employeeName", employee.getName());
        templateData.put("departmentName", employee.getDepartmentName());
        templateData.put("genderPronoun", employee.getGender().equals("M") ? "he" : "she");
        templateData.put("genderTeam", employee.getGender().equals("M") ? "his" : "her");
        templateData.put("dobMonth", employee.getDateOfBirth().getMonth().name());
        templateData.put("dobDay", employee.getDateOfBirth().getDayOfMonth());

        // Process the template and generate the HTML content
        StringWriter stringWriter = new StringWriter();
        template.process(templateData, stringWriter);

        return stringWriter.toString();
    }

    // Method to generate email content using Freemarker template
    private String generateFreemarkerManagerBirthdayTemplate(EmployeeDTO employee) throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate("birthday-manager.ftl"); // Load the template file

        // Prepare data for the template
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("employeeName", employee.getName());
        templateData.put("departmentName", employee.getDepartmentName());
        templateData.put("genderPronoun", employee.getGender().equals("M") ? "he" : "she");
        templateData.put("genderTeam", employee.getGender().equals("M") ? "his" : "her");
        templateData.put("dobMonth", employee.getDateOfBirth().getMonth().name());
        templateData.put("dobDay", employee.getDateOfBirth().getDayOfMonth());

        // Process the template and generate the HTML content
        StringWriter stringWriter = new StringWriter();
        template.process(templateData, stringWriter);

        return stringWriter.toString();
    }

    // Helper method to get the list of work anniversary employees
    private List<EmployeeDTO> getAnniversaryEmployees(LocalDate oneYearAgo, int month, int day) {
        return employeeRepository.findEmployeesWithAtLeastOneYearOfService(oneYearAgo, month, day).stream()
                .map(row -> mapToEmployeeDTO(row))
                .toList();
    }

    // Helper method to map raw data to EmployeeDTO
    private EmployeeDTO mapToEmployeeDTO(Object[] row) {
        // Extracting fields from the Object[] row and mapping them to EmployeeDTO
        String employeeName = Optional.ofNullable(row[0]).map(String.class::cast).orElse("");
        String email = Optional.ofNullable(row[1]).map(String.class::cast).orElse("");
        LocalDate dateOfJoining = Optional.ofNullable(row[2]).map(value -> ((java.sql.Date) value).toLocalDate()).orElse(null);
        LocalDate dateOfBirth = Optional.ofNullable(row[3]).map(value -> ((java.sql.Date) value).toLocalDate()).orElse(null);
        String departmentName = Optional.ofNullable(row[4]).map(String.class::cast).orElse("");
        String gender = Optional.ofNullable(row[5]).map(this::safeToString).orElse("");
        String designation = Optional.ofNullable(row[6]).map(String.class::cast).orElse("");
        String reportingManagerEmail = Optional.ofNullable(row.length > 7 ? row[7] : null).map(String.class::cast).orElse("");

        // Return a new EmployeeDTO with the mapped data
        return new EmployeeDTO(employeeName, email, dateOfJoining, dateOfBirth, departmentName, gender, designation, reportingManagerEmail);
    }

    public String safeToString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Character) {
            return value.toString();
        }
        return "";
    }

    // Helper method to send work anniversary email asynchronously
    private void sendWorkAnniversaryEmail(EmployeeDTO employee) {
        try {
            String message = generateAnniversaryFreemarkerTemplate(employee);
            sendEmail(employee.getEmail(), employee.getReportingManager(),"Happy Work Anniversary!", message, "anniversaryImage", imageLoader.getRandomWorkAnniversaryTemplate());
            log.info("-- Email sent successfully for Employee: {}", employee.getName());
        } catch (Exception e) {
            log.error("Error sending work anniversary email {} for Employee: {}", employee.getEmail(), employee.getName(), e);
        }
    }

    // Method to generate email content using Freemarker template for anniversary
    private String generateAnniversaryFreemarkerTemplate(EmployeeDTO employee) throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate("anniversary-email.ftl"); // Load the template file

        // Get the total years the employee has worked
        int yearsWorked = calculateWorkAnniversary(employee);  // Assuming you have a method to get years worked
        String suffix = getAnniversarySuffix(yearsWorked);

        // Prepare data for the template
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("employeeName", employee.getName());
        templateData.put("departmentName", employee.getDepartmentName());
        templateData.put("yearsWorked", yearsWorked);
        templateData.put("anniversarySuffix", suffix);

        // Process the template and generate the HTML content
        StringWriter stringWriter = new StringWriter();
        template.process(templateData, stringWriter);

        return stringWriter.toString();
    }

    // Method to calculate work anniversary years
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
    private void sendEmail(String to, String reportingManager, String subject, String message, String imageCid, Resource imageContent) throws MessagingException, IOException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(to);

        if(! StringUtils.isEmpty(cc)) {
            if(StringUtils.equalsIgnoreCase(subject, "Happy Work Anniversary!") && ! StringUtils.isEmpty(reportingManager)) {
                helper.setCc(reportingManager);
            } else {
                helper.setCc(cc);
            }
        }
        helper.setSubject(subject);
        helper.setText(message, true); // true to send HTML email
        helper.addInline(imageCid, imageContent);
        mailSender.send(mimeMessage);
        log.info("Email sent to {}", to);
    }
}
