package com.javaee.se_final_backend.controller.auth;

import com.javaee.se_final_backend.model.DTO.AddMemberRequest;
import com.javaee.se_final_backend.model.DTO.CreateFamilyRequest;
import com.javaee.se_final_backend.model.DTO.FamilyRegisterRequest;
import com.javaee.se_final_backend.model.DTO.FamilyRegisterResponse;
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

    @PostMapping("/register")
    public FamilyRegisterResponse registerFamily(@RequestBody FamilyRegisterRequest request) {
        return userService.registerFamily(request);
    }

    @PostMapping("/create")
    public FamilyRegisterResponse createFamily(@RequestBody CreateFamilyRequest request) {
        return userService.createFamily(request.getAdminId(), request.getFamilyName());
    }

    @PostMapping("/add-member")
    public FamilyRegisterResponse addFamilyMember(@RequestBody AddMemberRequest request) {
        return userService.addFamilyMember(request.getAdminId(), request.getMemberInfo());
    }
}
