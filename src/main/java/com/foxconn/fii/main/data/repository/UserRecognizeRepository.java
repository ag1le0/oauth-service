package com.foxconn.fii.main.data.repository;

import com.foxconn.fii.main.data.entity.UserRecognize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRecognizeRepository extends JpaRepository<UserRecognize, Integer> {

}
