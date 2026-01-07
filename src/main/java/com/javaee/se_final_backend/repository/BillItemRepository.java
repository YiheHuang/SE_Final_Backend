package com.javaee.se_final_backend.repository;

import com.javaee.se_final_backend.model.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillItemRepository extends JpaRepository<BillItem, Integer>{
    List<BillItem> findByBillId(Integer billId);
}
