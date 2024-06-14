package com.example.EmployeePair.controller;

import com.example.EmployeePair.model.PairRecord;
import com.example.EmployeePair.services.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/file")
@CrossOrigin
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping()
    public List<PairRecord> getData(@RequestBody MultipartFile file ) {
        return fileService.uploadFileAndGetAllPairs(file);
    }
}
