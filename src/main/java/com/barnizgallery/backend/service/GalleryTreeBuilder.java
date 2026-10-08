package com.barnizgallery.backend.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Photo;
import com.barnizgallery.backend.entity.Room;
import com.barnizgallery.backend.entity.ThreeDModel;
import com.barnizgallery.backend.enums.GenerationStatus;
import com.barnizgallery.backend.patterns.composite.ArtworkLeaf;
import com.barnizgallery.backend.patterns.composite.GalleryComposite;
import com.barnizgallery.backend.patterns.composite.RoomComposite;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.PhotoRepository;
import com.barnizgallery.backend.repository.RoomRepository;
import com.barnizgallery.backend.repository.ThreeDModelRepository;

/**
 * Builds the Composite tree Gallery → Rooms → Artworks from the database.
 * <p>
 * It always runs 4 queries (rooms with masters, artworks, first photos, completed models),
 * whatever the number of rooms or artworks, so there is no N+1 problem.
 * Must be called inside a transaction.
 */
@Component
public class GalleryTreeBuilder {

    private final RoomRepository roomRepository;
    private final ArtworkRepository artworkRepository;
    private final PhotoRepository photoRepository;
    private final ThreeDModelRepository threeDModelRepository;

    public GalleryTreeBuilder(RoomRepository roomRepository, ArtworkRepository artworkRepository,
            PhotoRepository photoRepository, ThreeDModelRepository threeDModelRepository) {
        this.roomRepository = roomRepository;
        this.artworkRepository = artworkRepository;
        this.photoRepository = photoRepository;
        this.threeDModelRepository = threeDModelRepository;
    }

    public GalleryComposite build() {
        Map<Integer, String> thumbnails = photoRepository.findFirstPhotoOfEachArtwork().stream()
                .collect(Collectors.toMap(p -> p.getArtwork().getArtworkId(), Photo::getFileUrl));
        Map<Integer, String> glbUrls = threeDModelRepository.findByGenerationStatus(GenerationStatus.COMPLETED)
                .stream()
                .filter(m -> m.getGlbFileUrl() != null)
                .collect(Collectors.toMap(m -> m.getArtwork().getArtworkId(), ThreeDModel::getGlbFileUrl));

        GalleryComposite gallery = new GalleryComposite();
        Map<Integer, RoomComposite> rooms = new LinkedHashMap<>();
        for (Room room : roomRepository.findAllWithMaster()) {
            RoomComposite node = new RoomComposite(room.getRoomId(), room.getNameEs(), room.getNameEn(),
                    room.getScene3dUrl(), room.getMaster().getMasterId(), room.getMaster().getName());
            rooms.put(room.getRoomId(), node);
            gallery.add(node);
        }
        for (Artwork artwork : artworkRepository.findAllByOrderByArtworkIdAsc()) {
            RoomComposite room = rooms.get(artwork.getRoom().getRoomId());
            if (room != null) {
                room.add(new ArtworkLeaf(artwork.getArtworkId(), artwork.getTitleEs(), artwork.getTitleEn(),
                        artwork.getStatus(), glbUrls.get(artwork.getArtworkId()),
                        thumbnails.get(artwork.getArtworkId())));
            }
        }
        return gallery;
    }
}
