package com.foxconn.fii.main.data.repository;

import com.foxconn.fii.main.data.entity.User;
import com.foxconn.fii.main.data.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Integer> {

    Optional<UserDevice> findByUserAndMac(User user, String mac);

    List<UserDevice> findByUser(User user);
}
