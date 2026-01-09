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
import java.util.Optional;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;
    private final Logger log = LoggerFactory.getLogger(FinanceController.class);

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

    @GetMapping("/bills/{id:\\d+}")
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
                                         @RequestParam String month,
                                         @RequestParam(defaultValue = "self") String scope) {
        return financeService.getBudgetForMonth(userId, month, scope);
    }

    @PostMapping("/budgets")
    public Map<String, Object> saveBudget(@RequestBody Map<String, Object> body) {
        log.info("saveBudget request body: {}", body);
        Map<String, Object> out = financeService.saveBudget(body);
        log.info("saveBudget response: {}", out);
        return out;
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

    @PostMapping("/import/upload")
    public Map<String, Object> uploadImport(@RequestParam("file") org.springframework.web.multipart.MultipartFile file, @RequestParam("uid") Integer uid) {
        log.info("uploadImport called, file={}", file == null ? null : file.getOriginalFilename());
        try {
            int created = financeService.importOrdersFromFile(file, uid);
            log.info("import completed, created={}", created);
            return Map.of("ok", true, "imported", created);
        } catch (Exception ex) {
            log.error("import failed", ex);
            return Map.of("ok", false, "error", ex.getMessage());
        }
    }

    @GetMapping("/bills/export")
    public void exportBills(@RequestParam Integer userId,
                            @RequestParam(required = false) String begin,
                            @RequestParam(required = false) String end,
                            @RequestParam(defaultValue = "self") String scope,
                            HttpServletResponse response) {
        LocalDateTime b = null, e = null;
        try {
            if (begin != null) b = LocalDateTime.parse(begin);
            if (end != null) e = LocalDateTime.parse(end);
        } catch (DateTimeParseException ignored) {}

        List<Map<String, Object>> bills = financeService.listBills(userId, b, e, scope);
        var user = financeService.getUserById(userId);
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
            String filename = "bills_" + (user != null ? user.getName() : userId) + ".xlsx";
            String encoded = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            OutputStream out = response.getOutputStream();

            try (XSSFWorkbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("账单导出");
                int rowIdx = 0;
                Row r0 = sheet.createRow(rowIdx++);
                r0.createCell(0).setCellValue("用户姓名");
                r0.createCell(1).setCellValue(user != null ? user.getName() : "");
                Row r1 = sheet.createRow(rowIdx++);
                r1.createCell(0).setCellValue("用户邮箱");
                r1.createCell(1).setCellValue(user != null ? user.getEmail() : "");
                Row r2 = sheet.createRow(rowIdx++);
                r2.createCell(0).setCellValue("用户角色");
                r2.createCell(1).setCellValue(user != null ? user.getRole() : "");
                rowIdx++; // blank row

                Row header = sheet.createRow(rowIdx++);
                header.createCell(0).setCellValue("账单ID");
                header.createCell(1).setCellValue("条目ID");
                header.createCell(2).setCellValue("类型");
                header.createCell(3).setCellValue("分类");
                header.createCell(4).setCellValue("金额");
                header.createCell(5).setCellValue("时间");
                header.createCell(6).setCellValue("内容");

                for (Map<String, Object> entry : bills) {
                    Object billObj = entry.get("bill");
                    Object itemsObj = entry.get("items");
                    Integer billId = null;
                    String type = "";
                    if (billObj instanceof com.javaee.se_final_backend.model.entity.Bill) {
                        var bObj = (com.javaee.se_final_backend.model.entity.Bill) billObj;
                        billId = bObj.getId();
                        type = bObj.getType();
                    }
                    if (itemsObj instanceof List) {
                        List<?> items = (List<?>) itemsObj;
                        for (Object itObj : items) {
                            if (itObj instanceof com.javaee.se_final_backend.model.entity.BillItem) {
                                var it = (com.javaee.se_final_backend.model.entity.BillItem) itObj;
                                Row rr = sheet.createRow(rowIdx++);
                                rr.createCell(0).setCellValue(billId != null ? billId : 0);
                                rr.createCell(1).setCellValue(it.getId() != null ? it.getId() : 0);
                                rr.createCell(2).setCellValue(type != null ? type : "");
                                rr.createCell(3).setCellValue(it.getCategory() != null ? it.getCategory() : "");
                                rr.createCell(4).setCellValue(it.getPrice() != null ? it.getPrice().toString() : "0");
                                rr.createCell(5).setCellValue(it.getTime() != null ? it.getTime().toString() : "");
                                rr.createCell(6).setCellValue(it.getContent() != null ? it.getContent() : "");
                            }
                        }
                    }
                }

                // autosize columns
                for (int c = 0; c <= 6; c++) sheet.autoSizeColumn(c);

                wb.write(out);
                out.flush();
            }
        } catch (Exception ex) {
            log.error("export failed", ex);
            try { response.sendError(500, "export_failed"); } catch (Exception ignored) {}
        }
    }

    private String sanitize(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }
}


