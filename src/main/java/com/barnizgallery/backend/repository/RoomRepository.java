package com.barnizgallery.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.model.entity.Room;

/**
 * Spring Data repository for {@link Room}.
 */
public interface RoomRepository extends JpaRepository<Room, Integer> {

    /** All rooms with their master loaded in the same query (avoids N+1). */
    @Query("select r from Room r join fetch r.master order by r.roomId")
    List<Room> findAllWithMaster();

    @Query("select r from Room r join fetch r.master where r.roomId = :roomId")
    Optional<Room> findByIdWithMaster(Integer roomId);

    Optional<Room> findByMasterMasterId(Integer masterId);

    boolean existsByMasterMasterId(Integer masterId);
}
