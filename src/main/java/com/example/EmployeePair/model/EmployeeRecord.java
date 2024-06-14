package com.example.EmployeePair.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeRecord {
    private int employeeId;
    private int projectId;
    private LocalDate startDate;
    private LocalDate endDate;
}
