package com.foxconn.fii.main.data.repository;

import com.foxconn.fii.main.data.entity.Department;
import com.foxconn.fii.main.data.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    List<Department> findByOuCodeAndLevel(String ouCode, String level);
}
