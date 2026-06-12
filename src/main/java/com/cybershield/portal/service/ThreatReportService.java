package com.cybershield.portal.service;

import com.cybershield.portal.model.ThreatReport;
import com.cybershield.portal.model.User;
import com.cybershield.portal.repository.ThreatReportRepository;
import com.cybershield.portal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ThreatReportService {

    private final ThreatReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ThreatAnalysisService analysisService;
    private final NotificationService notificationService;

    // Local uploads directory (in the project root)
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    public ThreatReportService(ThreatReportRepository reportRepository, UserRepository userRepository,
                               ThreatAnalysisService analysisService, NotificationService notificationService) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.analysisService = analysisService;
        this.notificationService = notificationService;
    }

    /**
     * Submits a new threat report. Generates a custom sequence ID, scans the URL, saves file evidence, and logs notifications.
     */
    public ThreatReport submitReport(String title, String threatType, String description, String url,
                                     String location, String contactInfo, MultipartFile file, User reporter) throws IOException {
        
        ThreatReport report = new ThreatReport();
        report.setTitle(title);
        report.setThreatType(threatType);
        report.setDescription(description);
        report.setWebsiteUrl(url);
        report.setLocation(location);
        report.setContactInfo(contactInfo);
        report.setDateTime(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        report.setUser(reporter);
        report.setStatus("SUBMITTED");

        // 1. Report ID Generation: CYB-YYYY-NNNN
        String year = String.valueOf(LocalDateTime.now().getYear());
        String prefix = "CYB-" + year + "-";
        long count = reportRepository.countByReportIdStartingWith(prefix);
        String nextId = prefix + String.format("%04d", count + 1001); // starts sequential counts at 1001 for professional display
        report.setReportId(nextId);

        // 2. Automated URL Risk Analysis
        if (url != null && !url.trim().isEmpty()) {
            ThreatAnalysisService.UrlScanResult scanResult = analysisService.scanUrl(url);
            report.setRiskScore(scanResult.score);
            report.setSeverity(scanResult.riskLevel);
        } else {
            // Default severity if no URL is provided (can be manual assessment based on description/type)
            report.setRiskScore(20);
            report.setSeverity("SAFE");
        }

        // 3. Save uploaded evidence file
        if (file != null && !file.isEmpty()) {
            File uploadFolder = new File(UPLOAD_DIR);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String randomFilename = UUID.randomUUID().toString() + fileExtension;
            File savedFile = new File(UPLOAD_DIR + randomFilename);
            file.transferTo(savedFile);
            
            // Save absolute or relative path for serving
            report.setEvidencePath("/uploads/" + randomFilename);
        }

        ThreatReport saved = reportRepository.save(report);

        // 4. Create in-app notifications
        if (reporter != null) {
            notificationService.createNotification(reporter, 
                    String.format("Your threat report has been submitted successfully. Report ID: %s.", saved.getReportId()));
        }

        // Simulating mock console email notification (Java Mail API integration)
        System.out.println("=================================================");
        System.out.println("MOCK Java Mail Alert: Sending Email to " + (reporter != null ? reporter.getEmail() : contactInfo));
        System.out.println("Subject: CyberShield Incident Report Submitted - " + saved.getReportId());
        System.out.println("Message: Dear User, your report (" + saved.getTitle() + ") has been received.");
        System.out.println("Initial Risk Score: " + saved.getRiskScore() + " (" + saved.getSeverity() + ")");
        System.out.println("=================================================");

        return saved;
    }

    /**
     * Updates the status and analyst notes of a report. Triggers notifications.
     */
    public ThreatReport updateStatus(String reportId, String newStatus, String notes, User analyst) {
        Optional<ThreatReport> reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isPresent()) {
            ThreatReport report = reportOpt.get();
            report.setStatus(newStatus);
            report.setAnalystNotes(notes);
            if (analyst != null) {
                report.setAnalyst(analyst);
            }
            ThreatReport saved = reportRepository.save(report);

            // Notify user
            if (report.getUser() != null) {
                notificationService.createNotification(report.getUser(), 
                        String.format("Update on Case %s: Status changed to %s. Notes: %s", 
                                report.getReportId(), newStatus, notes));
            }

            // Simulating email alert on status update
            System.out.println("=================================================");
            System.out.println("MOCK Java Mail Alert: Sending Email to " + (report.getUser() != null ? report.getUser().getEmail() : report.getContactInfo()));
            System.out.println("Subject: CyberShield Case Status Update - " + report.getReportId());
            System.out.println("Message: Your case status has been updated to: " + newStatus + ". Analyst Notes: " + notes);
            System.out.println("=================================================");

            return saved;
        }
        return null;
    }

    public List<ThreatReport> getReportsByUser(User user) {
        return reportRepository.findByUserOrderByDateTimeDesc(user);
    }

    public List<ThreatReport> getAllReports() {
        return reportRepository.findAllByOrderByDateTimeDesc();
    }

    public Optional<ThreatReport> getReportById(String reportId) {
        return reportRepository.findById(reportId);
    }

    public List<ThreatReport> getReportsBySeverity(String severity) {
        return reportRepository.findBySeverityOrderByDateTimeDesc(severity);
    }

    public List<ThreatReport> getReportsByStatus(String status) {
        return reportRepository.findByStatusOrderByDateTimeDesc(status);
    }
}
