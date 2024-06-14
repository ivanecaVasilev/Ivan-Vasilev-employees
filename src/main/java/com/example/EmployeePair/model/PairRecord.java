package com.example.EmployeePair.model;

import lombok.Data;

@Data
public class PairRecord {
    private int employeeOneID;
    private int employeeTwoID;
    private int projectID;
    private long days;
}
