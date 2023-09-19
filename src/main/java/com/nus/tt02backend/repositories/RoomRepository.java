package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoomRepository extends JpaRepository<Room, Long>{
}


