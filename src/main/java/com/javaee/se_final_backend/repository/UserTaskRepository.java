package com.javaee.se_final_backend.repository;

import com.javaee.se_final_backend.model.entity.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserTaskRepository extends JpaRepository<UserTask, Integer>{
    List<UserTask> findByUserId(Integer userId);

    // 根据taskId查询所有关联
    List<UserTask> findByTaskId(Integer taskId);

    // 删除任务的所有关联
    @Modifying
    @Query("DELETE FROM UserTask ut WHERE ut.taskId = :taskId")
    void deleteByTaskId(@Param("taskId") Integer taskId);

    // 查询单个用户的任务ID列表
    @Query("SELECT ut.taskId FROM UserTask ut WHERE ut.userId = :userId")
    List<Integer> findTaskIdsByUserId(@Param("userId") Integer userId);

    // 查询家庭所有成员的任务ID
    @Query(value = "SELECT DISTINCT ut.task_id FROM user_task ut " +
            "INNER JOIN user u ON ut.user_id = u.id " +
            "WHERE u.family_id = :familyId",
            nativeQuery = true)
    List<Integer> findTaskIdsByFamilyId(@Param("familyId") Integer familyId);
}
