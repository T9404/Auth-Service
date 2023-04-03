package com.example.messenger.entity.user;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users_properties")
public class UserPropertiesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "count_messages")
    @Builder.Default
    private Long countMessages = 0L;
    @Column(name = "is_email_verified")
    @Builder.Default
    private Boolean isEmailVerified = false;
    @Column(name = "is_phone_number_verified")
    @Builder.Default
    private Boolean isPhoneNumberVerified = false;
    @Column(name = "is_oath_verified")
    @Builder.Default
    private Boolean isOathVerified = false;
    @Column(name = "is_blocked")
    @Builder.Default
    private Boolean isBlocked = false;
    @Column(name = "is_accepted_message")
    @Builder.Default
    private Boolean isAcceptedMessage = true;
    @Column(name = "is_accepted_video_call")
    @Builder.Default
    private Boolean isAcceptedVideoCall = true;
    @Column(name = "is_accepted_voice_call")
    @Builder.Default
    private Boolean isAcceptedVoiceCall = true;

    @OneToOne(mappedBy = "userPropertiesEntity", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id")
    private UserEntity userEntity;
}
