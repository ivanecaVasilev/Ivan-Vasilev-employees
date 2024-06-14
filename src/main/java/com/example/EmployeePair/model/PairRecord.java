package com.example.EmployeePair.model;

import lombok.Data;

@Data
public class PairRecord {
    private int employeeOneId;
    private int employeeTwoId;
    private int projectId;
    private long days;
}
