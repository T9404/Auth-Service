package com.example.messenger.entity.user;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users_info")
public class UserInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "names")
    private String name;
    @Column(name = "surnames")
    private String surname;
    @Column(name = "emails")
    private String email;
    @Column(name = "statuses")
    private String status;
    @Column(name = "last_onlines")
    private Date lastOnline;

    @OneToOne(mappedBy = "userInfoEntity", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id")
    private UserEntity userEntity;
}
