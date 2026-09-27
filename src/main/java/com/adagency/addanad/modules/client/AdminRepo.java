package com.adagency.addanad.modules.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepo extends JpaRepository<AdminDB, Long> {

    Optional<AdminDB> findByEmail(String email);

    List<AdminDB> findByAdminType(String adminType);

    boolean existsByEmail(String email);
}
