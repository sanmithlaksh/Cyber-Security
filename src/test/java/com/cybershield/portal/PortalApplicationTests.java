package com.cybershield.portal;

import com.cybershield.portal.service.DijkstraService;
import com.cybershield.portal.service.ThreatAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PortalApplicationTests {

    @Autowired
    private DijkstraService dijkstraService;

    @Autowired
    private ThreatAnalysisService analysisService;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$");

    @Test
    void contextLoads() {
        // Basic context loading check
    }

    @Test
    void testDijkstraShortestPath() {
        // Test A -> E shortest path
        DijkstraService.PathResult resultAE = dijkstraService.findShortestPath("Node A", "Node E");
        assertNotNull(resultAE);
        assertEquals(6, resultAE.getTotalWeight());
        
        List<String> pathAE = resultAE.getPath();
        assertEquals(4, pathAE.size());
        assertEquals("Node A", pathAE.get(0));
        assertEquals("Node B", pathAE.get(1));
        assertEquals("Node D", pathAE.get(2));
        assertEquals("Node E", pathAE.get(3));

        // Test A -> D shortest path (A -> B -> D: cost 3)
        DijkstraService.PathResult resultAD = dijkstraService.findShortestPath("Node A", "Node D");
        assertEquals(3, resultAD.getTotalWeight());
        assertEquals("Node B", resultAD.getPath().get(1));
    }

    @Test
    void testUrlRiskScanner() {
        // Test a safe URL
        ThreatAnalysisService.UrlScanResult safeResult = analysisService.scanUrl("https://www.google.com");
        assertEquals("SAFE", safeResult.riskLevel);
        assertTrue(safeResult.score <= 30);

        // Test an insecure blacklisted URL
        ThreatAnalysisService.UrlScanResult badResult = analysisService.scanUrl("http://free-paypal-gift.com/login");
        assertEquals("CRITICAL", badResult.riskLevel);
        assertTrue(badResult.score >= 80);
        assertTrue(badResult.recommendations.contains("does not use HTTPS"));
        assertTrue(badResult.recommendations.contains("found in the Threat Intelligence Blacklist"));
    }

    @Test
    void testPasswordStrengthValidation() {
        // Valid password: uppercase, lowercase, digit, special char, length >= 8
        assertTrue(PASSWORD_PATTERN.matcher("Secured@2026").matches());
        assertTrue(PASSWORD_PATTERN.matcher("Admin@123").matches());

        // Invalid: No uppercase
        assertFalse(PASSWORD_PATTERN.matcher("secured@2026").matches());
        // Invalid: No digit
        assertFalse(PASSWORD_PATTERN.matcher("Secured@").matches());
        // Invalid: No special char
        assertFalse(PASSWORD_PATTERN.matcher("Secured2026").matches());
        // Invalid: Too short
        assertFalse(PASSWORD_PATTERN.matcher("Sec@1").matches());
    }
}
