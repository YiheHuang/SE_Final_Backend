package com.javaee.se_final_backend.controller.health;

import com.javaee.se_final_backend.model.entity.ExaminationReport;
import com.javaee.se_final_backend.service.ExaminationReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class HealthReportController {

    private final ExaminationReportService service;

    @PostMapping("/upload")
    public void upload(
            @RequestParam Integer userId,
            @RequestParam MultipartFile file
    ) throws Exception {
        service.upload(userId, file);
    }

    @GetMapping("/list")
    public List<ExaminationReport> list(@RequestParam Integer userId) {
        return service.list(userId);
    }

    @GetMapping("/file")
    public void file(
            @RequestParam String path,
            HttpServletResponse resp
    ) throws Exception {

        File file = service.loadFile(path);

        resp.setContentType("application/pdf");
        resp.setHeader(
                "Content-Disposition",
                "inline; filename=\"" + path + "\""
        );

        Files.copy(file.toPath(), resp.getOutputStream());
    }
}

