package com.javaee.se_final_backend.model.DTO;

import java.util.Map;

public class PermissionRequest {
    private Integer adminId;
    private Integer userId;
    private Map<String, Boolean> permissions;

    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Map<String, Boolean> getPermissions() { return permissions; }
    public void setPermissions(Map<String, Boolean> permissions) { this.permissions = permissions; }
}


