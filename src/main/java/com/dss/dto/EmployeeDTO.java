package com.dss.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
    private String name;
    private String email;
    private LocalDate dateOfJoining;
    private LocalDate dateOfBirth;
    private String departmentName;
    private String gender;
}
