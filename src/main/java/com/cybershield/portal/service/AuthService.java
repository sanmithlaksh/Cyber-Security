package com.cybershield.portal.service;

import com.cybershield.portal.model.User;
import com.cybershield.portal.repository.UserRepository;
import com.cybershield.portal.util.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // Password regex: At least 8 characters, 1 digit, 1 uppercase, 1 special character
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$");
    // Phone regex: 10 to 15 digits, allows optional leading +
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public String registerUser(String name, String email, String phone, String password, String confirmPassword, String role) {
        if (name == null || name.trim().isEmpty()) {
            return "Name cannot be empty.";
        }
        if (email == null || email.trim().isEmpty()) {
            return "Email cannot be empty.";
        }
        if (userRepository.existsByEmail(email)) {
            return "Email is already registered.";
        }
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            return "Invalid phone number format. Use 10-15 digits.";
        }
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            return "Password must be at least 8 characters long, contain an uppercase letter, a digit, and a special character.";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }

        // Set default role if not specified or empty
        String finalRole = (role == null || role.trim().isEmpty()) ? "ROLE_USER" : role;
        
        User newUser = new User(
                name.trim(),
                email.trim().toLowerCase(),
                phone.trim(),
                passwordEncoder.encode(password),
                finalRole
        );
        userRepository.save(newUser);
        return "SUCCESS";
    }

    public String loginUser(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null; // Invalid input
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Return generated JWT
                return jwtUtils.generateToken(user.getEmail(), user.getName(), user.getRole());
            }
        }
        return null; // Invalid credentials
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
