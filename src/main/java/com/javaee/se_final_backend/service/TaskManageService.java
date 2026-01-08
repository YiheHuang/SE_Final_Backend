package com.javaee.se_final_backend.service;

import com.javaee.se_final_backend.model.DTO.*;
import com.javaee.se_final_backend.model.entity.Task;
import com.javaee.se_final_backend.model.entity.User;
import com.javaee.se_final_backend.model.entity.UserTask;
import com.javaee.se_final_backend.repository.TaskRepository;
import com.javaee.se_final_backend.repository.UserRepository;
import com.javaee.se_final_backend.repository.UserTaskRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskManageService {

    private final TaskRepository taskRepository;
    private final UserTaskRepository userTaskRepository;
    private final UserRepository userRepository;

    // 类型颜色映射
    private static final Map<String, String> TYPE_COLORS = Map.of(
            "study", "#FF6B6B",
            "sport", "#4ECDC4",
            "work", "#FFE66D",
            "entertainment", "#95E1D3",
            "meeting", "#DDA0DD",
            "life", "#87CEEB",
            "other", "#C8C8C8"
    );

    // 状态描述映射
    private static final Map<String, String> STATUS_DESC = Map.of(
            "TODO", "待办",
            "DOING", "进行中",
            "DONE", "已完成"
    );

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 获取家庭成员列表
     */
    public List<Map<String, Object>> getFamilyMembers(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getFamilyId() == null) {
            // 没有家庭，只返回自己
            return List.of(Map.of(
                    "userId", user.getId(),
                    "name", user.getName(),
                    "role", user.getRole() != null ? user.getRole() : "成员"
            ));
        }

        List<User> familyMembers = userRepository.findByFamilyId(user.getFamilyId());
        return familyMembers.stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", u.getId());
                    map.put("name", u.getName());
                    map.put("role", u.getRole() != null ? u.getRole() : "成员");
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取周任务列表
     */
    public List<Map<String, Object>> getWeeklyTasks(Integer userId, LocalDateTime weekStart) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        LocalDateTime weekEnd = weekStart.plusDays(7);

        List<Integer> taskIds;

        taskIds = userTaskRepository.findTaskIdsByUserId(userId);

        if (taskIds == null || taskIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询该周的任务
        List<Task> tasks = taskRepository.findTasksInWeek(taskIds, weekStart, weekEnd);

        // 只返回主任务（parentId == id 的是主任务或独立任务）
        return tasks.stream()
                .filter(t -> t.getParentId() == null || t.getParentId().equals(t.getId()))
                .map(task -> buildTaskMap(task, userId))
                .collect(Collectors.toList());
    }

    /**
     * 获取某月有任务的日期列表
     */
    public List<String> getCalendarDates(Integer userId, Integer year, Integer month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        List<Integer> taskIds = userTaskRepository.findByUserId(userId).stream()
                .map(UserTask::getTaskId)
                .collect(Collectors.toList());
        if (taskIds.isEmpty()) {
            return Collections.emptyList();
        }

        return taskRepository.findDatesWithTasks(taskIds, monthStart, monthEnd);
    }

    /**
     * 获取任务详情
     */
    public TaskDetailResponse getTaskDetail(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        TaskDetailResponse response = new TaskDetailResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setContent(task.getContent());
        response.setType(task.getType());
        response.setTypeColor(TYPE_COLORS.getOrDefault(task.getType(), "#C8C8C8"));
        response.setStatus(task.getStatus());
        response.setStatusDesc(STATUS_DESC.getOrDefault(task.getStatus(), "未知"));
        response.setBeginTime(task.getBeginTime().format(DATETIME_FORMATTER));
        response.setEndTime(task.getEndTime().format(DATETIME_FORMATTER));
        response.setParentId(task.getParentId());
        response.setProgress(task.getProgress() != null ? task.getProgress() : 0);

        // 判断是否为复合事务
        boolean isComposite = task.getParentId() != null && task.getParentId().equals(task.getId());
        List<Task> subTasks = taskRepository.findSubTasksByParentId(task.getId());
        response.setIsComposite(!subTasks.isEmpty());

        // 获取参与人员
        List<UserTask> userTasks = userTaskRepository.findByTaskId(taskId);
        List<TaskDetailResponse.ParticipantInfo> participants = userTasks.stream()
                .map(ut -> {
                    User user = userRepository.findById(ut.getUserId()).orElse(null);
                    if (user == null) return null;
                    TaskDetailResponse.ParticipantInfo info = new TaskDetailResponse.ParticipantInfo();
                    info.setUserId(user.getId());
                    info.setName(user.getName());
                    info.setRole(user.getRole() != null ? user.getRole() : "成员");
                    return info;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        response.setParticipants(participants);

        // 获取子任务
        if (!subTasks.isEmpty()) {
            List<TaskDetailResponse.SubTaskInfo> subTaskInfos = subTasks.stream()
                    .map(st -> {
                        TaskDetailResponse.SubTaskInfo info = new TaskDetailResponse.SubTaskInfo();
                        info.setId(st.getId());
                        info.setTitle(st.getTitle());
                        info.setBeginTime(st.getBeginTime().format(DATETIME_FORMATTER));
                        info.setEndTime(st.getEndTime().format(DATETIME_FORMATTER));
                        info.setStatus(st.getStatus());
                        info.setStatusDesc(STATUS_DESC.getOrDefault(st.getStatus(), "未知"));
                        return info;
                    })
                    .sorted(Comparator.comparing(TaskDetailResponse.SubTaskInfo::getBeginTime))
                    .collect(Collectors.toList());
            response.setSubTasks(subTaskInfos);

            // 计算进度
            response.setProgress(calculateProgress(subTasks));
        }

        // 判断是否为重复任务
        response.setIsRepeat(task.getParentId() != null && !task.getParentId().equals(task.getId()));
        response.setRepeatGroupId(task.getParentId());

        return response;
    }

    /**
     * 创建任务
     */
    @Transactional
    public ApiResponse<TaskDetailResponse> createTask(Integer userId, TaskCreateRequest request) {
        // 1. 验证必填字段
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ApiResponse.error("标题不能为空");
        }

        LocalDateTime beginTime = LocalDateTime.parse(request.getBeginTime(), DATETIME_FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(request.getEndTime(), DATETIME_FORMATTER);

        // 2. 验证时间
        if (beginTime.isBefore(LocalDateTime.now())) {
            return ApiResponse.error("不能创建过去的任务");
        }

        if (endTime.isBefore(beginTime) || endTime.equals(beginTime)) {
            return ApiResponse.error("结束时间必须晚于开始时间");
        }

        // 3. 确定参与人员
        List<Integer> participantIds = request.getParticipantIds();
        if (participantIds == null || participantIds.isEmpty()) {
            participantIds = List.of(userId);
        }

        // 4. 获取重复周数
        int repeatWeeks = 1;
        if (Boolean.TRUE.equals(request.getIsRepeat()) && request.getRepeatWeeks() != null) {
            repeatWeeks = Math.min(Math.max(request.getRepeatWeeks(), 1), 50);
        }

        // 5. 冲突检测 - 检测所有重复周
        for (int week = 0; week < repeatWeeks; week++) {
            LocalDateTime weekBegin = beginTime.plusWeeks(week);
            LocalDateTime weekEnd = endTime.plusWeeks(week);

            ConflictInfo conflict = checkConflict(participantIds, weekBegin, weekEnd, null);
            if (conflict != null) {
                String weekInfo = repeatWeeks > 1 ? String.format("（第%d周）", week + 1) : "";
                return ApiResponse.error(weekInfo + conflict.toMessage());
            }
        }

        // 6. 创建任务 (循环创建平行的独立任务)
        List<Task> createdTasks = new ArrayList<>();


        for (int week = 0; week < repeatWeeks; week++) {
            LocalDateTime weekBegin = beginTime.plusWeeks(week);
            LocalDateTime weekEnd = endTime.plusWeeks(week);

            Task task = new Task();
            task.setTitle(request.getTitle().trim());
            task.setContent(request.getContent());
            task.setType(request.getType() != null ? request.getType() : "other");
            task.setStatus(request.getStatus() != null ? request.getStatus() : "TODO");
            task.setBeginTime(weekBegin);
            task.setEndTime(weekEnd);
            task.setProgress(0);

            // 先保存以获取 ID
            taskRepository.save(task);

            task.setParentId(task.getId());
            taskRepository.save(task);

            createdTasks.add(task);

            // 创建参与人员关联
            for (Integer participantId : participantIds) {
                UserTask ut = new UserTask();
                ut.setUserId(participantId);
                ut.setTaskId(task.getId());
                userTaskRepository.save(ut);
            }

            // 处理复合事务的子任务
            if (Boolean.TRUE.equals(request.getIsComposite()) &&
                    request.getSubTasks() != null && !request.getSubTasks().isEmpty()) {

                for (TaskCreateRequest.SubTaskRequest subReq : request.getSubTasks()) {
                    LocalDateTime subBegin = LocalDateTime.parse(subReq.getBeginTime(), DATETIME_FORMATTER).plusWeeks(week);
                    LocalDateTime subEnd = LocalDateTime.parse(subReq.getEndTime(), DATETIME_FORMATTER).plusWeeks(week);

                    Task subTask = new Task();
                    subTask.setTitle(subReq.getTitle());
                    subTask.setType(request.getType());
                    subTask.setStatus(subReq.getStatus() != null ? subReq.getStatus() : "TODO");
                    subTask.setBeginTime(subBegin);
                    subTask.setEndTime(subEnd);

                    // 子任务指向当周的主任务 ID，这是正确的内部结构
                    subTask.setParentId(task.getId());
                    subTask.setProgress(0);

                    taskRepository.save(subTask);

                    // 子任务关联人员
                    for (Integer participantId : participantIds) {
                        UserTask subUt = new UserTask();
                        subUt.setUserId(participantId);
                        subUt.setTaskId(subTask.getId());
                        userTaskRepository.save(subUt);
                    }
                }

                // 更新当前这周任务的进度
                updateCompositeTaskProgress(task.getId());
            }
        }

        return ApiResponse.success("创建成功", getTaskDetail(createdTasks.get(0).getId()));
    }

    @Autowired
    private EntityManager entityManager;
    /**
     * 更新任务
     */
    @Transactional
    public ApiResponse<TaskDetailResponse> updateTask(Integer taskId, TaskCreateRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        LocalDateTime beginTime = LocalDateTime.parse(request.getBeginTime(), DATETIME_FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(request.getEndTime(), DATETIME_FORMATTER);

        if (endTime.isBefore(beginTime) || endTime.equals(beginTime)) {
            return ApiResponse.error("结束时间必须晚于开始时间");
        }

        // 确定参与人员
        List<Integer> participantIds = request.getParticipantIds();


        // 冲突检测（排除自己）
        ConflictInfo conflict = checkConflict(participantIds, beginTime, endTime, taskId);
        if (conflict != null) {
            return ApiResponse.error(conflict.toMessage());
        }



        // 更新任务
        task.setTitle(request.getTitle().trim());
        task.setContent(request.getContent());
        task.setType(request.getType() != null ? request.getType() : task.getType());
        task.setStatus(request.getStatus() != null ? request.getStatus() : task.getStatus());
        task.setBeginTime(beginTime);
        task.setEndTime(endTime);
        taskRepository.save(task);



        userTaskRepository.flush();
        entityManager.clear();
        // 处理子任务
        if (Boolean.TRUE.equals(request.getIsComposite()) && request.getSubTasks() != null) {
            // 获取现有子任务
            List<Task> existingSubTasks = taskRepository.findSubTasksByParentId(taskId);
            Set<Integer> updatedSubTaskIds = new HashSet<>();

            for (TaskCreateRequest.SubTaskRequest subReq : request.getSubTasks()) {
                LocalDateTime subBegin = LocalDateTime.parse(subReq.getBeginTime(), DATETIME_FORMATTER);
                LocalDateTime subEnd = LocalDateTime.parse(subReq.getEndTime(), DATETIME_FORMATTER);

                Task subTask;
                if (subReq.getId() != null) {
                    // 更新现有子任务
                    subTask = taskRepository.findById(subReq.getId())
                            .orElse(new Task());
                    updatedSubTaskIds.add(subReq.getId());
                } else {
                    // 创建新子任务
                    subTask = new Task();
                }

                subTask.setTitle(subReq.getTitle());
                subTask.setType(task.getType());
                subTask.setStatus(subReq.getStatus() != null ? subReq.getStatus() : "TODO");
                subTask.setBeginTime(subBegin);
                subTask.setEndTime(subEnd);
                subTask.setParentId(taskId);
                subTask.setProgress(0);

                taskRepository.save(subTask);

            }

            // 删除不再需要的子任务
            for (Task existingSub : existingSubTasks) {
                if (!updatedSubTaskIds.contains(existingSub.getId())) {
                    userTaskRepository.deleteByTaskId(existingSub.getId());
                    taskRepository.delete(existingSub);
                }
            }

            // 更新复合任务进度
            updateCompositeTaskProgress(taskId);
        }

        return ApiResponse.success("更新成功", getTaskDetail(taskId));
    }

    /**
     * 更新任务状态
     */
    @Transactional
    public ApiResponse<Void> updateTaskStatus(Integer taskId, String status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        task.setStatus(status);
        taskRepository.save(task);

        // 如果是子任务，更新父任务进度
        if (task.getParentId() != null && !task.getParentId().equals(task.getId())) {
            updateCompositeTaskProgress(task.getParentId());
        }

        return ApiResponse.success("状态已更新", null);
    }

    /**
     * 删除任务
     */
    @Transactional
    public ApiResponse<Void> deleteTask(Integer taskId, Boolean deleteAll) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        try {
            if (Boolean.TRUE.equals(deleteAll) && task.getParentId() != null && !task.getParentId().equals(task.getId())) {
                // 删除所有重复任务
                Integer groupId = task.getParentId();
                List<Task> repeatTasks = taskRepository.findByRepeatGroupId(groupId);

                // 收集所有需要删除的任务ID（包括子任务）
                List<Integer> allTaskIds = new ArrayList<>();
                for (Task rt : repeatTasks) {
                    // 收集子任务ID
                    List<Task> subTasks = taskRepository.findSubTasksByParentId(rt.getId());
                    for (Task st : subTasks) {
                        allTaskIds.add(st.getId());
                    }
                    allTaskIds.add(rt.getId());
                }

                for (Integer id : allTaskIds) {
                    userTaskRepository.deleteByTaskId(id);
                }

                for (Integer id : allTaskIds) {
                    taskRepository.clearParentId(id);
                }

                for (Integer id : allTaskIds) {
                    taskRepository.deleteTaskById(id);
                }

            } else {
                // 只删除当前任务及其子任务
                List<Task> subTasks = taskRepository.findSubTasksByParentId(taskId);

                // 收集所有要删除的ID
                List<Integer> toDeleteIds = new ArrayList<>();
                for (Task st : subTasks) {
                    toDeleteIds.add(st.getId());
                }
                toDeleteIds.add(taskId);

                // 删除UserTask关联
                for (Integer id : toDeleteIds) {
                    userTaskRepository.deleteByTaskId(id);
                }

                // 清除parentId
                for (Integer id : toDeleteIds) {
                    taskRepository.clearParentId(id);
                }

                // 清除其他任务对当前任务的引用
                taskRepository.clearParentIdByParent(taskId);

                // 删除任务
                for (Integer id : toDeleteIds) {
                    taskRepository.deleteTaskById(id);
                }
            }

            return ApiResponse.success("删除成功", null);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否为重复任务
     */
    public Map<String, Object> isRepeatTask(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        boolean isRepeat = task.getParentId() != null && !task.getParentId().equals(task.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("isRepeat", isRepeat);
        result.put("repeatGroupId", task.getParentId());

        if (isRepeat) {
            List<Task> repeatTasks = taskRepository.findByRepeatGroupId(task.getParentId());
            result.put("repeatCount", repeatTasks.size());
        }

        return result;
    }

    /**
     * 获取任务统计
     */
    public List<TaskStatisticsDTO> getStatistics(Integer userId, String period) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        LocalDateTime endTime;

        switch (period) {
            case "month":
                // 本月1号 到 下月1号
                startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                endTime = startTime.plusMonths(1);
                break;
            case "year":
                // 本年1月1号 到 明年1月1号
                startTime = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                endTime = startTime.plusYears(1);
                break;
            case "week":
            default:
                // 本周一 到 下周一
                int dayOfWeek = now.getDayOfWeek().getValue();
                startTime = now.minusDays(dayOfWeek - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                endTime = startTime.plusWeeks(1);
                break;
        }

        // 获取用户的任务ID
        List<Integer> taskIds = userTaskRepository.findByUserId(userId).stream()
                .map(UserTask::getTaskId)
                .collect(Collectors.toList());

        if (taskIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询统计数据
        List<Object[]> stats = taskRepository.getTaskStatisticsByType(taskIds, startTime, endTime);

        // 计算总时长
        long totalMinutes = stats.stream()
                .mapToLong(s -> s[1] != null ? ((Number) s[1]).longValue() : 0)
                .sum();

        // 构建结果
        return stats.stream()
                .map(s -> {
                    String type = (String) s[0];
                    long minutes = s[1] != null ? ((Number) s[1]).longValue() : 0;
                    double percentage = totalMinutes > 0 ?
                            Math.round(minutes * 1000.0 / totalMinutes) / 10.0 : 0;
                    return new TaskStatisticsDTO(type, minutes, percentage);
                })
                .sorted((a, b) -> Long.compare(b.getTotalMinutes(), a.getTotalMinutes()))
                .collect(Collectors.toList());
    }

    // ========== 私有辅助方法 ==========

    /**
     * 冲突检测
     */
    private ConflictInfo checkConflict(List<Integer> participantIds,
                                       LocalDateTime beginTime,
                                       LocalDateTime endTime,
                                       Integer excludeTaskId) {
        for (Integer participantId : participantIds) {
            List<Integer> userTaskIds = userTaskRepository.findByUserId(participantId).stream()
                    .map(UserTask::getTaskId)
                    .filter(id -> excludeTaskId == null || !id.equals(excludeTaskId))
                    .collect(Collectors.toList());

            if (userTaskIds.isEmpty()) continue;

            List<Task> conflicts = taskRepository.findConflictingTasks(userTaskIds, beginTime, endTime);

            if (!conflicts.isEmpty()) {
                Task conflictTask = conflicts.get(0);
                User user = userRepository.findById(participantId).orElse(null);

                ConflictInfo info = new ConflictInfo();
                info.setUserId(participantId);
                info.setUserName(user != null ? user.getName() : "未知用户");
                info.setConflictTaskTitle(conflictTask.getTitle());
                info.setConflictTimeRange(
                        conflictTask.getBeginTime().format(TIME_FORMATTER) +
                                "-" +
                                conflictTask.getEndTime().format(TIME_FORMATTER)
                );
                return info;
            }
        }
        return null;
    }

    /**
     * 计算复合任务进度
     */
    private int calculateProgress(List<Task> subTasks) {
        if (subTasks == null || subTasks.isEmpty()) return 0;

        long totalMinutes = 0;
        long completedMinutes = 0;

        for (Task subTask : subTasks) {
            long duration = java.time.Duration.between(
                    subTask.getBeginTime(),
                    subTask.getEndTime()
            ).toMinutes();

            totalMinutes += duration;
            if ("DONE".equals(subTask.getStatus())) {
                completedMinutes += duration;
            }
        }

        return totalMinutes > 0 ? (int) Math.round(completedMinutes * 100.0 / totalMinutes) : 0;
    }

    /**
     * 更新复合任务进度
     */
    private void updateCompositeTaskProgress(Integer parentTaskId) {
        List<Task> subTasks = taskRepository.findSubTasksByParentId(parentTaskId);
        int progress = calculateProgress(subTasks);

        Task parentTask = taskRepository.findById(parentTaskId).orElse(null);
        if (parentTask != null) {
            parentTask.setProgress(progress);

            // 更新复合任务状态
            if (progress == 100) {
                parentTask.setStatus("DONE");
            } else if (progress > 0) {
                parentTask.setStatus("DOING");
            }

            taskRepository.save(parentTask);
        }
    }

    /**
     * 构建任务Map（用于列表显示）
     */
    private Map<String, Object> buildTaskMap(Task task, Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", task.getId());
        map.put("title", task.getTitle());
        map.put("content", task.getContent());
        map.put("type", task.getType());
        map.put("typeColor", TYPE_COLORS.getOrDefault(task.getType(), "#C8C8C8"));
        map.put("status", task.getStatus());
        map.put("statusDesc", STATUS_DESC.getOrDefault(task.getStatus(), "未知"));
        map.put("beginTime", task.getBeginTime().format(DATETIME_FORMATTER));
        map.put("endTime", task.getEndTime().format(DATETIME_FORMATTER));
        map.put("progress", task.getProgress() != null ? task.getProgress() : 0);

        // 检查是否为复合事务
        List<Task> subTasks = taskRepository.findSubTasksByParentId(task.getId());
        map.put("isComposite", !subTasks.isEmpty());

        if (!subTasks.isEmpty()) {
            List<Map<String, Object>> subTaskMaps = subTasks.stream()
                    .map(st -> {
                        Map<String, Object> stMap = new HashMap<>();
                        stMap.put("id", st.getId());
                        stMap.put("title", st.getTitle());
                        stMap.put("beginTime", st.getBeginTime().format(DATETIME_FORMATTER));
                        stMap.put("endTime", st.getEndTime().format(DATETIME_FORMATTER));
                        stMap.put("status", st.getStatus());
                        return stMap;
                    })
                    .collect(Collectors.toList());
            map.put("subTasks", subTaskMaps);
        }

        // 获取参与人员
        List<UserTask> userTasks = userTaskRepository.findByTaskId(task.getId());
        List<Map<String, Object>> participants = userTasks.stream()
                .map(ut -> {
                    User user = userRepository.findById(ut.getUserId()).orElse(null);
                    if (user == null) return null;
                    Map<String, Object> pMap = new HashMap<>();
                    pMap.put("userId", user.getId());
                    pMap.put("name", user.getName());
                    pMap.put("role", user.getRole());
                    return pMap;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        map.put("participants", participants);

        return map;
    }
}
