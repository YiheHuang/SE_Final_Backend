package com.javaee.se_final_backend.model.DTO;

import lombok.Data;
import java.util.List;

@Data
public class TaskCreateRequest {
    private String title;           // 标题（必填）
    private String content;         // 描述（选填）
    private String type;            // 类型：study, sport, work, entertainment, meeting, life, other
    private String beginTime;       // 开始时间 yyyy-MM-ddTHH:mm:ss
    private String endTime;         // 结束时间
    private List<Integer> participantIds;  // 参与人员ID列表
    private Boolean isComposite;    // 是否复合事务
    private List<SubTaskRequest> subTasks; // 子任务列表（复合事务时使用）
    private Boolean isRepeat;       // 是否重复
    private Integer repeatWeeks;    // 重复周数（1-50）
    private String status;          // 状态：TODO, DOING, DONE

    @Data
    public static class SubTaskRequest {
        private Integer id;         // 子任务ID（编辑时使用）
        private String title;
        private String beginTime;
        private String endTime;
        private String status;
    }
}
