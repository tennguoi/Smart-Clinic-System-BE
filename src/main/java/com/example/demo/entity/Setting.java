package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "setting_id")
    private UUID settingId;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "language", nullable = false)
    @Builder.Default
    private String language = "vi";

    @Column(name = "theme", nullable = false)
    @Builder.Default
    private String theme = "light";

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;
}
