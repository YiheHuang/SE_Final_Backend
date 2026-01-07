package com.javaee.se_final_backend.model.DTO;

import com.javaee.se_final_backend.model.entity.User;

public class FamilyRegisterResponse {
    private boolean success;
    private String message;
    private User admin;
    private Integer familyId;

    public FamilyRegisterResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public FamilyRegisterResponse(boolean success, String message, User admin, Integer familyId) {
        this.success = success;
        this.message = message;
        this.admin = admin;
        this.familyId = familyId;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public User getAdmin() { return admin; }
    public void setAdmin(User admin) { this.admin = admin; }

    public Integer getFamilyId() { return familyId; }
    public void setFamilyId(Integer familyId) { this.familyId = familyId; }
}
