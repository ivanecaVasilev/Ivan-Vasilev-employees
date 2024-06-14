package com.example.EmployeePair.controller;

import com.example.EmployeePair.model.PairRecord;
import com.example.EmployeePair.services.FileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping()
    public List<PairRecord> getData(@RequestParam String csvPath) {
        return fileService.getAllPairs(csvPath);
    }
}
