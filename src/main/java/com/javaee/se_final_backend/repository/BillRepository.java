package com.javaee.se_final_backend.repository;

import com.javaee.se_final_backend.model.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Integer>{
    List<Bill> findByUserIdIn(List<Integer> userIds);
    List<Bill> findByUserIdInAndBeginDateBetween(List<Integer> userIds, LocalDateTime begin, LocalDateTime end);
}
