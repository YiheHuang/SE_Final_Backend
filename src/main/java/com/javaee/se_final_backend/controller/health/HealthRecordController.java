package com.javaee.se_final_backend.controller.health;

import com.javaee.se_final_backend.model.entity.HealthRecord;
import com.javaee.se_final_backend.service.HealthRecordService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/api/health/record")
@CrossOrigin
public class HealthRecordController {

    @Autowired
    private HealthRecordService service;

    @GetMapping
    public List<HealthRecord> list(@RequestParam Integer userId) {
        return service.listRecords(userId);
    }

    @PostMapping
    public void add(@RequestParam Integer userId,
                    @RequestBody HealthRecord record) {
        service.addRecord(userId, record);
    }
}
