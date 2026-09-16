package com.adagency.addanad.modules.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<UserDB, String> {

    Optional<UserDB> findByEmail(String email);

    boolean existsByEmail(String email);
}
