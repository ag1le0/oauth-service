package com.foxconn.fii.main.data.repository;

import com.foxconn.fii.main.data.entity.User;
import com.foxconn.fii.main.data.entity.UserSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSystemRepository extends JpaRepository<UserSystem, Integer> {

    List<UserSystem> findByUser(User user);

    @Query("SELECT us.system FROM UserSystem us WHERE us.user = :user")
    List<String> findSystemByUser(User user);

    Optional<UserSystem> findByUserAndSystem(User user, String system);
}
