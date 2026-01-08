package com.javaee.se_final_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer familyId;

    private String name;
    private String email;
    private String phone;
    private String role;
    private String password;
    // access control flags stored as integers (0 = denied, 1 = allowed)
    private Integer taskAccess;
    private Integer healthAccess;
    private Integer financeAccess;
}

