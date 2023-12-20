package com.gmonitor.storage.entity.configuration;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_token")
public class UserTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String token;

    @Column
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @Column
    private boolean expired;

    @Column
    private boolean revoked;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
