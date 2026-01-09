package com.javaee.se_final_backend.service;

import com.javaee.se_final_backend.model.entity.ExaminationReport;
import com.javaee.se_final_backend.repository.ExaminationReportRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;


import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExaminationReportService {

    private final ExaminationReportRepository repository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public void upload(Integer userId, MultipartFile file) throws Exception {

        if (uploadDir == null) {
            throw new IllegalStateException("file.upload-dir 未配置");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File dest = new File(dir, filename); // ✅ 正确拼路径

        file.transferTo(dest);

        Map<String, Object> keyIndex = extractKeyIndex(dest);

        ExaminationReport report = new ExaminationReport();
        report.setUserId(userId);
        report.setFile(filename);
        report.setKeyIndex(keyIndex);
        report.setTime(LocalDateTime.now());

        repository.save(report);
    }

    public List<ExaminationReport> list(Integer userId) {
        return repository.findByUserIdOrderByTimeDesc(userId);
    }

    /** PDF 关键指标提取 */
    private Map<String, Object> extractKeyIndex(File pdf) throws Exception {
        Map<String, Object> map = new HashMap<>();

        try (PDDocument doc = PDDocument.load(pdf)) {
            String text = new PDFTextStripper().getText(doc);

            extract(map, text, "身高", "height");
            extract(map, text, "体重", "weight");
            extract(map, text, "BMI", "BMI");
            extract(map, text, "血压", "bloodPressure");
            extract(map, text, "空腹血糖", "glucose");
            extract(map, text, "总胆固醇", "cholesterol");
            extract(map, text, "尿酸", "uricAcid");
        }

        return map;
    }

    private void extract(Map<String, Object> map, String text, String key, String mapKey) {
        String regex = key + "\\s*[:：]\\s*([\\d./]+)";
        var m = java.util.regex.Pattern.compile(regex).matcher(text);
        if (m.find()) {
            map.put(mapKey, m.group(1));
        }
    }

    public File loadFile(String filename) {

        if (uploadDir == null) {
            throw new IllegalStateException("file.upload-dir 未配置");
        }

        File file = new File(uploadDir, filename);

        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }

        return file;
    }

}

