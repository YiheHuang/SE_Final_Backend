package com.javaee.se_final_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "UserTask")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserTask.UserTaskId.class)
public class UserTask {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Id
    @Column(name = "task_id")
    private Integer taskId;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserTaskId implements Serializable {
        private Integer userId;
        private Integer taskId;
    }
}