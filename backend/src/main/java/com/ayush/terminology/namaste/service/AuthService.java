package com.ayush.terminology.namaste.service;

import com.ayush.terminology.namaste.auth.config.JwtUtil;
import com.ayush.terminology.namaste.model.AppUser;
import com.ayush.terminology.namaste.model.ResetToken;
import com.ayush.terminology.namaste.model.Role;
import com.ayush.terminology.namaste.repo.ResetTokenRepository;
import com.ayush.terminology.namaste.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResetTokenRepository resetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> registerUser(Map<String, Object> data) {
        String email = ((String) data.get("email")).toLowerCase().trim();
        String password = (String) data.get("password");
        String name = (String) data.get("name");

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        String licenseNumber = (String) data.get("licenseNumber");
        if (licenseNumber != null && !licenseNumber.isEmpty()) {
            if (userRepository.findByLicenseNumber(licenseNumber).isPresent()) {
                throw new RuntimeException("License number already registered");
            }
        }

        AppUser user = AppUser.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .gender((String) data.get("gender"))
                .hospital((String) data.get("hospital"))
                .address((String) data.get("address"))
                .phone((String) data.get("phone"))
                .licenseNumber(licenseNumber)
                .registrationStatus("APPROVED")
                .enabled(true)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return Map.of("token", token, "message", "Registration successful");
    }

    public String loginUser(String email, String password) {
        AppUser user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (user.getPassword() == null) {
            throw new RuntimeException("Please login with Google");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is disabled");
        }

        return jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
    }

    public Map<String, Object> loginWithGoogleIdToken(String idToken) {
        // Extract email/name from the Google ID token payload (JWT)
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) throw new RuntimeException("Invalid token");

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            String email = extractJsonValue(payload, "email");
            String name = extractJsonValue(payload, "name");

            if (email == null) throw new RuntimeException("Email not found in token");

            Optional<AppUser> existing = userRepository.findByEmail(email.toLowerCase());

            if (existing.isPresent()) {
                AppUser user = existing.get();
                if (!user.isEnabled()) throw new RuntimeException("Account is disabled");

                String jwt = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
                return Map.of("token", jwt);
            } else {
                // Frontend expects: { registrationRequired, setupToken, prefill: { email, name } }
                Map<String, Object> prefill = new LinkedHashMap<>();
                prefill.put("email", email);
                prefill.put("name", name != null ? name : "");

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("registrationRequired", true);
                result.put("setupToken", idToken); // pass the original token as setupToken
                result.put("prefill", prefill);
                return result;
            }
        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }

    public Map<String, Object> completeGoogleRegistration(Map<String, Object> data) {
        // The frontend sends setupToken (the original Google ID token)
        String setupToken = (String) data.get("setupToken");
        String email = (String) data.get("email");
        String name = (String) data.get("name");

        // Extract email from setupToken if not provided directly
        if ((email == null || email.isEmpty()) && setupToken != null) {
            try {
                String[] parts = setupToken.split("\\.");
                if (parts.length >= 2) {
                    String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                    email = extractJsonValue(payload, "email");
                    if (name == null || name.isEmpty()) {
                        name = extractJsonValue(payload, "name");
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Invalid setup token");
            }
        }

        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        email = email.toLowerCase().trim();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        String licenseNumber = (String) data.get("licenseNumber");
        if (licenseNumber != null && !licenseNumber.isEmpty()) {
            if (userRepository.findByLicenseNumber(licenseNumber).isPresent()) {
                throw new RuntimeException("License number already registered");
            }
        }

        AppUser user = AppUser.builder()
                .name(name != null ? name : "")
                .email(email)
                .password(null)
                .role(Role.USER)
                .gender((String) data.get("gender"))
                .hospital((String) data.get("hospital"))
                .address((String) data.get("address"))
                .phone((String) data.get("phone"))
                .licenseNumber(licenseNumber)
                .oauthProvider("google")
                .registrationStatus("APPROVED")
                .enabled(true)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return Map.of("token", token);
    }

    public void createPasswordResetToken(String email) {
        AppUser user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        // Delete existing tokens
        resetTokenRepository.deleteByUserId(user.getId());

        String tokenStr = UUID.randomUUID().toString();
        ResetToken resetToken = ResetToken.builder()
                .token(tokenStr)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        resetTokenRepository.save(resetToken);
        // In production, send email with reset link
    }

    @Transactional
    public Map<String, String> resetPasswordByEmail(String email, String password) {
        AppUser user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        return Map.of("message", "Password updated successfully");
    }

    @Transactional
    public Map<String, String> resetPassword(String token, String password) {
        ResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }

        AppUser user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        resetTokenRepository.delete(resetToken);

        return Map.of("message", "Password reset successfully");
    }

    public Map<String, Object> getCurrentUserByEmail(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("role", user.getRole().name());
        result.put("gender", user.getGender());
        result.put("hospital", user.getHospital());
        result.put("address", user.getAddress());
        result.put("phone", user.getPhone());
        result.put("licenseNumber", user.getLicenseNumber());
        result.put("registrationStatus", user.getRegistrationStatus());
        result.put("oauthProvider", user.getOauthProvider());
        result.put("createdAt", user.getCreatedAt());
        return result;
    }

    @Transactional
    public Map<String, Object> updateProfile(String email, Map<String, Object> data) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (data.containsKey("name")) user.setName((String) data.get("name"));
        if (data.containsKey("gender")) user.setGender((String) data.get("gender"));
        if (data.containsKey("hospital")) user.setHospital((String) data.get("hospital"));
        if (data.containsKey("address")) user.setAddress((String) data.get("address"));
        if (data.containsKey("phone")) user.setPhone((String) data.get("phone"));

        userRepository.save(user);
        return getCurrentUserByEmail(email);
    }

    public Map<String, String> changePassword(String email, String currentPassword, String newPassword) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPassword() == null) {
            throw new RuntimeException("Cannot change password for OAuth accounts");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return Map.of("message", "Password changed successfully");
    }

    @Transactional
    public Map<String, String> deleteAccountByEmail(String email, String password) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPassword() != null && !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }

        userRepository.delete(user);
        return Map.of("message", "Account deleted successfully");
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int idx = json.indexOf(searchKey);
        if (idx == -1) return null;
        int colonIdx = json.indexOf(":", idx);
        if (colonIdx == -1) return null;
        int startQuote = json.indexOf("\"", colonIdx + 1);
        if (startQuote == -1) return null;
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return null;
        return json.substring(startQuote + 1, endQuote);
    }
}
