package com.dss.repository;

import com.dss.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

//    @Query("SELECT e FROM Employee e WHERE EXTRACT(MONTH FROM e.dateOfBirth) = :month " +
//            "AND EXTRACT(DAY FROM e.dateOfBirth) = :day")
//    List<Employee> findByBirthday(@Param("month") int month, @Param("day") int day);

    //    @Query("SELECT e FROM Employee e WHERE EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM e.startDate) >= 1")
//    List<Employee> findEmployeesWithAnniversary();

    @Query(value = "SELECT e.name, e.email_id, e.doj, e.dob, d.deptname, e.gender " +
            "FROM employee e JOIN department d ON e.deptcode = d.deptcode " +
            "WHERE EXTRACT(MONTH FROM e.dob) = :month " +
            "AND EXTRACT(DAY FROM e.dob) = :day and dol is null", nativeQuery = true)
    List<Object[]> findByBirthday(@Param("month") int month, @Param("day") int day);

    @Query(value = "SELECT e.name, e.email_id, e.doj, e.dob, d.deptname, e.gender " +
            "FROM employee e JOIN department d ON e.deptcode = d.deptcode " +
            "WHERE e.doj <= :oneYearAgo and EXTRACT(MONTH FROM e.doj) = :month and EXTRACT(DAY FROM e.doj) = :day and dol is null", nativeQuery = true)
    List<Object[]> findEmployeesWithAtLeastOneYearOfService(@Param("oneYearAgo") LocalDate oneYearAgo, @Param("month") int month, @Param("day") int day);

}
