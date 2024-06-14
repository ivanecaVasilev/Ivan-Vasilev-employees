package com.example.EmployeePair.services;

import com.example.EmployeePair.model.EmployeeRecord;
import com.example.EmployeePair.model.PairRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@Slf4j
public class FileService {

    @Value("${csv.delimiter}")
    private String COMMA_DELIMITER;

    private Map<String, ArrayList<PairRecord>> pairs;
    private Map<String, Long> totalDaysOfPair;

    public FileService() {
        this.pairs = new HashMap<>();
        this.totalDaysOfPair = new HashMap<>();
    }


    public List<PairRecord> uploadFileAndGetAllPairs(MultipartFile file) {
        resetPairs();
        List<EmployeeRecord> employeeRecords = readCsvFile(file);
        Map<String, ArrayList<PairRecord>> pairRecords = getPairRecords(employeeRecords);
        List<String> sortedPairsByTotalDays = sortByValue(totalDaysOfPair);
        List<PairRecord> result = new ArrayList<>();
        for (String pair : sortedPairsByTotalDays) {
            result.addAll(pairRecords.get(pair));
        }
        return result;
    }


    private void resetPairs() {
        pairs = new HashMap<>();
        totalDaysOfPair = new HashMap<>();
    }


    private List<EmployeeRecord> readCsvFile(MultipartFile file) {
        List<EmployeeRecord> records;
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            records = reader.lines()
                    .map(line -> Arrays.asList(line.split(COMMA_DELIMITER)))
                    .map(this::mapToEmployeeRecord)
                    .toList();
            log.info("CSV file {} read successfully.", file.getName());
        } catch (IOException e) {
            log.error("Error occurred while reading CSV file {}.", file.getName(), e);
            throw new RuntimeException(e);
        }
        return records;
    }

    private Map<String, ArrayList<PairRecord>> getPairRecords(List<EmployeeRecord> records) {
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
                            calculateTotalOverlapForPair(firstEmployee.getEmployeeId(), secondEmployee.getEmployeeId(), overlapDays);
                        }
                    }
                }
            }
        }
        return pairs;
    }

    private Map<Integer, List<EmployeeRecord>> mapByProjectId(List<EmployeeRecord> records) {
        return records.stream().collect(Collectors.groupingBy(EmployeeRecord::getProjectId));
    }


    private void addCommonProject(EmployeeRecord firstEmployee, EmployeeRecord secondEmployee, long overlapDays) {
        PairRecord pairRecord = new PairRecord();
        pairRecord.setEmployeeOneId(firstEmployee.getEmployeeId());
        pairRecord.setEmployeeTwoId(secondEmployee.getEmployeeId());
        pairRecord.setProjectId(secondEmployee.getProjectId());
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

    private void calculateTotalOverlapForPair(int firstEmployeeId, int secondEmployeeId, long overlapDays) {
        String key = String.format("%d and %d", firstEmployeeId, secondEmployeeId);
        if (totalDaysOfPair.containsKey(key))
            totalDaysOfPair.put(key, totalDaysOfPair.get(key) + overlapDays);
        else
            totalDaysOfPair.put(key, overlapDays);
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
