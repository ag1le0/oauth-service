package com.foxconn.fii.main.data.repository;

import com.foxconn.fii.main.data.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u JOIN  u.roles r WHERE r.role = :role")
    Page<User> findByRole(String role, Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u JOIN  u.roles r WHERE r.role in :role")
    Page<User> findByRoleIn(List<String> role, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE User SET failedLoginNumber = failedLoginNumber + 1 WHERE username = :username")
    void increasingFailedLoginNumber(@Param("username") String username);

    @Modifying
    @Transactional
    @Query("UPDATE User SET failedLoginNumber = 0 WHERE username = :username")
    void resetFailedLoginNumber(@Param("username") String username);

    List<User> findByLockedIsTrue();
}
