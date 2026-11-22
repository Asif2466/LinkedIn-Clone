package com.asif.linkedin.user_service.controller;

import com.asif.linkedin.user_service.dto.UserResponse;
import com.asif.linkedin.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get user profile
     * X-user-Id = request user id (from api-gateway)
     * userId in path = target user to fetch
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(
            @PathVariable String userId,
            @RequestHeader("X-user-Id") String requestingUserId
    ){
        log.info("Get user profile: {}, requested by: {}",
                userId, requestingUserId);
        return ResponseEntity.ok(userService.getUserProfile(userId, requestingUserId));
    }

    /**
     * update own profile
     * user can update only their own profile
     * @param userId
     * @param requestingUserId
     * @return
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable String userId,
            @RequestHeader("X-user-Id") String requestingUserId,
            @RequestBody UserResponse userResponse
    ){
        if(!userId.equals(requestingUserId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.updateProfile(userId, userResponse));
    }

    @PostMapping("{/{userId}/profile-photo")
    public ResponseEntity<UserResponse> uploadUserProfilePhoto(
            @PathVariable String userId,
            @RequestHeader("X-user-Id") String requestingUserId,
            @RequestParam("file")MultipartFile file
    ){
        if(!userId.equals(requestingUserId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.uploadUserProfilePhoto(userId, file));
    }

    /**
     * Send connection request
     * requesting comes from X-user_Id header -- already validated
     * @param targetUserId
     * @param requestingUserId
     * @return
     */
    @PostMapping("/{targetUserId}/connect}")
    public ResponseEntity<String> sendConnectionRequest(
            @PathVariable String targetUserId,
            @RequestHeader("X-user-Id") String requestingUserId
    ){
        return ResponseEntity.ok(
                userService.sendConnectionRequest(targetUserId, requestingUserId));
    }


    @PutMapping("/connection/{connectionId}/accept")
    public ResponseEntity<String> acceptConnectionRequest(
            @PathVariable String connectionId,
            @RequestHeader("X-user-Id") String requestingUserId
    ){
        return ResponseEntity.ok(userService.acceptConnectionRequest(
                connectionId, requestingUserId
        ));
    }

    @GetMapping("/connection/pending")
    public ResponseEntity<List<?>> getPendingConnectionRequests(
            @RequestHeader("X-user-Id") String requestingUserId
    ){
        return ResponseEntity.ok(userService.getPendingConnectionRequests(requestingUserId));
    }

    @GetMapping("/{userId}/connections")
    public ResponseEntity<List<UserResponse>> getConnections(
            @PathVariable String userId
    ){
        return ResponseEntity.ok(userService.getConnections(userId));
    }
}
