package cl.metspherical.calbucofelizbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String rut;

    @Column(unique = true)
    private String email;

    @Column(unique = true, nullable = false)
    private String number;

    @Column(unique = true)
    private String username;

    @Column
    private String avatar;

    @Column(nullable = false)
    private String names;

    private String lastNames;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String address;

    @Column
    private String description;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();
}

