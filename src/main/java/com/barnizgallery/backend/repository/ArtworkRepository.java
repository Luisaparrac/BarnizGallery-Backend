package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.enums.ArtworkStatus;

/**
 * Spring Data repository for {@link Artwork}.
 */
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    List<Artwork> findAllByOrderByArtworkIdAsc();

    List<Artwork> findByRoomRoomIdOrderByArtworkIdAsc(Long roomId);

    List<Artwork> findByStatusOrderByArtworkIdAsc(ArtworkStatus status);

    List<Artwork> findByRoomRoomIdAndStatusOrderByArtworkIdAsc(Long roomId, ArtworkStatus status);

    long countByRoomRoomId(Long roomId);

    /** Artwork count of every room in a single query. */
    @Query("select a.room.roomId as roomId, count(a) as artworkCount from Artwork a group by a.room.roomId")
    List<RoomArtworkCount> countGroupedByRoom();
}
