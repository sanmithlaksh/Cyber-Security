package com.cybershield.portal.service;

import com.cybershield.portal.model.ThreatReport;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Generates a PDF document with reports list and stats.
     */
    public void generatePdfReport(List<ThreatReport> reports, OutputStream out) throws DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(document, out);
        document.open();

        // Title Section
        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, java.awt.Color.BLACK);
        Paragraph title = new Paragraph("CYBERSHIELD - SECURITY INCIDENT SUMMARY REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Stats Summary
        com.lowagie.text.Font subFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.DARK_GRAY);
        Paragraph summary = new Paragraph(String.format("Total Reports Processed: %d", reports.size()), subFont);
        summary.setSpacingAfter(15);
        document.add(summary);

        // Table Header
        float[] columnWidths = {2.5f, 4f, 3.5f, 2.5f, 2.5f, 3f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        com.lowagie.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE);
        String[] headers = {"Report ID", "Title", "Threat Type", "Severity", "Status", "Date"};
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
            cell.setBackgroundColor(new java.awt.Color(30, 41, 59)); // Slate-800 dark theme color
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }

        // Table Rows
        com.lowagie.text.Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, java.awt.Color.BLACK);
        for (ThreatReport report : reports) {
            table.addCell(new Phrase(report.getReportId(), bodyFont));
            table.addCell(new Phrase(report.getTitle(), bodyFont));
            table.addCell(new Phrase(report.getThreatType(), bodyFont));
            
            // Format Severity with color
            PdfPCell sevCell = new PdfPCell(new Phrase(report.getSeverity(), bodyFont));
            if ("CRITICAL".equals(report.getSeverity())) {
                sevCell.setBackgroundColor(new java.awt.Color(254, 226, 226)); // Soft red
            } else if ("HIGH".equals(report.getSeverity())) {
                sevCell.setBackgroundColor(new java.awt.Color(254, 243, 199)); // Soft orange
            } else if ("MEDIUM".equals(report.getSeverity())) {
                sevCell.setBackgroundColor(new java.awt.Color(254, 249, 195)); // Soft yellow
            } else {
                sevCell.setBackgroundColor(new java.awt.Color(220, 252, 231)); // Soft green
            }
            table.addCell(sevCell);
            
            table.addCell(new Phrase(report.getStatus(), bodyFont));
            table.addCell(new Phrase(report.getDateTime() != null ? report.getDateTime().format(DATE_FORMATTER) : "N/A", bodyFont));
        }

        document.add(table);
        document.close();
    }

    /**
     * Generates an Excel workbook (XLSX) using Apache POI.
     */
    public void generateExcelReport(List<ThreatReport> reports, OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Threat Incidents");

            // Header Style
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Create Header Row
            String[] headers = {
                    "Report ID", "Reporter Name", "Reporter Email", "Incident Title", "Threat Type",
                    "Description", "Date & Time", "Website URL", "Risk Score", "Severity", "Status", "Analyst Notes"
            };

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (ThreatReport report : reports) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(report.getReportId());
                row.createCell(1).setCellValue(report.getUser() != null ? report.getUser().getName() : "Anonymous");
                row.createCell(2).setCellValue(report.getUser() != null ? report.getUser().getEmail() : "Anonymous");
                row.createCell(3).setCellValue(report.getTitle());
                row.createCell(4).setCellValue(report.getThreatType());
                row.createCell(5).setCellValue(report.getDescription() != null ? report.getDescription() : "");
                row.createCell(6).setCellValue(report.getDateTime() != null ? report.getDateTime().format(DATE_FORMATTER) : "N/A");
                row.createCell(7).setCellValue(report.getWebsiteUrl() != null ? report.getWebsiteUrl() : "");
                row.createCell(8).setCellValue(report.getRiskScore());
                row.createCell(9).setCellValue(report.getSeverity());
                row.createCell(10).setCellValue(report.getStatus());
                row.createCell(11).setCellValue(report.getAnalystNotes() != null ? report.getAnalystNotes() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }

    /**
     * Generates a simple CSV format report.
     */
    public void generateCsvReport(List<ThreatReport> reports, OutputStream out) {
        try (PrintWriter writer = new PrintWriter(out)) {
            // Write BOM for Excel compatibility with UTF-8
            writer.write('\ufeff');
            
            // Header
            writer.println("Report ID,Title,Threat Type,Date,Risk Score,Severity,Status,URL,Location");

            // Data rows
            for (ThreatReport report : reports) {
                writer.println(String.format(
                        "\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\"",
                        escapeCsv(report.getReportId()),
                        escapeCsv(report.getTitle()),
                        escapeCsv(report.getThreatType()),
                        report.getDateTime() != null ? report.getDateTime().format(DATE_FORMATTER) : "",
                        report.getRiskScore(),
                        escapeCsv(report.getSeverity()),
                        escapeCsv(report.getStatus()),
                        escapeCsv(report.getWebsiteUrl()),
                        escapeCsv(report.getLocation())
                ));
            }
            writer.flush();
        }
    }

    private String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }
}
