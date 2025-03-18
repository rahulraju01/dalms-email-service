package com.dss.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Employee {
    private String name;
    @Id
    @Column(name = "empcode")
    private Long empCode;
    private String department;
    private LocalDate doj;
    private String gender;
    private String designation;
    private String mobileNo;
    @Column(name = "email_id")
    private String emailId;
    private LocalDate doc;
    private LocalDate doI;
    private String deptCode;
    private Integer reportingTo;
    private String employeeGroup;
    private String isProcessed;
    private String createdBy;
    private LocalDate createdDate;
    private String updatedBy;
    private LocalDate updatedDate;
    private Integer isDeleted;
    @Column(name = "whiz_empcode")
    private String whizEmpCode;
    private LocalDate dob;
    @Column(name = "employeegroup1")
    private String employeeGroup1;
    @Column(name = "is_early_allowed")
    private String isEarlyAllowed;
    @Column(name = "age_as_on_doj")
    private Integer ageAsOnDoj;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Column(name = "start_date")
    private LocalDate startDate;
}
