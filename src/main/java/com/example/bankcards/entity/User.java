package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name")
    String name;

    @Column(name = "surname")
    String surname;

    @Column(name = "email", length = 255, unique = true, nullable = false)
    String email;

    @Column(name = "password")
    String password;

    @Column(name = "enabled")
    @ColumnDefault("true")
    Boolean enabled;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "authorities_id", referencedColumnName = "id")
    Authority authority;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    List<Card> cards = new ArrayList<>();
}
