package com.jubosongho.membership.repository;

import com.jubosongho.membership.domain.OrgZila;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrgZilaRepository extends JpaRepository<OrgZila, Long> {

    List<OrgZila> findAllByOrderByNameAsc();

    Optional<OrgZila> findByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
