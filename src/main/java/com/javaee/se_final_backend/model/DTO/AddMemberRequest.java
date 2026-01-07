package com.javaee.se_final_backend.model.DTO;

public class AddMemberRequest {
    private Integer adminId;
    private FamilyRegisterRequest.UserInfo memberInfo;

    // Getters and setters
    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public FamilyRegisterRequest.UserInfo getMemberInfo() { return memberInfo; }
    public void setMemberInfo(FamilyRegisterRequest.UserInfo memberInfo) { this.memberInfo = memberInfo; }
}
