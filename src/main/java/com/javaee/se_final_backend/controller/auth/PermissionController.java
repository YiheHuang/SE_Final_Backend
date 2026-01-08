package com.javaee.se_final_backend.controller.auth;

import com.javaee.se_final_backend.model.entity.User;
import com.javaee.se_final_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin
@RequiredArgsConstructor
public class PermissionController {

    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(PermissionController.class);

    @GetMapping
    public Map<String, Object> getPermissions(@RequestParam Integer userId) {
        User u = userRepository.findById(userId).orElse(null);
        if (u == null) return Map.of();
        return Map.of(
                "tasks", u.getTaskAccess() == null ? true : (u.getTaskAccess() != 0),
                "health", u.getHealthAccess() == null ? true : (u.getHealthAccess() != 0),
                "finance", u.getFinanceAccess() == null ? true : (u.getFinanceAccess() != 0)
        );
    }

    @PostMapping
    public Map<String, Object> setPermissions(@RequestBody Map<String, Object> req) {
        log.info("setPermissions payload: {}", req);
        Integer adminId = null;
        Integer targetUserId = null;
        try {
            Object a = req.get("adminId");
            if (a != null && a instanceof Number) adminId = ((Number) a).intValue();
            Object t = req.get("userId");
            if (t != null && t instanceof Number) targetUserId = ((Number) t).intValue();
        } catch (Exception ignored) {}

        Integer taskAccess = null;
        Integer healthAccess = null;
        Integer financeAccess = null;
        try {
            Object ta = req.get("task_access");
            if (ta == null) ta = req.get("taskAccess");
            if (ta != null && ta instanceof Number) taskAccess = ((Number) ta).intValue();
            Object ha = req.get("health_access");
            if (ha == null) ha = req.get("healthAccess");
            if (ha != null && ha instanceof Number) healthAccess = ((Number) ha).intValue();
            Object fa = req.get("finance_access");
            if (fa == null) fa = req.get("financeAccess");
            if (fa != null && fa instanceof Number) financeAccess = ((Number) fa).intValue();
        } catch (Exception ignored) {}

        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || !"admin".equals(admin.getRole())) {
            return Map.of("ok", false, "error", "no_permission");
        }

        User target = userRepository.findById(targetUserId).orElse(null);
        if (target == null || admin.getFamilyId() == null || !admin.getFamilyId().equals(target.getFamilyId())) {
            return Map.of("ok", false, "error", "invalid_target");
        }

        if (taskAccess != null) target.setTaskAccess(taskAccess);
        if (healthAccess != null) target.setHealthAccess(healthAccess);
        if (financeAccess != null) target.setFinanceAccess(financeAccess);

        userRepository.save(target);
        return Map.of("ok", true);
    }
}


