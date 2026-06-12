package com.cybershield.portal.controller;

import com.cybershield.portal.model.ThreatIntel;
import com.cybershield.portal.model.ThreatReport;
import com.cybershield.portal.model.User;
import com.cybershield.portal.repository.ThreatIntelRepository;
import com.cybershield.portal.repository.ThreatReportRepository;
import com.cybershield.portal.repository.UserRepository;
import com.cybershield.portal.service.AuthService;
import com.cybershield.portal.service.ReportGenerator;
import com.cybershield.portal.service.ThreatReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final ThreatReportService reportService;
    private final ThreatReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ThreatIntelRepository intelRepository;
    private final AuthService authService;
    private final ReportGenerator reportGenerator;

    public AdminController(ThreatReportService reportService, ThreatReportRepository reportRepository,
                           UserRepository userRepository, ThreatIntelRepository intelRepository,
                           AuthService authService, ReportGenerator reportGenerator) {
        this.reportService = reportService;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.intelRepository = intelRepository;
        this.authService = authService;
        this.reportGenerator = reportGenerator;
    }

    /**
     * Admin Dashboard view containing statistics aggregates and recent reports list.
     */
    @GetMapping("/admin")
    public String showAdminDashboard(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        
        List<ThreatReport> allReports = reportService.getAllReports();
        long userCount = userRepository.countByRole("ROLE_USER");
        long analystCount = userRepository.countByRole("ROLE_ANALYST");
        long resolvedCount = reportRepository.countByStatus("RESOLVED");
        long activeCount = reportRepository.countByStatus("INVESTIGATING") + reportRepository.countByStatus("UNDER_REVIEW");
        long criticalCount = reportRepository.countBySeverity("CRITICAL");

        model.addAttribute("reports", allReports);
        model.addAttribute("userCount", userCount);
        model.addAttribute("analystCount", analystCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("criticalCount", criticalCount);
        model.addAttribute("user", user);

        // Chart Data - Threat Types
        Map<String, Long> threatTypeCounts = allReports.stream()
                .collect(Collectors.groupingBy(ThreatReport::getThreatType, Collectors.counting()));
        model.addAttribute("threatTypeKeys", threatTypeCounts.keySet());
        model.addAttribute("threatTypeValues", threatTypeCounts.values());

        // Chart Data - Statuses
        Map<String, Long> statusCounts = allReports.stream()
                .collect(Collectors.groupingBy(ThreatReport::getStatus, Collectors.counting()));
        model.addAttribute("statusKeys", statusCounts.keySet());
        model.addAttribute("statusValues", statusCounts.values());

        return "admin/dashboard";
    }

    /**
     * Analyst Portal dashboard.
     */
    @GetMapping("/analyst")
    public String showAnalystDashboard(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        List<ThreatReport> reports = reportService.getAllReports();
        
        // Filter out cases that are critical/high
        List<ThreatReport> highSeverityReports = reports.stream()
                .filter(r -> "CRITICAL".equals(r.getSeverity()) || "HIGH".equals(r.getSeverity()))
                .collect(Collectors.toList());

        model.addAttribute("reports", reports);
        model.addAttribute("highSeverityReports", highSeverityReports);
        model.addAttribute("user", user);
        return "analyst/dashboard";
    }

    /**
     * Updates incident status from the Analyst/Admin dashboard.
     */
    @PostMapping("/analyst/update-status")
    public String handleStatusUpdate(
            @RequestParam("reportId") String reportId,
            @RequestParam("status") String status,
            @RequestParam("notes") String notes,
            Principal principal) {
        User analyst = getCurrentUser(principal);
        reportService.updateStatus(reportId, status, notes, analyst);
        return "redirect:/dashboard"; // redirects back, DashboardController handles routing based on role
    }

    /**
     * Admin screen to manage registered users.
     */
    @GetMapping("/admin/users")
    public String manageUsers(Principal principal, Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("user", getCurrentUser(principal));
        return "admin/users";
    }

    /**
     * Admin screen to manage Threat Database.
     */
    @GetMapping("/admin/threats")
    public String manageThreats(Principal principal, Model model) {
        model.addAttribute("threats", intelRepository.findAll());
        model.addAttribute("user", getCurrentUser(principal));
        return "admin/threats";
    }

    @PostMapping("/admin/threats/add")
    public String addThreatIntel(
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam("riskLevel") String riskLevel,
            @RequestParam("description") String description,
            @RequestParam("preventionMethods") String preventionMethods,
            Principal principal) {
        
        ThreatIntel intel = new ThreatIntel(
                name, category, riskLevel, description, preventionMethods, LocalDate.now().toString());
        intelRepository.save(intel);
        return "redirect:/admin/threats";
    }

    /**
     * Handles Daily, Weekly, Monthly, and Annual report downloads.
     */
    @PostMapping("/admin/reports/export")
    public void exportReports(
            @RequestParam("format") String format,
            @RequestParam("timeframe") String timeframe,
            HttpServletResponse response) throws IOException {

        List<ThreatReport> reports = reportRepository.findAllByOrderByDateTimeDesc();
        LocalDateTime threshold;

        switch (timeframe.toLowerCase()) {
            case "daily":
                threshold = LocalDateTime.now().minusDays(1);
                break;
            case "weekly":
                threshold = LocalDateTime.now().minusWeeks(1);
                break;
            case "monthly":
                threshold = LocalDateTime.now().minusMonths(1);
                break;
            case "annual":
                threshold = LocalDateTime.now().minusYears(1);
                break;
            default:
                threshold = null;
        }

        if (threshold != null) {
            reports = reports.stream()
                    .filter(r -> {
                        if (r.getDateTime() == null) return false;
                        try {
                            LocalDateTime dt = LocalDateTime.parse(r.getDateTime(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            return dt.isAfter(threshold);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        try {
            OutputStream out = response.getOutputStream();

            if ("pdf".equalsIgnoreCase(format)) {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=CyberShield_Report_" + timeframe + ".pdf");
                reportGenerator.generatePdfReport(reports, out);
            } else if ("excel".equalsIgnoreCase(format)) {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=CyberShield_Report_" + timeframe + ".xlsx");
                reportGenerator.generateExcelReport(reports, out);
            } else {
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment; filename=CyberShield_Report_" + timeframe + ".csv");
                reportGenerator.generateCsvReport(reports, out);
            }
            out.flush();
        } catch (Exception e) {
            System.err.println("Error generating export file: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        return authService.findUserByEmail(principal.getName()).orElse(null);
    }
}
