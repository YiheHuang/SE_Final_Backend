package com.javaee.se_final_backend.controller.finance;

import com.javaee.se_final_backend.model.DTO.BillCreateRequest;
import com.javaee.se_final_backend.model.DTO.BillSummaryResponse;
import com.javaee.se_final_backend.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @PostMapping("/bills")
    public Map<String, Object> createBill(@RequestBody BillCreateRequest req) {
        try {
            if (req.getItems() == null || req.getItems().isEmpty()) {
                return Map.of("ok", false, "error", "items_required");
            }
            var created = financeService.createBill(req);
            return Map.of("ok", true, "id", created.getId());
        } catch (Exception ex) {
            return Map.of("ok", false, "error", ex.getMessage());
        }
    }

    @GetMapping("/category-list")
    public Map<String, Object> categoryList() {
        return Map.of("categories", financeService.getCategoryList());
    }

    @PostMapping("/seed")
    public Map<String, Object> seed(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null) return Map.of("ok", false, "error", "email_required");
        return financeService.seedSampleData(email);
    }

    @GetMapping("/bills")
    public List<Map<String, Object>> listBills(@RequestParam Integer userId,
                                               @RequestParam(required = false) String begin,
                                               @RequestParam(required = false) String end,
                                               @RequestParam(defaultValue = "self") String scope) {
        LocalDateTime b = null, e = null;
        try {
            if (begin != null) b = LocalDateTime.parse(begin);
            if (end != null) e = LocalDateTime.parse(end);
        } catch (DateTimeParseException ignored) {}
        return financeService.listBills(userId, b, e, scope);
    }

    @GetMapping("/bills/{id}")
    public Map<String, Object> billDetail(@PathVariable Integer id) {
        return financeService.getBillDetail(id);
    }

    @DeleteMapping("/bills/{id}")
    public Map<String, Object> deleteBill(@PathVariable Integer id) {
        boolean ok = financeService.deleteBill(id);
        return Map.of("ok", ok);
    }

    @PutMapping("/bills/{id}")
    public Map<String, Object> updateBill(@PathVariable Integer id, @RequestBody BillCreateRequest req) {
        var b = financeService.updateBill(id, req);
        if (b == null) return Map.of("ok", false, "error", "not_found");
        return Map.of("ok", true, "id", b.getId());
    }

    @GetMapping("/summary")
    public BillSummaryResponse summary(@RequestParam Integer userId,
                                       @RequestParam(required = false) String begin,
                                       @RequestParam(required = false) String end,
                                       @RequestParam(defaultValue = "self") String scope) {
        LocalDateTime b = null, e = null;
        try {
            if (begin != null) b = LocalDateTime.parse(begin);
            if (end != null) e = LocalDateTime.parse(end);
        } catch (DateTimeParseException ignored) {}
        return financeService.summary(userId, b, e, scope);
    }

    @GetMapping("/budgets")
    public Map<String, Object> getBudget(@RequestParam Integer userId,
                                         @RequestParam String month) {
        return financeService.getBudgetForMonth(userId, month);
    }

    @PostMapping("/budgets")
    public Map<String, Object> saveBudget(@RequestBody Map<String, Object> body) {
        return financeService.saveBudget(body);
    }

    @GetMapping("/categories")
    public Map<String, Object> categories(@RequestParam Integer userId,
                                          @RequestParam(required = false) String begin,
                                          @RequestParam(required = false) String end,
                                          @RequestParam(defaultValue = "self") String scope) {
        LocalDateTime b = null, e = null;
        try {
            if (begin != null) b = LocalDateTime.parse(begin);
            if (end != null) e = LocalDateTime.parse(end);
        } catch (DateTimeParseException ignored) {}
        Map<String, Object> out = new HashMap<>();
        out.put("categories", financeService.categories(userId, b, e, scope));
        return out;
    }

    @GetMapping("/trend")
    public Map<String, Object> trend(@RequestParam Integer userId,
                                     @RequestParam(required = false) String begin,
                                     @RequestParam(required = false) String end,
                                     @RequestParam(defaultValue = "self") String scope,
                                     @RequestParam(defaultValue = "daily") String interval) {
        LocalDateTime b = null, e = null;
        try {
            if (begin != null) b = LocalDateTime.parse(begin);
            if (end != null) e = LocalDateTime.parse(end);
        } catch (DateTimeParseException ignored) {}
        Map<String, Object> out = new HashMap<>();
        out.put("trend", financeService.trend(userId, b, e, scope, interval));
        return out;
    }
}


