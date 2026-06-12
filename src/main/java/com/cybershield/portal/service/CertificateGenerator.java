package com.cybershield.portal.service;

import com.cybershield.portal.model.QuizResult;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;


@Service
public class CertificateGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public void generateCertificate(QuizResult result, OutputStream out) throws DocumentException {
        // Landscape A4 Page
        Document document = new Document(PageSize.A4.rotate(), 54, 54, 54, 54);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        // Add a background canvas for borders
        PdfContentByte canvas = writer.getDirectContent();
        Rectangle page = document.getPageSize();
        
        // Draw Outer Border (Cyber Blue)
        canvas.setLineWidth(5);
        canvas.setColorStroke(new Color(14, 116, 144)); // cyan-700
        canvas.rectangle(page.getLeft() + 20, page.getBottom() + 20, page.getWidth() - 40, page.getHeight() - 40);
        canvas.stroke();

        // Draw Inner Thin Gold Border
        canvas.setLineWidth(1.5f);
        canvas.setColorStroke(new Color(234, 179, 8)); // amber-500
        canvas.rectangle(page.getLeft() + 26, page.getBottom() + 26, page.getWidth() - 52, page.getHeight() - 52);
        canvas.stroke();

        // Header Spacer
        Paragraph spacer = new Paragraph();
        spacer.setSpacingAfter(40);
        document.add(spacer);

        // Certificate Header
        Font logoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(14, 116, 144));
        Paragraph logo = new Paragraph("CYBERSHIELD PORTAL", logoFont);
        logo.setAlignment(Element.ALIGN_CENTER);
        logo.setSpacingAfter(10);
        document.add(logo);

        Font certTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, new Color(30, 41, 59));
        Paragraph certTitle = new Paragraph("CERTIFICATE OF COMPLETION", certTitleFont);
        certTitle.setAlignment(Element.ALIGN_CENTER);
        certTitle.setSpacingAfter(25);
        document.add(certTitle);

        // Subtitle text
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Color.DARK_GRAY);
        Paragraph subText = new Paragraph("This is proudly presented to", subFont);
        subText.setAlignment(Element.ALIGN_CENTER);
        subText.setSpacingAfter(15);
        document.add(subText);

        // Name of Recipient
        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, new Color(234, 179, 8));
        Paragraph name = new Paragraph(result.getUser().getName().toUpperCase(), nameFont);
        name.setAlignment(Element.ALIGN_CENTER);
        name.setSpacingAfter(20);
        document.add(name);

        // Verification statement
        Paragraph desc = new Paragraph(
                String.format("for demonstrating exemplary cyber awareness by successfully passing the\n" +
                        "\"%s Security Quiz\" with a score of %d%%.", 
                        result.getCategory(), result.getScore()), subFont);
        desc.setAlignment(Element.ALIGN_CENTER);
        desc.setLeading(22);
        desc.setSpacingAfter(35);
        document.add(desc);

        // Footer Table: Date, Signatures, and Verification UUID
        PdfPTable footerTable = new PdfPTable(3);
        footerTable.setWidthPercentage(100);
        
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Font valFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(30, 41, 59));

        // Date Column
        PdfPCell dateCell = new PdfPCell();
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        String formattedDate = "";
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(result.getDateCompleted(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            formattedDate = dt.format(DATE_FORMATTER);
        } catch (Exception e) {
            formattedDate = result.getDateCompleted();
        }
        Paragraph dateVal = new Paragraph(formattedDate, valFont);
        dateVal.setAlignment(Element.ALIGN_CENTER);
        Paragraph dateLabel = new Paragraph("DATE ISSUED", footerFont);
        dateLabel.setAlignment(Element.ALIGN_CENTER);
        dateCell.addElement(dateVal);
        dateCell.addElement(dateLabel);
        footerTable.addCell(dateCell);

        // Certificate verification UUID Column
        PdfPCell idCell = new PdfPCell();
        idCell.setBorder(Rectangle.NO_BORDER);
        idCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph idVal = new Paragraph(result.getCertificateUuid().substring(0, 18).toUpperCase(), valFont);
        idVal.setAlignment(Element.ALIGN_CENTER);
        Paragraph idLabel = new Paragraph("VERIFICATION ID", footerFont);
        idLabel.setAlignment(Element.ALIGN_CENTER);
        idCell.addElement(idVal);
        idCell.addElement(idLabel);
        footerTable.addCell(idCell);

        // Authority Column
        PdfPCell signCell = new PdfPCell();
        signCell.setBorder(Rectangle.NO_BORDER);
        signCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph signVal = new Paragraph("CyberShield Authority", valFont);
        signVal.setAlignment(Element.ALIGN_CENTER);
        Paragraph signLabel = new Paragraph("ISSUING BODY", footerFont);
        signLabel.setAlignment(Element.ALIGN_CENTER);
        signCell.addElement(signVal);
        signCell.addElement(signLabel);
        footerTable.addCell(signCell);

        document.add(footerTable);
        document.close();
    }
}
