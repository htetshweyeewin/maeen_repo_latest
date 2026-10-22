package com.jubosongho.membership.repository;

import com.jubosongho.membership.domain.AdminUser;
import com.jubosongho.membership.domain.OrgZila;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);

    boolean existsByUsername(String username);

    List<AdminUser> findAllByOrderByRoleAscUsernameAsc();

    boolean existsByOrgZila(OrgZila orgZila);
}
