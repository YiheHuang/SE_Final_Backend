package com.javaee.se_final_backend.model.DTO;

import java.util.List;

public class FamilyRegisterRequest {
    private UserInfo admin;
    private List<UserInfo> members;

    public static class UserInfo {
        private String email;
        private String password;
        private String name;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Getters and setters
    public UserInfo getAdmin() { return admin; }
    public void setAdmin(UserInfo admin) { this.admin = admin; }

    public List<UserInfo> getMembers() { return members; }
    public void setMembers(List<UserInfo> members) { this.members = members; }
}
