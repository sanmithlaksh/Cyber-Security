package com.cybershield.portal.controller;

import com.cybershield.portal.model.ThreatIntel;
import com.cybershield.portal.model.ThreatReport;
import com.cybershield.portal.model.User;
import com.cybershield.portal.repository.ThreatIntelRepository;
import com.cybershield.portal.repository.ThreatReportRepository;
import com.cybershield.portal.repository.UserRepository;
import com.cybershield.portal.service.AuthService;
import com.cybershield.portal.service.NotificationService;
import com.cybershield.portal.service.ThreatAnalysisService;
import com.cybershield.portal.service.ThreatReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    private final ThreatReportService reportService;
    private final ThreatReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final ThreatAnalysisService analysisService;
    private final ThreatIntelRepository intelRepository;
    private final NotificationService notificationService;

    public DashboardController(ThreatReportService reportService, ThreatReportRepository reportRepository,
                               UserRepository userRepository, AuthService authService,
                               ThreatAnalysisService analysisService, ThreatIntelRepository intelRepository,
                               NotificationService notificationService) {
        this.reportService = reportService;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.analysisService = analysisService;
        this.intelRepository = intelRepository;
        this.notificationService = notificationService;
    }

    /**
     * Public landing page displaying live statistics from the database.
     */
    @GetMapping("/")
    public String showLandingPage(Model model) {
        long totalThreats = reportRepository.count();
        long activeInvestigations = reportRepository.countByStatus("INVESTIGATING") + reportRepository.countByStatus("UNDER_REVIEW");
        long resolvedCases = reportRepository.countByStatus("RESOLVED");
        long registeredUsers = userRepository.countByRole("ROLE_USER");

        model.addAttribute("totalThreats", totalThreats);
        model.addAttribute("activeInvestigations", activeInvestigations);
        model.addAttribute("resolvedCases", resolvedCases);
        model.addAttribute("registeredUsers", registeredUsers);
        return "landing";
    }

    /**
     * Resolves authenticated users and routes them to their specific dashboards (Admin, Analyst, or standard User).
     */
    @GetMapping("/dashboard")
    public String showDashboard(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        if ("ROLE_ADMIN".equals(user.getRole())) {
            return "redirect:/admin";
        } else if ("ROLE_ANALYST".equals(user.getRole())) {
            return "redirect:/analyst";
        }

        // Standard user dashboard variables
        List<ThreatReport> userReports = reportService.getReportsByUser(user);
        model.addAttribute("reports", userReports);
        model.addAttribute("user", user);
        model.addAttribute("notifications", notificationService.getNotificationsForUser(user));
        model.addAttribute("unreadNotifications", notificationService.getUnreadCount(user));
        return "user/dashboard";
    }

    /**
     * Form to submit a new threat report.
     */
    @GetMapping("/reports/new")
    public String showReportForm(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("user", user);
        return "user/report-threat";
    }

    @PostMapping("/reports/new")
    public String handleReportSubmission(
            @RequestParam("title") String title,
            @RequestParam("threatType") String threatType,
            @RequestParam("description") String description,
            @RequestParam(value = "websiteUrl", required = false) String websiteUrl,
            @RequestParam("location") String location,
            @RequestParam("contactInfo") String contactInfo,
            @RequestParam(value = "evidenceFile", required = false) MultipartFile file,
            Principal principal,
            Model model) {
        
        User user = getCurrentUser(principal);
        try {
            ThreatReport report = reportService.submitReport(
                    title, threatType, description, websiteUrl, location, contactInfo, file, user);
            return "redirect:/reports/track/" + report.getReportId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error saving report: " + e.getMessage());
            model.addAttribute("user", user);
            return "user/report-threat";
        }
    }

    /**
     * Page where users search/enter Report ID to track progress.
     */
    @GetMapping("/reports/track")
    public String showTrackPage(Principal principal, Model model) {
        model.addAttribute("user", getCurrentUser(principal));
        return "user/track-threat";
    }

    @GetMapping("/reports/track/{id}")
    public String showReportTimeline(@PathVariable("id") String reportId, Principal principal, Model model) {
        User user = getCurrentUser(principal);
        Optional<ThreatReport> reportOpt = reportService.getReportById(reportId);
        
        if (reportOpt.isPresent()) {
            ThreatReport report = reportOpt.get();
            // Validate ownership: Users can only track their own reports (unless anonymous or they are an Analyst/Admin)
            if (report.getUser() != null && user != null && 
                !report.getUser().getId().equals(user.getId()) && 
                "ROLE_USER".equals(user.getRole())) {
                return "redirect:/dashboard?error=unauthorized";
            }
            model.addAttribute("report", report);
        } else {
            model.addAttribute("errorMessage", "Report ID not found: " + reportId);
        }
        
        model.addAttribute("user", user);
        return "user/track-threat";
    }

    /**
     * Public Threat Intelligence Database page.
     */
    @GetMapping("/threat-db")
    public String showThreatDatabase(@RequestParam(value = "q", required = false) String query, Model model, Principal principal) {
        List<ThreatIntel> threats;
        if (query != null && !query.trim().isEmpty()) {
            threats = intelRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    query, query, query);
            model.addAttribute("query", query);
        } else {
            threats = intelRepository.findAll();
        }
        model.addAttribute("threats", threats);
        model.addAttribute("user", getCurrentUser(principal));
        return "threat-db";
    }

    /**
     * Public URL Threat Scanner tools.
     */
    @GetMapping("/scanner")
    public String showScannerPage(Principal principal, Model model) {
        model.addAttribute("user", getCurrentUser(principal));
        return "scanner";
    }

    @PostMapping("/scanner")
    public String handleUrlScan(@RequestParam("url") String url, Principal principal, Model model) {
        ThreatAnalysisService.UrlScanResult result = analysisService.scanUrl(url);
        model.addAttribute("url", url);
        model.addAttribute("scanResult", result);
        model.addAttribute("user", getCurrentUser(principal));
        return "scanner";
    }

    /**
     * Public Phishing Detector tools.
     */
    @GetMapping("/phishing-detector")
    public String showPhishingDetector(Principal principal, Model model) {
        model.addAttribute("user", getCurrentUser(principal));
        return "phishing-detector";
    }

    @PostMapping("/phishing-detector")
    public String handlePhishingDetection(@RequestParam("content") String content, Principal principal, Model model) {
        ThreatAnalysisService.EmailScanResult result = analysisService.scanEmail(content);
        model.addAttribute("content", content);
        model.addAttribute("scanResult", result);
        model.addAttribute("user", getCurrentUser(principal));
        return "phishing-detector";
    }

    @PostMapping("/api/notifications/read")
    @ResponseBody
    public String markNotificationsRead(Principal principal) {
        User user = getCurrentUser(principal);
        if (user != null) {
            notificationService.markAllAsRead(user);
            return "SUCCESS";
        }
        return "ERROR";
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        return authService.findUserByEmail(principal.getName()).orElse(null);
    }
}
