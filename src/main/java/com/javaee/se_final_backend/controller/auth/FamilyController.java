package com.javaee.se_final_backend.controller.auth;

import com.javaee.se_final_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.javaee.se_final_backend.model.entity.User;

@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
@CrossOrigin
public class FamilyController {

    private final UserService userService;

    @GetMapping("/getmembers")
    public List<Map<String, Object>> members(
            @RequestParam Integer userId
    ) {
        return userService.members(userId);
    }

    // Backwards-compatible endpoint: /api/family/members?familyId=...
    @GetMapping("/members")
    public List<User> membersByFamily(
            @RequestParam Integer familyId
    ) {
        return userService.getFamilyMembers(familyId);
    }
}
