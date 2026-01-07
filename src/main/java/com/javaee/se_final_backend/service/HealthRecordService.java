package com.javaee.se_final_backend.service;

import com.javaee.se_final_backend.model.entity.HealthRecord;
import com.javaee.se_final_backend.repository.HealthRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HealthRecordService {

    @Autowired
    private HealthRecordRepository repository;

    public void addRecord(Integer userId, HealthRecord record) {
        record.setUserId(userId);
        record.setDate(LocalDateTime.now());
        repository.save(record);
    }

    public List<HealthRecord> listRecords(Integer userId) {
        return repository.findByUserIdOrderByDateDesc(userId);
    }
}
