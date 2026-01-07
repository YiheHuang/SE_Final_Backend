package com.javaee.se_final_backend.service;

import com.javaee.se_final_backend.model.entity.*;
import com.javaee.se_final_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class HealthStatisticsService {

    private final HealthRecordRepository recordRepository;
    private final UserTaskRepository userTaskRepository;
    private final TaskRepository taskRepository;
    private final ExaminationReportRepository reportRepository;
    private final HealthStatisticsRepository statisticsRepository;
    private final AIClientService aiClient;

    private static final String REPORT_DIR =
            "D:\\TJU\\SE\\FinalProject\\uploads\\HealthReports\\";

    /* 核心统计 */
    public Map<String, Object> statistics(
            Integer userId,
            LocalDateTime begin,
            LocalDateTime end) {

        Map<String, Object> res = new HashMap<>();

        /* 睡眠 */
        var sleeps = recordRepository
                .findByUserIdAndTypeAndDateBetween(
                        userId, "SLEEP", begin, end);

        double avgSleep = sleeps.stream()
                .mapToDouble(r -> Double.parseDouble(r.getContent()))
                .average().orElse(0);

        res.put("sleepAvg", avgSleep);

        /* 饮食 */
        var diets = recordRepository
                .findByUserIdAndTypeAndDateBetween(
                        userId, "DIET", begin, end);

        res.put("diets", diets.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("date", r.getDate().toLocalDate().toString());
            m.put("content", r.getContent());
            return m;
        }).toList());

        /* 运动 */
        long sportCount = userTaskRepository.findByUserId(userId)
                .stream()
                .map(ut -> taskRepository.findById(ut.getTaskId()).orElse(null))
                .filter(Objects::nonNull)
                .filter(t -> "SPORT".equals(t.getType()))
                .filter(t -> !t.getBeginTime().isBefore(begin)
                        && !t.getBeginTime().isAfter(end))
                .count();

        res.put("sportCount", sportCount);

        /* 体检对比 */
        var reports = reportRepository
                .findTop2ByUserIdOrderByTimeDesc(userId);

        if (!reports.isEmpty()) {
            Map<String, Object> cur = reports.get(0).getKeyIndex();
            Map<String, Object> pre =
                    reports.size() > 1 ? reports.get(1).getKeyIndex() : null;

            res.put("indexCompare", compare(cur, pre));
        }

        return res;
    }

    public Integer getLatestRating(Integer userId) {
        if (statisticsRepository.findFirstByUserIdOrderByIdDesc(userId).isEmpty()) {
            return 0;
        } else {
            return statisticsRepository
                    .findFirstByUserIdOrderByIdDesc(userId).get().getRating();
        }
    }

    public String generateAIReport(
            Integer userId,
            LocalDateTime begin,
            LocalDateTime end) {

        Map<String, Object> data = statistics(userId, begin, end);

        String prompt = buildPrompt(data, begin, end);

        String report = aiClient.chat(prompt);

        HealthStatistics stat = new HealthStatistics();
        stat.setUserId(userId);
        stat.setBeginDate(begin);
        stat.setEndDate(end);
        stat.setReport(report);
        stat.setRating(parseRating(report));

        statisticsRepository.save(stat);

        return report;
    }

    private String buildPrompt(Map<String, Object> data,
                               LocalDateTime begin,
                               LocalDateTime end) {

        return """
                你是一名专业的家庭健康管理医生，请根据以下健康数据进行综合评估。
                
                【统计区间】
                %s 至 %s
                
                【睡眠】
                平均睡眠时长：%.1f 小时
                
                【运动】
                运动次数：%d 次
                
                【饮食记录】
                %s
                
                【体检指标】
                %s
                
                请严格按照以下格式输出：
                
                【健康评分】
                rating: <0-100整数>
                
                【健康分析报告】
                1. 总体健康评价
                2. 睡眠分析
                3. 饮食分析
                4. 运动分析
                5. 风险提示
                6. 改善建议
                """.formatted(
                begin.toLocalDate(),
                end.toLocalDate(),
                (double) data.get("sleepAvg"),
                (long) data.get("sportCount"),
                formatDiets(data.get("diets")),
                formatIndexes(data.get("indexCompare"))
        );
    }

    private int parseRating(String text) {
        var m = java.util.regex.Pattern
                .compile("rating:\\s*(\\d{1,3})")
                .matcher(text);
        return m.find()
                ? Math.min(100, Integer.parseInt(m.group(1)))
                : 0;
    }

    private String formatDiets(Object diets) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) diets;
        if (list == null || list.isEmpty()) return "无记录";

        return list.stream()
                .map(d -> d.get("date") + "：" + d.get("content"))
                .reduce("", (a, b) -> a + "\n- " + b);
    }

    private String formatIndexes(Object idx) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) idx;
        if (list == null || list.isEmpty()) return "无体检数据";

        return list.stream()
                .map(i -> i.get("name") + "：" + i.get("current"))
                .reduce("", (a, b) -> a + "\n- " + b);
    }

    private List<Map<String, Object>> compare(
            Map<String, Object> cur,
            Map<String, Object> pre) {

        List<Map<String, Object>> list = new ArrayList<>();

        for (String k : cur.keySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", k);
            m.put("current", cur.get(k));
            m.put("previous", pre != null ? pre.getOrDefault(k, "—") : "—");

            try {
                if (pre != null && pre.containsKey(k)) {
                    double d1 = Double.parseDouble(cur.get(k).toString());
                    double d2 = Double.parseDouble(pre.get(k).toString());
                    m.put("delta", String.format("%+.2f", d1 - d2));
                } else {
                    m.put("delta", "—");
                }
            } catch (Exception e) {
                m.put("delta", "—");
            }

            list.add(m);
        }
        return list;
    }
}

