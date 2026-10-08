package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.enums.ArtworkStatus;

/**
 * Spring Data repository for {@link Artwork}.
 */
public interface ArtworkRepository extends JpaRepository<Artwork, Integer> {

    List<Artwork> findAllByOrderByArtworkIdAsc();

    List<Artwork> findByRoomRoomIdOrderByArtworkIdAsc(Integer roomId);

    List<Artwork> findByStatusOrderByArtworkIdAsc(ArtworkStatus status);

    List<Artwork> findByRoomRoomIdAndStatusOrderByArtworkIdAsc(Integer roomId, ArtworkStatus status);

    long countByRoomRoomId(Integer roomId);

    /** Artwork count of every room in a single query. */
    @Query("select a.room.roomId as roomId, count(a) as artworkCount from Artwork a group by a.room.roomId")
    List<RoomArtworkCount> countGroupedByRoom();
}
