package com.javaee.se_final_backend.model.DTO;

public class CreateFamilyRequest {
    private Integer adminId;
    private String familyName;

    // Getters and setters
    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }
}
