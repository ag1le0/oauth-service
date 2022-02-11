package com.foxconn.fii.main.data.repository;

import com.foxconn.fii.main.data.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRole(String role);

    List<Role> findBySystemIn(List<String> systems);
}
