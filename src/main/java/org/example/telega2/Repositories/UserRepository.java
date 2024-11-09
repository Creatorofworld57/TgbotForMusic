package org.example.telega2.Repositories;

import org.example.telega2.Models.UserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<UserState,Long> {
    Optional<UserState> findByChatId(long chatId);
}
