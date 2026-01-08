package com.javaee.se_final_backend.scheduler;

import com.javaee.se_final_backend.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskStatusScheduler {

    private final TaskRepository taskRepository;

    /**
     * 每到整分钟执行一次
     * cron: 秒 分 时 日 月 星期
     */
    @Transactional
    @Scheduled(cron = "0 * * * * ?")
    public void updateTaskStatus() {
        log.info("Task status update scheduled");
        LocalDateTime now = LocalDateTime.now();

        int todoToDoing = taskRepository.updateTodoToDoing();


        log.info("Task status update finished");

        if (todoToDoing > 0 ) {
            log.info(
                    "Task status updated: TODO->DOING={},  time={}",
                    todoToDoing, now
            );
        }
    }
}
