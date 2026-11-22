package com.asif.linkedin.user_service.service;

import com.asif.linkedin.user_service.dto.AuthResponse;
import com.asif.linkedin.user_service.dto.LoginRequest;
import com.asif.linkedin.user_service.dto.RegisterRequest;
import com.asif.linkedin.user_service.entity.User;
import com.asif.linkedin.user_service.entity.UserRole;
import com.asif.linkedin.user_service.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final static String USER_CREATED = "user.created";

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user {}", request.email());

        if(userRepository.existByEmail(request.email())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .headline(request.headline())
                .location(request.location())
                .role(UserRole.NORMAL_USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User {} registered successfully", savedUser.getId());

        // Publish user.created event
        // Search service consume this event and indexes user
        Map<String, Object> userCreatedEvent = new HashMap<>();
        userCreatedEvent.put("userId", savedUser.getId());
        userCreatedEvent.put("email", savedUser.getEmail());
        userCreatedEvent.put("firstName", savedUser.getFirstName());
        userCreatedEvent.put("lastName", savedUser.getLastName());
        userCreatedEvent.put("location", savedUser.getLocation());
        userCreatedEvent.put("headline", savedUser.getHeadline());

        kafkaTemplate.send(USER_CREATED, savedUser.getId(), userCreatedEvent);
        log.info("user.created event published: {}", savedUser.getId());

        //Generate token
        String token = generateToken(savedUser.getId(), savedUser.getEmail());

        return maptoAuthResponse(savedUser, token);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found: "+ request.email()));

        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid credentials");
        }

        log.info("User {} logged in successfully", user.getId());

        //Generate token
        String token = generateToken(user.getId(), user.getEmail());

        return maptoAuthResponse(user, token);

    }

    /**
     * Generates access Token
     * @param id
     * @param email
     * @return
     */
    private String generateToken(String id, String email) {
        return Jwts.builder()
                .claim("userId", id)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates refresh token
     * Used to get a new access token when it expires
     * Server validates and return new token
     * Client sends refresh token to /auth/refresh endpoint
     * @param id
     * @return
     */
    private String generateRefreshToken(String id) {
        return Jwts.builder()
                .claim("userId", id)
                .subject(id)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private AuthResponse maptoAuthResponse(User user, String token) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                token,
                generateRefreshToken(user.getId()),
                "Bearer"
        );
    }

}
