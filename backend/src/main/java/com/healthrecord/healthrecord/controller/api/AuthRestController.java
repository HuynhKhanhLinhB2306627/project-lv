package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.dto.RegistrationDto;
import com.healthrecord.healthrecord.entity.RefreshToken;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.service.RefreshTokenService;
import com.healthrecord.healthrecord.service.UserService;
import com.healthrecord.healthrecord.util.JwtUtils;
import com.healthrecord.healthrecord.util.SecurityUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private com.healthrecord.healthrecord.service.HealthProfileService healthProfileService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        User user = SecurityUtils.getCurrentUser();
        
        if (user == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                user = userService.findByEmail(email);
            }
        }

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Vui lòng đăng nhập lại"));
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("fullName", user.getFullName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole().name());
        data.put("phoneNumber", user.getPhoneNumber());

        
        try {
            com.healthrecord.healthrecord.entity.HealthProfile personalProfile = healthProfileService.findPersonalProfile();
            if (personalProfile != null) {
                data.put("avatar", personalProfile.getAvatar());
                
                data.put("fullName", personalProfile.getFullName());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy avatar cho user me: " + e.getMessage());
        }
        
        return ResponseEntity.ok(data);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpServletResponse response) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu tài khoản hoặc mật khẩu"));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userService.findByEmail(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Không tìm thấy người dùng sau xác thực"));
            }

            String accessToken = jwtUtils.generateToken(username);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

            
            addRefreshTokenToCookie(response, refreshToken.getToken());

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("fullName", user.getFullName());
            userData.put("role", user.getRole().name());

            com.healthrecord.healthrecord.entity.HealthProfile personalProfile = healthProfileService.findPersonalProfile();
            if (personalProfile != null && personalProfile.getAvatar() != null) {
                userData.put("avatar", personalProfile.getAvatar());
            }

            return ResponseEntity.ok(Map.of(
                "success", true, 
                "accessToken", accessToken,
                "user", userData
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Sai email hoặc mật khẩu"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationDto registrationDto) {
        try {
            userService.registerUser(registrationDto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đăng ký thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String requestRefreshToken = extractRefreshTokenFromCookie(request);
        
        if (requestRefreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Thiếu Refresh Token"));
        }

        try {
            
            RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(requestRefreshToken);
            User user = newRefreshToken.getUser();
            
            String newAccessToken = jwtUtils.generateToken(user.getEmail());
            
            
            addRefreshTokenToCookie(response, newRefreshToken.getToken());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "accessToken", newAccessToken
            ));
        } catch (Exception e) {
            
            clearRefreshTokenCookie(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Phiên đăng nhập đã hết hạn: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        String requestRefreshToken = extractRefreshTokenFromCookie(request);
        
        if (requestRefreshToken != null) {
            refreshTokenService.deleteByToken(requestRefreshToken);
        }
        
        clearRefreshTokenCookie(response);
        SecurityContextHolder.clearContext();
        
        return ResponseEntity.ok(Map.of("success", true, "message", "Đăng xuất thành công"));
    }

    

    private void addRefreshTokenToCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)    
                .path("/")
                .maxAge(refreshExpiration / 1000)
                .sameSite("None") 
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
