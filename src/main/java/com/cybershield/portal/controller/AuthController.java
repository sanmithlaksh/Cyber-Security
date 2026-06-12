package com.cybershield.portal.controller;

import com.cybershield.portal.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            if ("unauthorized".equals(error)) {
                model.addAttribute("errorMessage", "Access Denied: Please log in first.");
            } else {
                model.addAttribute("errorMessage", "Invalid Email or Password.");
            }
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have logged out successfully.");
        }
        return "login";
    }

    @PostMapping("/api/auth/login")
    public String handleLogin(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              HttpServletResponse response,
                              Model model) {
        String token = authService.loginUser(email, password);
        if (token != null) {
            // Put JWT in HTTP-Only Cookie
            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Change to true in production with HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 day
            response.addCookie(cookie);

            // Redirect based on role check? Or general dashboard that forwards appropriately
            return "redirect:/dashboard";
        }
        return "redirect:/login?error=true";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        return "register";
    }

    @PostMapping("/api/auth/register")
    public String handleRegister(@RequestParam("name") String name,
                                 @RequestParam("email") String email,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("password") String password,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 @RequestParam(value = "role", defaultValue = "ROLE_USER") String role,
                                 Model model) {
        String result = authService.registerUser(name, email, phone, password, confirmPassword, role);
        if ("SUCCESS".equals(result)) {
            model.addAttribute("successMessage", "Account created successfully! Please log in.");
            return "login";
        } else {
            model.addAttribute("errorMessage", result);
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            model.addAttribute("role", role);
            return "register";
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpServletResponse response) {
        // Clear HTTP-Only Cookie
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/login?logout=true";
    }
}
