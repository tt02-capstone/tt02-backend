package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>{
}


