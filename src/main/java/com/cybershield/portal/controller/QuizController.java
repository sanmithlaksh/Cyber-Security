package com.cybershield.portal.controller;

import com.cybershield.portal.model.QuizQuestion;
import com.cybershield.portal.model.QuizResult;
import com.cybershield.portal.model.User;
import com.cybershield.portal.repository.QuizQuestionRepository;
import com.cybershield.portal.repository.QuizResultRepository;
import com.cybershield.portal.service.AuthService;
import com.cybershield.portal.service.CertificateGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    private final QuizQuestionRepository questionRepository;
    private final QuizResultRepository resultRepository;
    private final AuthService authService;
    private final CertificateGenerator certificateGenerator;

    public QuizController(QuizQuestionRepository questionRepository, QuizResultRepository resultRepository,
                          AuthService authService, CertificateGenerator certificateGenerator) {
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
        this.authService = authService;
        this.certificateGenerator = certificateGenerator;
    }

    /**
     * Shows quiz category selection.
     */
    @GetMapping
    public String showQuizHome(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        List<QuizResult> pastResults = resultRepository.findByUserOrderByDateCompletedDesc(user);
        model.addAttribute("user", user);
        model.addAttribute("pastResults", pastResults);
        return "user/quiz-home";
    }

    /**
     * Starts quiz for a specific category.
     */
    @GetMapping("/{category}")
    public String startQuiz(@PathVariable("category") String category, Principal principal, Model model) {
        List<QuizQuestion> questions = questionRepository.findByCategory(category);
        if (questions.isEmpty()) {
            return "redirect:/quiz";
        }
        model.addAttribute("category", category);
        model.addAttribute("questions", questions);
        model.addAttribute("user", getCurrentUser(principal));
        return "user/quiz-run";
    }

    /**
     * Handles quiz scoring and evaluation.
     */
    @PostMapping("/submit")
    public String handleQuizSubmission(@RequestParam("category") String category,
                                       HttpServletRequest request,
                                       Principal principal,
                                       Model model) {
        User user = getCurrentUser(principal);
        List<QuizQuestion> questions = questionRepository.findByCategory(category);

        int correctCount = 0;
        int totalQuestions = questions.size();

        for (QuizQuestion q : questions) {
            String paramName = "answer_" + q.getId();
            String userAnswer = request.getParameter(paramName);
            if (userAnswer != null && userAnswer.trim().equalsIgnoreCase(q.getCorrectAnswer().trim())) {
                correctCount++;
            }
        }

        int scorePercentage = (totalQuestions > 0) ? (correctCount * 100) / totalQuestions : 0;
        boolean passed = scorePercentage >= 80;

        String certUuid = null;
        if (passed) {
            certUuid = UUID.randomUUID().toString();
        }

        QuizResult result = new QuizResult(
                user,
                category,
                scorePercentage,
                totalQuestions,
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                certUuid
        );
        resultRepository.save(result);

        model.addAttribute("score", scorePercentage);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("totalCount", totalQuestions);
        model.addAttribute("passed", passed);
        model.addAttribute("certUuid", certUuid);
        model.addAttribute("user", user);
        model.addAttribute("category", category);

        return "user/quiz-result";
    }

    /**
     * Streams the generated PDF certificate.
     */
    @GetMapping("/certificate/{uuid}")
    public void downloadCertificate(@PathVariable("uuid") String uuid, HttpServletResponse response) {
        Optional<QuizResult> resultOpt = resultRepository.findByCertificateUuid(uuid);
        if (resultOpt.isPresent()) {
            QuizResult result = resultOpt.get();
            try {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=Certificate_" + result.getCategory() + ".pdf");
                
                OutputStream out = response.getOutputStream();
                certificateGenerator.generateCertificate(result, out);
                out.flush();
            } catch (Exception e) {
                System.err.println("Could not generate certificate: " + e.getMessage());
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        return authService.findUserByEmail(principal.getName()).orElse(null);
    }
}
