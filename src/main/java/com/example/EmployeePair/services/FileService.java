package com.example.EmployeePair.services;

import com.example.EmployeePair.model.EmployeeRecord;
import com.example.EmployeePair.model.PairRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@Slf4j
public class FileService {

    @Value("${csv.delimiter}")
    private String COMMA_DELIMITER;

    private final Map<String, ArrayList<PairRecord>> pairs;
    private final Map<String, Long> totalDaysOfPair;

    public FileService(Map<String, ArrayList<PairRecord>> pairs, Map<String, Long> totalDaysOfPair) {
        this.pairs = pairs;
        this.totalDaysOfPair = totalDaysOfPair;
    }


    public List<PairRecord> getAllPairs(String csvPath) {
        List<EmployeeRecord> employeeRecords = readAllRecords(csvPath);
        Map<String, ArrayList<PairRecord>> commonProjectRecords = getCommonProjectRecords(employeeRecords);
        List<String> sortedPairsByTotalDays = sortByValue(totalDaysOfPair);
        List<PairRecord> result = new ArrayList<>();
        for (String pair : sortedPairsByTotalDays) {
            result.addAll(commonProjectRecords.get(pair));
        }
        return result;
    }

    private List<String> sortByValue(Map<String, Long> totalDaysOfPair) {
        List<String> resultmap
                = totalDaysOfPair.entrySet()
                .stream()
                .sorted((f1, f2) -> Long.compare(f2.getValue(), f1.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        return resultmap;
    }

    private List<EmployeeRecord> readAllRecords(String csvPath) {
        List<EmployeeRecord> records;

        try (
                BufferedReader reader = Files.newBufferedReader(Paths.get(csvPath))) {
            records = reader.lines()
                    .map(line -> Arrays.asList(line.split(COMMA_DELIMITER)))
                    .map(this::mapToEmployeeRecord)
                    .toList();
            log.info("CSV file {} read successfully.", csvPath);
        } catch (IOException e) {
            log.error("Error occurred while reading CSV file {}.", csvPath, e);
            throw new RuntimeException(e);
        }
        return records;
    }


    private Map<Integer, List<EmployeeRecord>> mapByProjectId(List<EmployeeRecord> records) {
        return records.stream().collect(Collectors.groupingBy(EmployeeRecord::getProjectId));
    }

    private Map<String, ArrayList<PairRecord>> getCommonProjectRecords(List<EmployeeRecord> records) {
        Map<Integer, List<EmployeeRecord>> employeeListByProjectId = mapByProjectId(records);
        for (Map.Entry<Integer, List<EmployeeRecord>> projectIdEntry : employeeListByProjectId.entrySet()) {
            for (int i = 0; i < projectIdEntry.getValue().size() - 1; i++) {
                for (int j = i + 1; j < projectIdEntry.getValue().size(); j++) {
                    EmployeeRecord firstEmployee = projectIdEntry.getValue().get(i);
                    EmployeeRecord secondEmployee = projectIdEntry.getValue().get(j);

                    if (isOverlapping(firstEmployee, secondEmployee)) {
                        long overlapDays = calculateOverlap(firstEmployee, secondEmployee);

                        if (overlapDays > 0) {
                            addCommonProject(firstEmployee, secondEmployee, overlapDays);
                            calculateTotalOverlap(firstEmployee.getEmployeeId(), secondEmployee.getEmployeeId(), overlapDays);
                        }
                    }
                }
            }
        }
        return pairs;
    }


    private void calculateTotalOverlap(int firstEmployeeId, int secondEmployeeId, long overlapDays) {
        String key = String.format("%d and %d", firstEmployeeId, secondEmployeeId);
        if (totalDaysOfPair.containsKey(key))
            totalDaysOfPair.put(key, totalDaysOfPair.get(key) + overlapDays);
        else
            totalDaysOfPair.put(key, overlapDays);
    }

    private void addCommonProject(EmployeeRecord firstEmployee, EmployeeRecord secondEmployee, long overlapDays) {
        PairRecord pairRecord = new PairRecord();
        pairRecord.setEmployeeOneID(firstEmployee.getEmployeeId());
        pairRecord.setEmployeeTwoID(secondEmployee.getEmployeeId());
        pairRecord.setProjectID(secondEmployee.getProjectId());
        pairRecord.setDays(overlapDays);
        String key = String.format("%d and %d", firstEmployee.getEmployeeId(), secondEmployee.getEmployeeId());
        if (pairs.containsKey(key)) {
            List<PairRecord> pairRecords = pairs.get(key);
            pairRecords.add(pairRecord);
        } else {
            ArrayList<PairRecord> pairRecords = new ArrayList<>();
            pairRecords.add(pairRecord);
            pairs.put(key, pairRecords);
        }
    }

    private long calculateOverlap(EmployeeRecord firstEmployee, EmployeeRecord secondEmployee) {

        LocalDate initialDate = firstEmployee.getStartDate()
                .isAfter(secondEmployee.getStartDate())
                ? firstEmployee.getStartDate()
                : secondEmployee.getStartDate();

        LocalDate finalDate = firstEmployee.getEndDate()
                .isBefore(secondEmployee.getEndDate())
                ? firstEmployee.getEndDate()
                : secondEmployee.getEndDate();

        return DAYS.between(initialDate, finalDate);
    }

    private boolean isOverlapping(EmployeeRecord firstEmployee, EmployeeRecord secondEmployee) {
        return (firstEmployee.getStartDate().isBefore(secondEmployee.getEndDate())
                || firstEmployee.getStartDate().isEqual(secondEmployee.getEndDate()))
                && (firstEmployee.getEndDate().isAfter(secondEmployee.getStartDate())
                || firstEmployee.getEndDate().isEqual(secondEmployee.getStartDate()));
    }


    private EmployeeRecord mapToEmployeeRecord(List<String> record) {
        DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ofPattern("[MM/dd/yyyy]" + "[dd-MM-yyyy]" + "[yyyy-MM-dd]" + "[uuuu-M-d]"));
        DateTimeFormatter dateTimeFormatter = dateTimeFormatterBuilder.toFormatter();
        EmployeeRecord employeeRecord = new EmployeeRecord();
        employeeRecord.setEmployeeId(Integer.parseInt(record.get(0)));
        employeeRecord.setProjectId(Integer.parseInt(record.get(1)));
        employeeRecord.setStartDate(LocalDate.parse(record.get(2), dateTimeFormatter));
        employeeRecord.setEndDate(record.get(3).equals("NULL") ? LocalDate.now() : LocalDate.parse(record.get(3), dateTimeFormatter));
        return employeeRecord;
    }
}
