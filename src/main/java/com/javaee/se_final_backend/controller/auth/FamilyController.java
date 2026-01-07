package com.javaee.se_final_backend.controller.auth;

import com.javaee.se_final_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
}
