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
    @Query(value = """
    update task t
    set t.status = 'DOING'
    where t.status = 'TODO'
      and NOW() >= DATE_ADD(t.begin_time, INTERVAL 8 HOUR)
      and NOW() < DATE_ADD(t.end_time, INTERVAL 8 HOUR)
    """, nativeQuery = true)
    int updateTodoToDoing();


    // 根据ID列表查询任务
    List<Task> findByIdIn(List<Integer> taskIds);

    // 查询某时间段内的任务（用于周视图）
    @Query("SELECT t FROM Task t WHERE t.id IN :taskIds AND " +
            "((t.beginTime >= :weekStart AND t.beginTime < :weekEnd) OR " +
            "(t.endTime > :weekStart AND t.endTime <= :weekEnd) OR " +
            "(t.beginTime <= :weekStart AND t.endTime >= :weekEnd))")
    List<Task> findTasksInWeek(@Param("taskIds") List<Integer> taskIds,
                               @Param("weekStart") LocalDateTime weekStart,
                               @Param("weekEnd") LocalDateTime weekEnd);

    // 查询某月有任务的日期
    @Query(value = "SELECT DISTINCT DATE_FORMAT(t.begin_time, '%Y-%m-%d') " +
            "FROM task t " +
            "WHERE t.id IN :taskIds " +
            "AND t.begin_time >= :monthStart " +
            "AND t.begin_time < :monthEnd",
            nativeQuery = true)
    List<String> findDatesWithTasks(@Param("taskIds") List<Integer> taskIds,
                                    @Param("monthStart") LocalDateTime monthStart,
                                    @Param("monthEnd") LocalDateTime monthEnd);

    // 查询用户在指定时间段内的所有任务（用于冲突检测）
    @Query("SELECT t FROM Task t WHERE t.id IN :taskIds AND " +
            "((t.beginTime < :endTime AND t.endTime > :beginTime)) AND " +
            "(" +
            "  t.parentId != t.id " +
            "  OR " +
            "  (t.parentId = t.id AND NOT EXISTS (SELECT s FROM Task s WHERE s.parentId = t.id AND s.id != t.id))" +
            ")")
    List<Task> findConflictingTasks(@Param("taskIds") List<Integer> taskIds,
                                    @Param("beginTime") LocalDateTime beginTime,
                                    @Param("endTime") LocalDateTime endTime);

    // 查询子任务（parentId等于指定值，但id不等于parentId）
    @Query("SELECT t FROM Task t WHERE t.parentId = :parentId AND t.id <> :parentId")
    List<Task> findSubTasksByParentId(@Param("parentId") Integer parentId);

    // 查询同一重复组的所有任务
    @Query("SELECT t FROM Task t WHERE t.parentId = :groupId")
    List<Task> findByRepeatGroupId(@Param("groupId") Integer groupId);

    // 统计某时间段内的任务时长（按类型分组）
    @Query(value = "SELECT t.type, " +
            "SUM(TIMESTAMPDIFF(MINUTE, " +
            "    GREATEST(t.begin_time, :startTime), " +  // 取较晚的开始时间
            "    LEAST(t.end_time, :endTime)" +           // 取较早的结束时间
            ")) " +
            "FROM task t WHERE t.id IN :taskIds AND t.parent_id = t.id AND " +
            "t.begin_time < :endTime AND t.end_time > :startTime " +  // 任务与时间段有交集
            "GROUP BY t.type",
            nativeQuery = true)
    List<Object[]> getTaskStatisticsByType(@Param("taskIds") List<Integer> taskIds,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);



    // 将指定任务的parentId设为NULL
    @Modifying
    @Query(value = "UPDATE task SET parent_id = NULL WHERE id = :taskId", nativeQuery = true)
    void clearParentId(@Param("taskId") Integer taskId);

    // 将所有指向某任务的parentId设为NULL
    @Modifying
    @Query(value = "UPDATE task SET parent_id = NULL WHERE parent_id = :parentId", nativeQuery = true)
    void clearParentIdByParent(@Param("parentId") Integer parentId);

    // 删除任务
    @Modifying
    @Query(value = "DELETE FROM task WHERE id = :taskId", nativeQuery = true)
    void deleteTaskById(@Param("taskId") Integer taskId);
}