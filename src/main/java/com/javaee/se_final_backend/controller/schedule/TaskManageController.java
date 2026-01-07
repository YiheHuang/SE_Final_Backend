package com.javaee.se_final_backend.controller.schedule;

import com.javaee.se_final_backend.model.DTO.ApiResponse;
import com.javaee.se_final_backend.model.DTO.TaskCreateRequest;
import com.javaee.se_final_backend.model.DTO.TaskDetailResponse;
import com.javaee.se_final_backend.model.DTO.TaskStatisticsDTO;
import com.javaee.se_final_backend.service.TaskManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 事务管理控制器
 * 处理家庭事务管理相关的所有接口
 */
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
@CrossOrigin
public class TaskManageController {

    private final TaskManageService taskManageService;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * 获取家庭成员列表
     * GET /api/task/family-members?userId=1
     */
    @GetMapping("/family-members")
    public ApiResponse<List<Map<String, Object>>> getFamilyMembers(@RequestParam Integer userId) {
        try {
            List<Map<String, Object>> members = taskManageService.getFamilyMembers(userId);
            return ApiResponse.success(members);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取周任务列表
     * GET /api/task/weekly?userId=1&weekStart=2026-01-06T00:00:00
     */
    @GetMapping("/weekly")
    public ApiResponse<List<Map<String, Object>>> getWeeklyTasks(
            @RequestParam Integer userId,
            @RequestParam String weekStart) {
        try {
            LocalDateTime weekStartTime = LocalDateTime.parse(weekStart, DATETIME_FORMATTER);
            List<Map<String, Object>> tasks = taskManageService.getWeeklyTasks(userId, weekStartTime);
            return ApiResponse.success(tasks);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取日历中有任务的日期
     * GET /api/task/calendar/dates?userId=1&year=2026&month=1
     */
    @GetMapping("/calendar/dates")
    public ApiResponse<List<String>> getCalendarDates(
            @RequestParam Integer userId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        try {
            List<String> dates = taskManageService.getCalendarDates(userId, year, month);
            return ApiResponse.success(dates);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取任务详情
     * GET /api/task/detail/{taskId}
     */
    @GetMapping("/detail/{taskId}")
    public ApiResponse<TaskDetailResponse> getTaskDetail(@PathVariable Integer taskId) {
        try {
            TaskDetailResponse detail = taskManageService.getTaskDetail(taskId);
            return ApiResponse.success(detail);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建任务
     * POST /api/task/create
     */
    @PostMapping("/create")
    public ApiResponse<TaskDetailResponse> createTask(
            @RequestParam(required = false) Integer userId,
            @RequestBody TaskCreateRequest request) {
        try {
            // 如果没有指定userId，使用参与人员中的第一个
            if (userId == null && request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
                userId = request.getParticipantIds().get(0);
            }
            if (userId == null) {
                return ApiResponse.error("用户ID不能为空");
            }
            return taskManageService.createTask(userId, request);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新任务
     * PUT /api/task/update/{taskId}
     */
    @PutMapping("/update/{taskId}")
    public ApiResponse<TaskDetailResponse> updateTask(
            @PathVariable Integer taskId,
            @RequestBody TaskCreateRequest request) {
        try {
            return taskManageService.updateTask(taskId, request);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新任务状态
     * PUT /api/task/status/{taskId}
     */
    @PutMapping("/status/{taskId}")
    public ApiResponse<Void> updateTaskStatus(
            @PathVariable Integer taskId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            if (status == null) {
                return ApiResponse.error("状态不能为空");
            }
            return taskManageService.updateTaskStatus(taskId, status);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除任务
     * DELETE /api/task/delete/{taskId}?deleteAll=false
     */
    @DeleteMapping("/delete/{taskId}")
    public ApiResponse<Void> deleteTask(
            @PathVariable Integer taskId,
            @RequestParam(required = false, defaultValue = "false") Boolean deleteAll) {
        try {
            return taskManageService.deleteTask(taskId, deleteAll);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查是否为重复任务
     * GET /api/task/is-repeat/{taskId}
     */
    @GetMapping("/is-repeat/{taskId}")
    public ApiResponse<Map<String, Object>> isRepeatTask(@PathVariable Integer taskId) {
        try {
            Map<String, Object> result = taskManageService.isRepeatTask(taskId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取任务统计
     * GET /api/task/statistics?userId=1&period=week
     * period: week/month/year
     */
    @GetMapping("/statistics")
    public ApiResponse<List<TaskStatisticsDTO>> getStatistics(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "week") String period) {
        try {
            List<TaskStatisticsDTO> stats = taskManageService.getStatistics(userId, period);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
