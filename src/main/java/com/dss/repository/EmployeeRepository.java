package com.dss.repository;

import com.dss.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query(value = "SELECT e.name, e.email_id, e.doj, e.dob, d.deptname, e.gender, e.designation " +
            "FROM employee e JOIN department d ON e.deptcode = d.deptcode " +
            "WHERE EXTRACT(MONTH FROM e.dob) = :month " +
            "AND EXTRACT(DAY FROM e.dob) = :day and dol is null", nativeQuery = true)
    List<Object[]> findByBirthday(@Param("month") int month, @Param("day") int day);

    @Query(value = "SELECT e.name, e.email_id, e.doj, e.dob, d.deptname, e.gender, e.designation, " +
            "mgr.email_id AS reporting_manager FROM employee e " +
            "JOIN department d ON e.deptcode = d.deptcode " +
            "LEFT JOIN employee mgr ON mgr.empcode = e.reportingto " +
            "WHERE e.doj <= :oneYearAgo AND EXTRACT(MONTH FROM e.doj) = :month " +
            "AND EXTRACT(DAY FROM e.doj) = :day AND e.dol IS NULL", nativeQuery = true)
    List<Object[]> findEmployeesWithAtLeastOneYearOfService(@Param("oneYearAgo") LocalDate oneYearAgo, @Param("month") int month, @Param("day") int day);

    @Query(value = "select distinct emp.designation from employee emp where lower(emp.designation) like '%manager%';",
            nativeQuery = true)
    Set<String> fetchDesignationList();
}
