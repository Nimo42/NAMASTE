package com.ayush.terminology.namaste.auth.controller;

import com.ayush.terminology.namaste.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> data) {
        try {
            Map<String, Object> result = authService.registerUser(data);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/register/google-complete")
    public ResponseEntity<?> googleCompleteRegistration(@RequestBody Map<String, Object> data) {
        try {
            Map<String, Object> result = authService.completeGoogleRegistration(data);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data) {
        try {
            String token = authService.loginUser(data.get("email"), data.get("password"));
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> data) {
        try {
            Map<String, Object> result = authService.loginWithGoogleIdToken(data.get("idToken"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> data) {
        try {
            authService.createPasswordResetToken(data.get("email"));
            return ResponseEntity.ok("Reset link sent successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password/update")
    public ResponseEntity<?> forgotPasswordUpdate(@RequestBody Map<String, String> data) {
        try {
            Map<String, String> result = authService.resetPasswordByEmail(
                    data.get("email"), data.get("password"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> data) {
        try {
            Map<String, String> result = authService.resetPassword(
                    data.get("token"), data.get("password"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            String email = authentication.getName();
            Map<String, Object> user = authService.getCurrentUserByEmail(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(Authentication authentication,
                                           @RequestBody Map<String, Object> data) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            String email = authentication.getName();
            Map<String, Object> updated = authService.updateProfile(email, data);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication,
                                            @RequestBody Map<String, String> data) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            String email = authentication.getName();
            Map<String, String> result = authService.changePassword(
                    email, data.get("currentPassword"), data.get("newPassword"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(Authentication authentication,
                                           @RequestBody Map<String, String> data) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            String email = authentication.getName();
            Map<String, String> result = authService.deleteAccountByEmail(email, data.get("password"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}