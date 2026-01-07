package com.javaee.se_final_backend.model.DTO;

import lombok.Data;
import java.util.List;

@Data
public class TaskDetailResponse {
    private Integer id;
    private String title;
    private String content;
    private String type;
    private String typeColor;
    private String status;
    private String statusDesc;
    private String beginTime;
    private String endTime;
    private Boolean isComposite;
    private Integer progress;
    private Integer parentId;
    private List<ParticipantInfo> participants;
    private List<SubTaskInfo> subTasks;
    private Boolean isRepeat;           // 是否为重复任务
    private Integer repeatGroupId;      // 重复组ID（用parentId表示）

    @Data
    public static class ParticipantInfo {
        private Integer userId;
        private String name;
        private String role;
    }

    @Data
    public static class SubTaskInfo {
        private Integer id;
        private String title;
        private String beginTime;
        private String endTime;
        private String status;
        private String statusDesc;
    }
}
