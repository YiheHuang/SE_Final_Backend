package com.javaee.se_final_backend.controller.auth;

import com.javaee.se_final_backend.model.DTO.AddMemberRequest;
import com.javaee.se_final_backend.model.DTO.CreateFamilyRequest;
import com.javaee.se_final_backend.model.DTO.FamilyRegisterRequest;
import com.javaee.se_final_backend.model.DTO.FamilyRegisterResponse;
import com.javaee.se_final_backend.model.entity.User;
import com.javaee.se_final_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/user/login")
    public User login(@RequestBody User loginRequest) {
        return userService.login(loginRequest.getEmail(), loginRequest.getPassword());
    }

    @GetMapping("/user/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/user/family/members")
    public List<User> getFamilyMembers(@RequestParam Integer familyId) {
        return userService.getFamilyMembers(familyId);
    }
}
