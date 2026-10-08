package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.Room;

/**
 * Spring Data repository for {@link Room}.
 */
public interface RoomRepository extends JpaRepository<Room, Integer> {
}
