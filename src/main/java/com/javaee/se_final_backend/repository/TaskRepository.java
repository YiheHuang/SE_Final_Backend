package com.javaee.se_final_backend.repository;

import com.javaee.se_final_backend.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface TaskRepository extends JpaRepository<Task, Integer>{
    List<Task> findByTypeAndIdIn(String type, List<Integer> taskIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Task t
    set t.status = 'DOING'
    where t.status = 'TODO'
      and CURRENT_TIMESTAMP >= t.beginTime
      and CURRENT_TIMESTAMP < t.endTime
""")
    int updateTodoToDoing();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Task t
    set t.status = 'DONE'
    where t.status = 'DOING' or t.status = 'TODO'
      and CURRENT_TIMESTAMP >= t.endTime
""")
    int updateDoingToDone();
}
