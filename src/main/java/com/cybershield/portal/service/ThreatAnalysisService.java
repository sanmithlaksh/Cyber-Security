package com.cybershield.portal.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ThreatAnalysisService {

    private static final Set<String> BLACKLISTED_DOMAINS = new HashSet<>(Arrays.asList(
            "free-paypal-gift.com", "verify-login-banking.xyz", "secure-update-bank.com",
            "whatsapp-gift-rewards.top", "urgent-action-required.net", "netflix-free-premium.xyz",
            "secure-billing-icloud.info", "meta-security-support.cc", "runscape-gold-hack.xyz"
    ));

    private static final String[] SUSPICIOUS_KEYWORDS = {
            "verify", "bank", "update", "paypal", "login", "secure", "gift", "urgent",
            "free", "reward", "otp", "whatsapp", "signin", "alert", "claim", "account",
            "bonus", "suspended", "security-alert", "support-team"
    };

    private static final String[] URGENT_PHRASES = {
            "urgent action", "verify now", "immediately", "within 24 hours", "suspended",
            "action required", "compromised", "account lock", "unauthorized access"
    };

    private static final String[] THREATENING_PHRASES = {
            "closed", "terminated", "fined", "arrest", "legal action", "prosecution",
            "permanently delete", "blocked"
    };

    private static final String[] CREDENTIAL_PHRASES = {
            "click here", "login to", "update password", "verify account", "confirm credentials",
            "enter password", "reset your"
    };

    private static final String[] FINANCIAL_PHRASES = {
            "bank alert", "transfer", "refund", "invoice", "payment approved", "winning amount",
            "inheritance", "unclaimed funds", "wire transfer", "lottery"
    };

    private static final Pattern IP_PATTERN = Pattern.compile("^https?://(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");

    /**
     * Scans a URL and calculates a risk score (0-100).
     */
    public UrlScanResult scanUrl(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return new UrlScanResult(0, "SAFE", "Invalid or empty URL. Nothing to scan.");
        }

        String url = urlString.trim().toLowerCase();
        int score = 0;
        StringBuilder recommendations = new StringBuilder();

        // 1. HTTPS Check
        if (!url.startsWith("https://")) {
            score += 25;
            recommendations.append("- URL does not use HTTPS. Communication is unencrypted and insecure. Avoid entering sensitive details.\n");
        } else {
            recommendations.append("- Site uses HTTPS, which secures active communication, but note that malicious sites can also use HTTPS.\n");
        }

        // 2. IP Address check
        if (IP_PATTERN.matcher(url).find()) {
            score += 25;
            recommendations.append("- URL uses a raw IP address instead of a domain name, which is highly indicative of cyber scams.\n");
        }

        // 3. Blacklist Check
        String host = getHost(url);
        if (BLACKLISTED_DOMAINS.contains(host)) {
            score += 50;
            recommendations.append("- Domain is explicitly found in the Threat Intelligence Blacklist database. DANGEROUS!\n");
        }

        // 4. Suspicious TLD check
        if (host != null && (host.endsWith(".xyz") || host.endsWith(".top") || host.endsWith(".info") || host.endsWith(".cc") || host.endsWith(".tk"))) {
            score += 15;
            recommendations.append("- Domain uses a high-risk Top-Level Domain (TLD) like .xyz, .top, or .info, commonly used in spam campaigns.\n");
        }

        // 5. Keyword analysis
        int keywordCount = 0;
        for (String keyword : SUSPICIOUS_KEYWORDS) {
            if (url.contains(keyword)) {
                score += 10;
                keywordCount++;
                if (keywordCount >= 3) break; // cap keyword penalty
            }
        }
        if (keywordCount > 0) {
            recommendations.append("- URL contains suspicious keywords related to banking, login, account updates, or social rewards.\n");
        }

        // 6. Domain Age Mock
        int simulatedAgeDays = getSimulatedDomainAge(url);
        if (simulatedAgeDays < 180) {
            score += 15;
            recommendations.append("- Domain age appears very recent (less than 6 months old). New domains are frequently associated with phishing.\n");
        }

        // Bound score between 0 and 100
        score = Math.min(100, score);
        
        String riskLevel;
        if (score <= 30) {
            riskLevel = "SAFE";
            if (recommendations.length() == 0 || recommendations.toString().startsWith("- Site uses HTTPS")) {
                recommendations.append("- No obvious threat vectors detected. URL seems relatively safe. Still, check credentials before submitting.\n");
            }
        } else if (score <= 60) {
            riskLevel = "MEDIUM";
        } else if (score <= 80) {
            riskLevel = "HIGH";
        } else {
            riskLevel = "CRITICAL";
        }

        return new UrlScanResult(score, riskLevel, recommendations.toString());
    }

    /**
     * Analyzes email content and outputs phishing probability.
     */
    public EmailScanResult scanEmail(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new EmailScanResult(0, "SAFE", "Invalid or empty content. Nothing to scan.");
        }

        String text = content.toLowerCase();
        int score = 0;
        int urgentCount = 0;
        int threatCount = 0;
        int credCount = 0;
        int finCount = 0;

        for (String phrase : URGENT_PHRASES) {
            if (text.contains(phrase)) {
                score += 15;
                urgentCount++;
            }
        }
        for (String phrase : THREATENING_PHRASES) {
            if (text.contains(phrase)) {
                score += 15;
                threatCount++;
            }
        }
        for (String phrase : CREDENTIAL_PHRASES) {
            if (text.contains(phrase)) {
                score += 20;
                credCount++;
            }
        }
        for (String phrase : FINANCIAL_PHRASES) {
            if (text.contains(phrase)) {
                score += 15;
                finCount++;
            }
        }

        // Cap score
        score = Math.min(100, score);

        StringBuilder analysis = new StringBuilder();
        if (urgentCount > 0) {
            analysis.append(String.format("- Detected %d urgent phrases. Phishing emails frequently use false urgency to bypass logical inspection.\n", urgentCount));
        }
        if (threatCount > 0) {
            analysis.append(String.format("- Detected %d threatening terms. Scammers use coercion (account closure, legal warnings) to create panic.\n", threatCount));
        }
        if (credCount > 0) {
            analysis.append(String.format("- Detected %d credential-collection indicators. Phishing emails try to capture passwords, MFA codes, or links.\n", credCount));
        }
        if (finCount > 0) {
            analysis.append(String.format("- Detected %d financial trigger terms. Requests for transfers, refunds, or invoice billing are common lures.\n", finCount));
        }

        String riskLevel;
        if (score <= 30) {
            riskLevel = "SAFE";
            analysis.append("- Lower risk profile detected. However, double check sender addresses and look out for spelling mistakes.");
        } else if (score <= 65) {
            riskLevel = "MODERATE RISK";
            analysis.append("- Moderate indicator matches. Inspect closely. Verify the sender through official channels before interacting.");
        } else {
            riskLevel = "DANGEROUS";
            analysis.append("- Critical indicator match! Highly characteristic of a coordinated phishing/scam attack. Do NOT open attachments or click links.");
        }

        return new EmailScanResult(score, riskLevel, analysis.toString());
    }

    private String getHost(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host != null) {
                return host.startsWith("www.") ? host.substring(4) : host;
            }
        } catch (Exception e) {
            // fall back to regex/split if invalid URI format
            String clean = url.replaceFirst("^(https?://)?(www\\.)?", "");
            int slash = clean.indexOf('/');
            if (slash != -1) {
                return clean.substring(0, slash);
            }
            return clean;
        }
        return null;
    }

    private int getSimulatedDomainAge(String url) {
        // Consistent hash of URL to produce a stable simulated age for demo purposes
        int hash = Math.abs(url.hashCode());
        // Random age between 5 days and 10 years (3650 days)
        return 5 + (hash % 3645);
    }

    // Inner record-like classes
    public static class UrlScanResult {
        public final int score;
        public final String riskLevel;
        public final String recommendations;

        public UrlScanResult(int score, String riskLevel, String recommendations) {
            this.score = score;
            this.riskLevel = riskLevel;
            this.recommendations = recommendations;
        }
    }

    public static class EmailScanResult {
        public final int score;
        public final String riskLevel;
        public final String recommendations;

        public EmailScanResult(int score, String riskLevel, String recommendations) {
            this.score = score;
            this.riskLevel = riskLevel;
            this.recommendations = recommendations;
        }
    }
}
