package com.jubosongho.membership.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_user")
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** শুধু ZILA_ADMIN এর জন্য প্রযোজ্য */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "org_zila_id")
    private OrgZila orgZila;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public OrgZila getOrgZila() { return orgZila; }
    public void setOrgZila(OrgZila orgZila) { this.orgZila = orgZila; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
