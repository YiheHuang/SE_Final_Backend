package com.javaee.se_final_backend.repository;

import com.javaee.se_final_backend.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer>{
    List<Task> findByTypeAndIdIn(String type, List<Integer> taskIds);
}
