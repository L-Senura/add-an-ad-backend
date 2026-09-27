package com.adagency.addanad.modules.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepo extends JpaRepository<ClientDB, Long> {

    Optional<ClientDB> findByEmail(String email);

    List<ClientDB> findByStatus(String status);

    boolean existsByEmail(String email);
}
