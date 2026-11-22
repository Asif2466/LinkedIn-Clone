package com.asif.linkedin.user_service.service;

import com.asif.linkedin.user_service.dto.UserResponse;
import com.asif.linkedin.user_service.entity.Connection;
import com.asif.linkedin.user_service.entity.ConnectionStatus;
import com.asif.linkedin.user_service.entity.User;
import com.asif.linkedin.user_service.repository.ConnectionRepository;
import com.asif.linkedin.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final ConnectionRepository connectionRepository;
    private final S3Service s3Service;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String CONNECTION_REQUESTED_TOPIC = "connection.request";
    private static final String CONNECTION_ACCEPTED_TOPIC = "connection.accepted";
    private final static String USER_UPDATED = "user.updated";

    public String sendConnectionRequest(String receiverId, String requesterId) {
        if(connectionRepository.existsByRequesterIdAndReceiverId(requesterId, receiverId)){
            throw new RuntimeException("Connection request already sent");
        }

        Connection connection = Connection.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .status(ConnectionStatus.PENDING)
                .build();

        connectionRepository.save(connection);

        //Publish connection.request event
        Map<String,Object> connectionRequestEvent = new HashMap<>();
        connectionRequestEvent.put("requesterId", requesterId);
        connectionRequestEvent.put("receiverId", receiverId);

        kafkaTemplate.send(CONNECTION_REQUESTED_TOPIC, requesterId, connectionRequestEvent);

        log.info("Connection request sent: {} -> {}", requesterId, receiverId);

        return "Connection request sent";
    }

    public List<UserResponse> getPendingConnectionRequests(String requestingUserId) {
        return connectionRepository.findByReceiverIdAndStatus(requestingUserId, ConnectionStatus.PENDING).stream()
                .map(connection -> getUserProfile(connection.getRequesterId(), requestingUserId))
                .collect(Collectors.toList());
    }

    public String acceptConnectionRequest(String connectionId, String requestingUserId) {

        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        connection.setStatus(ConnectionStatus.CONNECTED);
        connectionRepository.save(connection);

        //Publish connection accepted event
        Map<String,Object> connectionAcceptedEvent = new HashMap<>();
        connectionAcceptedEvent.put("requesterId", connection.getRequesterId());
        connectionAcceptedEvent.put("receiverId", connection.getReceiverId());

        kafkaTemplate.send(CONNECTION_ACCEPTED_TOPIC, connection.getRequesterId(), connectionAcceptedEvent);

        return "Connection accepted";

    }

    public List<UserResponse> getConnections(String requesterId) {
        List<Connection> connections = connectionRepository.findByRequesterIdAndStatus(requesterId, ConnectionStatus.CONNECTED);
        //connections.addAll(connectionRepository.findByReceiverIdAndStatus(requesterId, ConnectionStatus.CONNECTED));
        return connections.stream()
                .map(connection -> getUserProfile(connection.getReceiverId(), connection.getRequesterId()))
                .collect(Collectors.toList());

    }

    public UserResponse getUserProfile(String receiverId, String requesterId) {
        User user = userRepository.findById(receiverId).orElseThrow(
                () -> new RuntimeException("User not found: "+receiverId)
        );
        return mapToUserResponse(user);
    }

    public UserResponse updateProfile(String userId, UserResponse request) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found: "+userId)
        );

        user.setAbout(request.about());
        user.setHeadline(request.headline());
        user.setLocation(request.location());
        user.setSkills(request.skills());

        User savedUser = userRepository.save(user);

        //Publish user.updated event
        // Search consumes this event updates the indexes

        Map<String, Object> userUpdatedEvent = new HashMap<>();
        userUpdatedEvent.put("userId", savedUser.getId());
        userUpdatedEvent.put("firstName", savedUser.getFirstName());
        userUpdatedEvent.put("lastName", savedUser.getLastName());
        userUpdatedEvent.put("location", savedUser.getLocation());
        userUpdatedEvent.put("headline", savedUser.getHeadline());
        userUpdatedEvent.put("about", savedUser.getAbout());
        userUpdatedEvent.put("skills", savedUser.getSkills());

        kafkaTemplate.send(USER_UPDATED, savedUser.getId(), userUpdatedEvent);
        log.info("user.updated event published: {}", savedUser.getId());

        return mapToUserResponse(savedUser);

    }


    public UserResponse uploadUserProfilePhoto(String userId, MultipartFile file) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found: "+userId)
        );

        String photoUrl = s3Service.uploadFile(file, "profile/" + userId +"/avatar");

        user.setProfilePhotoUrl(photoUrl);
        User savedUser = userRepository.save(user);

        log.info("user profile photo uploaded: {}", savedUser.getId());

        return mapToUserResponse(savedUser);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getHeadline(), user.getAbout(),
                user.getLocation(), user.getProfilePhotoUrl(), user.getCoverPhotoUrl(), user.getSkills(), user.getCreatedAt());
    }


}
