package com.cybershield.portal.config;

import com.cybershield.portal.model.QuizQuestion;
import com.cybershield.portal.model.ThreatIntel;
import com.cybershield.portal.model.User;
import com.cybershield.portal.repository.QuizQuestionRepository;
import com.cybershield.portal.repository.ThreatIntelRepository;
import com.cybershield.portal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ThreatIntelRepository intelRepository;
    private final QuizQuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository, ThreatIntelRepository intelRepository,
                               QuizQuestionRepository questionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.intelRepository = intelRepository;
        this.questionRepository = questionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Users
        if (userRepository.count() == 0) {
            User admin = new User(
                    "System Administrator",
                    "admin@cybershield.com",
                    "+19999999999",
                    passwordEncoder.encode("Admin@123"),
                    "ROLE_ADMIN"
            );
            User analyst = new User(
                    "Cyber Threat Analyst",
                    "analyst@cybershield.com",
                    "+18888888888",
                    passwordEncoder.encode("Analyst@123"),
                    "ROLE_ANALYST"
            );
            User standardUser = new User(
                    "John Doe",
                    "user@cybershield.com",
                    "+17777777777",
                    passwordEncoder.encode("User@123"),
                    "ROLE_USER"
            );

            for (User u : Arrays.asList(admin, analyst, standardUser)) {
                userRepository.save(u);
            }
            System.out.println(">>> Database Seeded: Default Users Created.");
            System.out.println("    Admin: admin@cybershield.com / Admin@123");
            System.out.println("    Analyst: analyst@cybershield.com / Analyst@123");
            System.out.println("    User: user@cybershield.com / User@123");
        }

        // 2. Seed Threat Intel Database
        if (intelRepository.count() == 0) {
            List<ThreatIntel> threats = Arrays.asList(
                    new ThreatIntel(
                            "WhatsApp OTP Scam",
                            "Social Engineering",
                            "HIGH",
                            "Scammers pose as friends or support representatives asking for a 6-digit WhatsApp verification code sent to your phone. Sharing it compromises your account.",
                            "Never share verification PINs/OTPs. Enable 2-step verification inside WhatsApp settings.",
                            LocalDate.now().minusDays(10).toString()
                    ),
                    new ThreatIntel(
                            "Fake Banking Website",
                            "Phishing",
                            "CRITICAL",
                            "Spoofed login pages mimicking legitimate banking interfaces (e.g. secure-bank-login.xyz) designed to capture account credentials and high-risk transfer tokens.",
                            "Always verify the URL matches your bank's exact domain. Use banking apps instead of search engine ads.",
                            LocalDate.now().minusDays(5).toString()
                    ),
                    new ThreatIntel(
                            "QR Code Payment Fraud",
                            "Financial Scams",
                            "MEDIUM",
                            "Victims are sent a QR code under the guise of receiving payment. Scanning the code and entering their UPI PIN actually deducts money from their account.",
                            "Remember: You NEVER need to enter your PIN or scan a QR code to RECEIVE money.",
                            LocalDate.now().minusDays(2).toString()
                    ),
                    new ThreatIntel(
                            "Cryptocurrency Investment Scam",
                            "Financial Scams",
                            "CRITICAL",
                            "Fake high-yield investment web portals (such as double-your-crypto.cc) claiming automated high returns, locking deposited funds permanently.",
                            "Avoid platforms promising guaranteed returns. Research platform registrations with regulators.",
                            LocalDate.now().minusDays(8).toString()
                    ),
                    new ThreatIntel(
                            "Fake Job Recruitment Scam",
                            "Social Engineering",
                            "MEDIUM",
                            "Victims receive SMS or WhatsApp messages offering high-paying part-time remote work. They are asked to pay processing fees or deposit capital.",
                            "Never pay to get a job. Be wary of recruitment conducted entirely over WhatsApp/Telegram.",
                            LocalDate.now().minusDays(1).toString()
                    )
            );
            intelRepository.saveAll(threats);
            System.out.println(">>> Database Seeded: Threat Intel Database populated.");
        }

        // 3. Seed Quiz Questions
        if (questionRepository.count() == 0) {
            List<QuizQuestion> questions = Arrays.asList(
                    // Phishing Awareness
                    new QuizQuestion(
                            "Phishing",
                            "What is the primary indicator of a phishing email?",
                            "It contains an attachment with a generic title",
                            "Urgent language demanding immediate credential verification or payment action",
                            "It was sent outside normal business hours",
                            "The email uses a blue background color",
                            "B"
                    ),
                    new QuizQuestion(
                            "Phishing",
                            "If you receive an email from 'support@paypa1-update.com' asking you to reset your password, what should you do?",
                            "Click the link immediately to secure your account",
                            "Reply to the email and ask if they are genuine",
                            "Ignore it and report it; paypa1 is a typo-squatted fake domain designed to mimic PayPal",
                            "Reset your password but use a weak one temporarily",
                            "C"
                    ),
                    new QuizQuestion(
                            "Phishing",
                            "Which of the following is target-specific phishing aimed at high-profile executives?",
                            "Smishing",
                            "Vishing",
                            "Spear Phishing",
                            "Whaling",
                            "D"
                    ),

                    // Password Security
                    new QuizQuestion(
                            "Passwords",
                            "Which of the following describes a strong password policy?",
                            "Using your birthday combined with your initials",
                            "A password of at least 12 characters combining uppercase, lowercase, numbers, and symbols, changed occasionally",
                            "Using a common word but replacing 's' with '$'",
                            "Writing your password on a sticky note under your keyboard",
                            "B"
                    ),
                    new QuizQuestion(
                            "Passwords",
                            "What is the safest way to manage multiple strong passwords for all your online accounts?",
                            "Write them down in a notebook kept on your desk",
                            "Use a secure password manager that generates and encrypts unique passwords",
                            "Use the same password with small variations for every website",
                            "Store them in an unencrypted Word document on your desktop",
                            "B"
                    ),
                    new QuizQuestion(
                            "Passwords",
                            "What does Multi-Factor Authentication (MFA) provide?",
                            "Faster logins",
                            "An additional layer of defense, making accounts hard to breach even if the password is stolen",
                            "Automatic password updates every 30 days",
                            "Direct backup of your local files to the cloud",
                            "B"
                    ),

                    // Social Engineering
                    new QuizQuestion(
                            "Social Engineering",
                            "What is the term for a scammer calling you on the phone, posing as a bank agent to steal your OTP?",
                            "Smishing (SMS Phishing)",
                            "Vishing (Voice Phishing)",
                            "Baiting",
                            "Tailgating",
                            "B"
                    ),
                    new QuizQuestion(
                            "Social Engineering",
                            "A coworker asks you to hold the secured entry door open for them because they forgot their badge. What type of physical security risk is this?",
                            "Phishing",
                            "Pretexting",
                            "Tailgating",
                            "Shoulder Surfing",
                            "C"
                    ),
                    new QuizQuestion(
                            "Social Engineering",
                            "What is 'Baiting' in social engineering?",
                            "Leaving malware-infected USB drives in public areas hoping someone plugs them in",
                            "Sending threat emails with malicious invoice links",
                            "Creating a fake Wi-Fi hotspot in a coffee shop",
                            "Calling someone posing as tech support",
                            "A"
                    ),

                    // Network Security
                    new QuizQuestion(
                            "Network",
                            "What is the primary function of a firewall in network security?",
                            "To back up local hard drive data to remote servers",
                            "To inspect and control incoming and outgoing network traffic based on security rules",
                            "To scan files on a computer for viruses",
                            "To encrypt email communications",
                            "B"
                    ),
                    new QuizQuestion(
                            "Network",
                            "Why is it risky to log into personal banking apps on public Wi-Fi hotspots (like at airports or cafes)?",
                            "Public Wi-Fi is too slow to load banking portals",
                            "Hackers can intercept unencrypted data packets using 'Man-in-the-Middle' attacks",
                            "Your device battery will drain faster",
                            "Public Wi-Fi networks charge hidden fees for banking sites",
                            "B"
                    ),
                    new QuizQuestion(
                            "Network",
                            "What security benefit does a Virtual Private Network (VPN) provide?",
                            "It blocks all computer viruses",
                            "It encrypts your internet connection, hiding your traffic from third parties and masking your IP",
                            "It updates your operating system firewall automatically",
                            "It allows you to browse the web without an internet service provider",
                            "B"
                    ),

                    // Safe Browsing
                    new QuizQuestion(
                            "Browsing",
                            "What does 'HTTPS' at the beginning of a URL indicate?",
                            "The website is verified to have no malicious files",
                            "The website is safe, secure, and run by a verified government agency",
                            "The communication between your browser and the server is encrypted",
                            "The website is hosted locally in your country",
                            "C"
                    ),
                    new QuizQuestion(
                            "Browsing",
                            "What is a 'Drive-by Download' in safe web browsing?",
                            "Malware that downloads and installs automatically without user consent when visiting an infected website",
                            "Downloading files while in a moving vehicle",
                            "A file downloaded through an email attachment",
                            "Installing browser extensions from official web stores",
                            "A"
                    ),
                    new QuizQuestion(
                            "Browsing",
                            "Which of the following is a safe practice when downloading files from the web?",
                            "Download from any website as long as the connection is HTTPS",
                            "Turn off your antivirus so the download finishes faster",
                            "Verify the file source and only download from official, reputable domains",
                            "Double-click any downloaded file immediately without checking its extension",
                            "C"
                    )
            );
            questionRepository.saveAll(questions);
            System.out.println(">>> Database Seeded: Security Quiz Questions populated.");
        }
    }
}
