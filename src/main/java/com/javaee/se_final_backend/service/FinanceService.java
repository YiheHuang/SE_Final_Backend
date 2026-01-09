package com.javaee.se_final_backend.service;

import com.javaee.se_final_backend.model.DTO.BillCreateRequest;
import com.javaee.se_final_backend.model.DTO.BillItemDTO;
import com.javaee.se_final_backend.model.DTO.BillSummaryResponse;
import com.javaee.se_final_backend.model.entity.Bill;
import com.javaee.se_final_backend.model.entity.BillItem;
import com.javaee.se_final_backend.model.entity.User;
import com.javaee.se_final_backend.repository.BillItemRepository;
import com.javaee.se_final_backend.repository.BillRepository;
import com.javaee.se_final_backend.repository.UserRepository;
import com.javaee.se_final_backend.repository.BudgetRepository;
import com.javaee.se_final_backend.repository.BudgetItemRepository;
import com.javaee.se_final_backend.model.entity.Budget;
import com.javaee.se_final_backend.model.entity.BudgetItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final Logger log = LoggerFactory.getLogger(FinanceService.class);
    private static final java.util.List<String> PREDEFINED_CATEGORIES = java.util.List.of(
            "餐饮","出行","购物","娱乐","日用","医疗","教育","交通","工资","转账","其他"
    );

    @Transactional
    public Bill createBill(BillCreateRequest req) {
        List<BillItemDTO> items = req.getItems() == null ? Collections.emptyList() : req.getItems();

        LocalDateTime begin = null;
        LocalDateTime end = null;
        for (BillItemDTO it : items) {
            if (it.getTime() == null) continue;
            LocalDateTime parsed = parseTime(it.getTime());
            if (parsed == null) continue;
            if (begin == null || parsed.isBefore(begin)) begin = parsed;
            if (end == null || parsed.isAfter(end)) end = parsed;
        }

        Bill bill = new Bill();
        bill.setUserId(req.getUserId());
        bill.setType(req.getType());
        bill.setBeginDate(begin);
        bill.setEndDate(end);

        bill = billRepository.save(bill);

        for (BillItemDTO it : items) {
            BillItem bi = new BillItem();
            bi.setBillId(bill.getId());
            bi.setContent(it.getContent());
            String cat = it.getCategory();
            if (cat == null || !PREDEFINED_CATEGORIES.contains(cat)) cat = "其他";
            bi.setCategory(cat);
            bi.setPrice(it.getPrice());
            LocalDateTime parsed = null;
            if (it.getTime() != null) parsed = parseTime(it.getTime());
            bi.setTime(parsed);
            billItemRepository.save(bi);
        }

        return bill;
    }

    private LocalDateTime parseTime(String s) {
        if (s == null) return null;
        try {
            // try ISO first
            return LocalDateTime.parse(s);
        } catch (Exception ignored) {}
        try {
            // accept "yyyy-MM-dd HH:mm:ss" or "yyyy-MM-dd HH:mm"
            java.time.format.DateTimeFormatter f1 = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(s, f1);
        } catch (Exception ignored) {}
        try {
            java.time.format.DateTimeFormatter f2 = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return LocalDateTime.parse(s, f2);
        } catch (Exception ignored) {}
        try {
            // accept date-only like 2026-01-05
            java.time.format.DateTimeFormatter f3 = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
            java.time.LocalDate d = java.time.LocalDate.parse(s, f3);
            return d.atStartOfDay();
        } catch (Exception ignored) {}
        return null;
    }

    @Transactional
    public Bill updateBill(Integer billId, BillCreateRequest req) {
        Bill bill = billRepository.findById(billId).orElse(null);
        if (bill == null) return null;

        List<BillItemDTO> items = req.getItems() == null ? Collections.emptyList() : req.getItems();

        LocalDateTime begin = null;
        LocalDateTime end = null;
        for (BillItemDTO it : items) {
            if (it.getTime() == null) continue;
            LocalDateTime parsed = parseTime(it.getTime());
            if (parsed == null) continue;
            if (begin == null || parsed.isBefore(begin)) begin = parsed;
            if (end == null || parsed.isAfter(end)) end = parsed;
        }

        bill.setType(req.getType());
        bill.setBeginDate(begin);
        bill.setEndDate(end);
        bill = billRepository.save(bill);

        // replace items
        List<BillItem> exist = billItemRepository.findByBillId(bill.getId());
        if (!exist.isEmpty()) {
            billItemRepository.deleteAll(exist);
        }

        for (BillItemDTO it : items) {
            BillItem bi = new BillItem();
            bi.setBillId(bill.getId());
            bi.setContent(it.getContent());
            bi.setCategory(it.getCategory());
            bi.setPrice(it.getPrice());
            LocalDateTime parsed = it.getTime() == null ? null : parseTime(it.getTime());
            bi.setTime(parsed);
            billItemRepository.save(bi);
        }

        return bill;
    }

    public Map<String, Object> getBillDetail(Integer billId) {
        Bill bill = billRepository.findById(billId).orElse(null);
        if (bill == null) return Map.of("error", "not_found");
        List<BillItem> items = billItemRepository.findByBillId(billId);
        return Map.of("bill", bill, "items", items);
    }

    @Transactional
    public boolean deleteBill(Integer billId) {
        if (!billRepository.existsById(billId)) return false;
        List<BillItem> items = billItemRepository.findByBillId(billId);
        billItemRepository.deleteAll(items);
        billRepository.deleteById(billId);
        return true;
    }

    public List<Map<String, Object>> listBills(Integer userId, LocalDateTime begin, LocalDateTime end, String scope) {
        List<Integer> userIds = resolveUserIds(userId, scope);

        List<Bill> bills;
        if (begin != null && end != null) {
            bills = billRepository.findByUserIdInAndBeginDateBetween(userIds, begin, end);
        } else {
            bills = billRepository.findByUserIdIn(userIds);
        }

        return bills.stream().map(b -> {
            List<BillItem> items = billItemRepository.findByBillId(b.getId());
            Map<String, Object> m = new HashMap<>();
            m.put("bill", b);
            m.put("items", items);
            return m;
        }).collect(Collectors.toList());
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public BillSummaryResponse summary(Integer userId, LocalDateTime begin, LocalDateTime end, String scope) {
        List<Integer> userIds = resolveUserIds(userId, scope);

        List<Bill> bills;
        if (begin != null && end != null) {
            bills = billRepository.findByUserIdInAndBeginDateBetween(userIds, begin, end);
        } else {
            bills = billRepository.findByUserIdIn(userIds);
        }

        BigDecimal totalIn = BigDecimal.ZERO;
        BigDecimal totalOut = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryMap = new HashMap<>();
        Map<String, Map<String, BigDecimal>> dailyMap = new TreeMap<>();

        for (Bill b : bills) {
            List<BillItem> items = billItemRepository.findByBillId(b.getId());
            for (BillItem it : items) {
                BigDecimal price = it.getPrice() == null ? BigDecimal.ZERO : it.getPrice();
                String category = it.getCategory() == null ? "其他" : it.getCategory();
                categoryMap.put(category, categoryMap.getOrDefault(category, BigDecimal.ZERO).add(price));

                String day = it.getTime() != null ? it.getTime().toLocalDate().toString() : (b.getBeginDate() != null ? b.getBeginDate().toLocalDate().toString() : "unknown");
                Map<String, BigDecimal> dayEntry = dailyMap.computeIfAbsent(day, k -> {
                    Map<String, BigDecimal> m = new HashMap<>();
                    m.put("in", BigDecimal.ZERO);
                    m.put("out", BigDecimal.ZERO);
                    return m;
                });

                if ("收入".equalsIgnoreCase(b.getType())) {
                    totalIn = totalIn.add(price);
                    dayEntry.put("in", dayEntry.get("in").add(price));
                } else {
                    totalOut = totalOut.add(price);
                    dayEntry.put("out", dayEntry.get("out").add(price));
                }
            }
        }

        BillSummaryResponse resp = new BillSummaryResponse();
        resp.setPeriod(begin != null && end != null ? begin.toLocalDate() + " ~ " + end.toLocalDate() : "all");
        resp.setTotalIn(totalIn);
        resp.setTotalOut(totalOut);
        resp.setCategoryBreakdown(categoryMap);

        List<Map<String, Object>> dailyTrend = dailyMap.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("date", e.getKey());
            m.put("in", e.getValue().getOrDefault("in", BigDecimal.ZERO));
            m.put("out", e.getValue().getOrDefault("out", BigDecimal.ZERO));
            return m;
        }).collect(Collectors.toList());
        resp.setDailyTrend(dailyTrend);

        return resp;
    }

    public java.util.List<String> getCategoryList() {
        return PREDEFINED_CATEGORIES;
    }

    // JSON-array import via API removed; file-only upload supported via importOrdersFromFile.

    @Transactional
    public int importOrdersFromFile(org.springframework.web.multipart.MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return 0;
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        byte[] bytes = file.getBytes();
        String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8).trim();

        // if looks like JSON array
        if (text.startsWith("[")) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> orders = mapper.readValue(text, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>(){});
            return importOrders(orders);
        }

        // otherwise treat as CSV (header expected)
        // simple CSV parser: split lines, first line header
        String[] lines = text.split("\\r?\\n");
        if (lines.length < 1) return 0;
        String header = lines[0];
        String[] cols = header.split(",");
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < cols.length; i++) idx.put(cols[i].trim().toLowerCase(), i);

        List<Map<String, Object>> orders = new ArrayList<>();
        for (int r = 1; r < lines.length; r++) {
            String line = lines[r].trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            Map<String, Object> map = new HashMap<>();
            try {
                if (idx.containsKey("userid")) map.put("userId", Integer.parseInt(parts[idx.get("userid")].trim()));
                if (idx.containsKey("type")) map.put("type", parts[idx.get("type")].trim());
                if (idx.containsKey("content")) map.put("content", parts[idx.get("content")].trim());
                if (idx.containsKey("category")) map.put("category", parts[idx.get("category")].trim());
                if (idx.containsKey("price")) map.put("price", new java.math.BigDecimal(parts[idx.get("price")].trim()));
                if (idx.containsKey("time")) map.put("time", parts[idx.get("time")].trim());
                orders.add(map);
            } catch (Exception ignored) {}
        }
        return importOrders(orders);
    }

    // helper to import a list of order maps (from JSON or CSV parsing)
    @Transactional
    private int importOrders(List<Map<String, Object>> orders) {
        if (orders == null || orders.isEmpty()) return 0;
        int created = 0;
        for (Map<String, Object> o : orders) {
            try {
                BillCreateRequest req = new BillCreateRequest();
                // userId may be Integer or String
                Integer uid = null;
                if (o.containsKey("userId") && o.get("userId") != null) {
                    Object v = o.get("userId");
                    if (v instanceof Number) uid = ((Number) v).intValue();
                    else {
                        try { uid = Integer.parseInt(v.toString()); } catch (Exception ignored) {}
                    }
                }
                // default to system user if missing (use first admin user or 1)
                if (uid == null) uid = 1;
                req.setUserId(uid);

                String type = o.getOrDefault("type", "支出").toString();
                req.setType(type);

                BillItemDTO it = new BillItemDTO();
                it.setContent(o.getOrDefault("content", "").toString());
                it.setCategory(o.getOrDefault("category", "其他").toString());
                Object priceObj = o.get("price");
                try {
                    java.math.BigDecimal p = priceObj == null ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(priceObj.toString());
                    it.setPrice(p);
                } catch (Exception ex) {
                    it.setPrice(java.math.BigDecimal.ZERO);
                }
                it.setTime(o.getOrDefault("time", "").toString());

                List<BillItemDTO> items = new ArrayList<>();
                items.add(it);
                req.setItems(items);

                createBill(req);
                created++;
            } catch (Exception ex) {
                // ignore individual row errors and continue
            }
        }
        return created;
    }

    @Transactional
    public Map<String, Object> seedSampleData(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return Map.of("ok", false, "error", "user_not_found");

        Integer userId = user.getId();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // create 3 sample bills with items in last 10 days
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 3; i++) {
            Bill b = new Bill();
            b.setUserId(userId);
            b.setType(i == 0 ? "收入" : "支出");
            b.setBeginDate(now.minusDays(10 - i));
            b.setEndDate(now.minusDays(10 - i));
            b = billRepository.save(b);

            int itemsCount = 1 + rnd.nextInt(3);
            for (int j = 0; j < itemsCount; j++) {
                BillItem bi = new BillItem();
                bi.setBillId(b.getId());
                String cat = PREDEFINED_CATEGORIES.get(rnd.nextInt(PREDEFINED_CATEGORIES.size()));
                bi.setCategory(cat);
                bi.setContent(cat + " 花费");
                bi.setPrice(new java.math.BigDecimal(10 + rnd.nextInt(200)));
                bi.setTime(now.minusDays(10 - i).plusHours(rnd.nextInt(12)));
                billItemRepository.save(bi);
            }
        }

        // create budget for current month
        java.time.YearMonth ym = java.time.YearMonth.now();
        LocalDateTime bdate = ym.atDay(1).atStartOfDay();
        LocalDateTime edate = ym.atEndOfMonth().atTime(23,59,59);

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setBeginDate(bdate);
        budget.setEndDate(edate);
        budget.setType("MONTHLY");
        budget = budgetRepository.save(budget);

        // create some budget items
        for (int i = 0; i < 4; i++) {
            BudgetItem bi = new BudgetItem();
            bi.setBudgetId(budget.getId());
            bi.setCategory(PREDEFINED_CATEGORIES.get(i));
            bi.setTotal(new java.math.BigDecimal(100 + i * 50));
            budgetItemRepository.save(bi);
        }

        return Map.of("ok", true, "userId", userId);
    }

    private List<Integer> resolveUserIds(Integer userId, String scope) {
        if ("family".equalsIgnoreCase(scope)) {
            User self = userRepository.findById(userId).orElse(null);
            if (self == null || self.getFamilyId() == null) return List.of(userId);
            List<User> users = userRepository.findByFamilyId(self.getFamilyId());
            return users.stream().map(User::getId).collect(Collectors.toList());
        } else {
            return List.of(userId);
        }
    }
    
    /* Budget operations */
    @Transactional
    public Map<String, Object> getBudgetForMonth(Integer userId, String month, String scope) {
        try {
            java.time.YearMonth ym = java.time.YearMonth.parse(month);
            LocalDateTime b = ym.atDay(1).atStartOfDay();
            LocalDateTime e = ym.atEndOfMonth().atTime(23,59,59);

            List<Budget> budgets = budgetRepository.findByUserIdAndBeginDateBetween(userId, b, e);
            Budget budget;
            if (budgets.isEmpty()) {
                // no budget set
                budget = null;
            } else {
                budget = budgets.get(0);
            }

            // compute spent by category for the user (family scope)
            List<Integer> userIds = resolveUserIds(userId, scope);
            List<Bill> bills = billRepository.findByUserIdInAndBeginDateBetween(userIds, b, e);
            Map<String, java.math.BigDecimal> spentByCat = new HashMap<>();
            java.math.BigDecimal totalSpent = java.math.BigDecimal.ZERO;
            for (Bill bill : bills) {
                List<BillItem> items = billItemRepository.findByBillId(bill.getId());
                for (BillItem it : items) {
                    java.math.BigDecimal price = it.getPrice() == null ? java.math.BigDecimal.ZERO : it.getPrice();
                    String cat = it.getCategory() == null ? "其他" : it.getCategory();
                    // Only count expenses toward "spent". Income should not increase spent.
                    if (!"收入".equalsIgnoreCase(bill.getType())) {
                        spentByCat.put(cat, spentByCat.getOrDefault(cat, java.math.BigDecimal.ZERO).add(price));
                        totalSpent = totalSpent.add(price);
                    }
                }
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("month", month);
            resp.put("budget", budget == null ? java.math.BigDecimal.ZERO : sumBudgetItems(budget.getId()));
            resp.put("spent", totalSpent);
            // For simplified model we only return total budget (stored as BudgetItems sum)
            resp.put("categories", new ArrayList<Map<String, Object>>());
            return resp;
        } catch (Exception ex) {
            return Map.of("error", "invalid_month");
        }
    }

    private java.math.BigDecimal sumBudgetItems(Integer budgetId) {
        List<BudgetItem> items = budgetItemRepository.findByBudgetId(budgetId);
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (BudgetItem bi : items) {
            if (bi.getTotal() != null) total = total.add(bi.getTotal());
        }
        return total;
    }

    @Transactional
    public Map<String, Object> saveBudget(java.util.Map<String, Object> body) {
        try {
            // robustly parse userId (could be Integer, Long, Double or String)
            Integer userId = null;
            Object userIdObj = body.get("userId");
            if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).intValue();
            } else if (userIdObj instanceof String) {
                try { userId = Integer.parseInt((String) userIdObj); } catch (Exception ignored) {}
            }
            if (userId == null) {
                return Map.of("ok", false, "error", "userId_required");
            }
            String month = (String) body.get("month");
            java.math.BigDecimal budgetTotal = body.get("budget") == null ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(body.get("budget").toString());

            String scope = body.get("scope") == null ? "self" : body.get("scope").toString();
            java.time.YearMonth ym = java.time.YearMonth.parse(month);
            LocalDateTime b = ym.atDay(1).atStartOfDay();
            LocalDateTime e = ym.atEndOfMonth().atTime(23,59,59);

            List<Budget> existing = budgetRepository.findByUserIdAndBeginDateBetween(userId, b, e);
            Budget budget;
            if (existing.isEmpty()) {
                budget = new Budget();
                budget.setUserId(userId);
                budget.setBeginDate(b);
                budget.setEndDate(e);
                budget.setType("MONTHLY");
                budget = budgetRepository.save(budget);
            } else {
                budget = existing.get(0);
                // clear existing items
                budgetItemRepository.deleteByBudgetId(budget.getId());
            }

            // Store the total budget as a single BudgetItem named "总预算"
            BudgetItem bi = new BudgetItem();
            bi.setBudgetId(budget.getId());
            bi.setCategory("总预算");
            bi.setTotal(budgetTotal);
            bi = budgetItemRepository.save(bi);
            // ensure persisted and read back authoritative total from DB
            java.math.BigDecimal storedTotal = sumBudgetItems(budget.getId());
            log.info("saveBudget: userId={}, month={}, scope={}, requestedBudget={}, storedTotal={}", userId, month, scope, budgetTotal, storedTotal);
            // compute spent according to scope
            List<Integer> userIds = resolveUserIds(userId, scope);
            List<Bill> bills = billRepository.findByUserIdInAndBeginDateBetween(userIds, b, e);
            java.math.BigDecimal totalSpent = java.math.BigDecimal.ZERO;
            for (Bill bill : bills) {
                List<BillItem> items = billItemRepository.findByBillId(bill.getId());
                for (BillItem it : items) {
                    java.math.BigDecimal price = it.getPrice() == null ? java.math.BigDecimal.ZERO : it.getPrice();
                    if (!"收入".equalsIgnoreCase(bill.getType())) totalSpent = totalSpent.add(price);
                }
            }

            Map<String, Object> out = Map.of("ok", true, "budgetId", budget.getId(), "budget", storedTotal, "spent", totalSpent);
            log.info("saveBudget result: {}", out);
            return out;
        } catch (Exception ex) {
            return Map.of("ok", false, "error", ex.getMessage());
        }
    }
    
    /* Category aggregation */
    public Map<String, java.math.BigDecimal> categories(Integer userId, LocalDateTime begin, LocalDateTime end, String scope) {
        List<Integer> userIds = resolveUserIds(userId, scope);
        List<Bill> bills;
        if (begin != null && end != null) {
            bills = billRepository.findByUserIdInAndBeginDateBetween(userIds, begin, end);
        } else {
            bills = billRepository.findByUserIdIn(userIds);
        }

        Map<String, java.math.BigDecimal> categoryMap = new HashMap<>();
        for (Bill b : bills) {
            List<BillItem> items = billItemRepository.findByBillId(b.getId());
            for (BillItem it : items) {
                java.math.BigDecimal price = it.getPrice() == null ? java.math.BigDecimal.ZERO : it.getPrice();
                String category = it.getCategory() == null ? "其他" : it.getCategory();
                categoryMap.put(category, categoryMap.getOrDefault(category, java.math.BigDecimal.ZERO).add(price));
            }
        }
        return categoryMap;
    }

    /* Time series trend */
    public List<Map<String, Object>> trend(Integer userId, LocalDateTime begin, LocalDateTime end, String scope, String interval) {
        List<Integer> userIds = resolveUserIds(userId, scope);
        List<Bill> bills;
        if (begin != null && end != null) {
            bills = billRepository.findByUserIdInAndBeginDateBetween(userIds, begin, end);
        } else {
            bills = billRepository.findByUserIdIn(userIds);
        }

        // group key function: daily / monthly (weekly could be added)
        java.time.format.DateTimeFormatter dailyFmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        java.time.format.DateTimeFormatter monthlyFmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, Map<String, java.math.BigDecimal>> map = new TreeMap<>();

        for (Bill b : bills) {
            List<BillItem> items = billItemRepository.findByBillId(b.getId());
            for (BillItem it : items) {
                java.time.LocalDateTime t = it.getTime() != null ? it.getTime() : b.getBeginDate();
                if (t == null) continue;
                String key = "daily".equalsIgnoreCase(interval) ? t.toLocalDate().format(dailyFmt) : t.toLocalDate().format(monthlyFmt);
                Map<String, java.math.BigDecimal> entry = map.computeIfAbsent(key, k -> {
                    Map<String, java.math.BigDecimal> m = new HashMap<>();
                    m.put("in", java.math.BigDecimal.ZERO);
                    m.put("out", java.math.BigDecimal.ZERO);
                    return m;
                });
                java.math.BigDecimal price = it.getPrice() == null ? java.math.BigDecimal.ZERO : it.getPrice();
                if ("收入".equalsIgnoreCase(b.getType())) {
                    entry.put("in", entry.get("in").add(price));
                } else {
                    entry.put("out", entry.get("out").add(price));
                }
            }
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (var e : map.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("period", e.getKey());
            m.put("in", e.getValue().getOrDefault("in", java.math.BigDecimal.ZERO));
            m.put("out", e.getValue().getOrDefault("out", java.math.BigDecimal.ZERO));
            list.add(m);
        }
        return list;
    }
}

