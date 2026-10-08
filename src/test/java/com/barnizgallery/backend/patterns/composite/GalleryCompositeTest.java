package com.barnizgallery.backend.patterns.composite;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.dto.GalleryNodeResponse;
import com.barnizgallery.backend.enums.ArtworkStatus;

class GalleryCompositeTest {

    @Test
    void leafCountsItself() {
        ArtworkLeaf leaf = new ArtworkLeaf(1, "Obra", "Artwork", ArtworkStatus.IN_AUCTION, null, null);

        assertThat(leaf.countArtworks()).isEqualTo(1);
        assertThat(leaf.countAvailableForAuction()).isEqualTo(1);
    }

    @Test
    void compositesAddTheCountsOfTheirChildren() {
        RoomComposite room1 = new RoomComposite(1, "Sala 1", "Room 1", null, 1, "Master 1");
        room1.add(new ArtworkLeaf(1, "A", "A", ArtworkStatus.EXHIBITED, null, null));
        room1.add(new ArtworkLeaf(2, "B", "B", ArtworkStatus.IN_AUCTION, null, null));
        RoomComposite room2 = new RoomComposite(2, "Sala 2", "Room 2", null, 2, "Master 2");
        room2.add(new ArtworkLeaf(3, "C", "C", ArtworkStatus.SOLD, null, null));
        RoomComposite emptyRoom = new RoomComposite(3, "Sala 3", "Room 3", null, 3, "Master 3");
        GalleryComposite gallery = new GalleryComposite();
        gallery.add(room1);
        gallery.add(room2);
        gallery.add(emptyRoom);

        assertThat(room1.countArtworks()).isEqualTo(2);
        assertThat(emptyRoom.countArtworks()).isZero();
        assertThat(gallery.countArtworks()).isEqualTo(3);
        assertThat(gallery.countAvailableForAuction()).isEqualTo(1);
    }

    @Test
    void toNodeBuildsTheTreeRecursively() {
        RoomComposite room = new RoomComposite(1, "Sala", "Room", "scene.glb", 7, "Master");
        room.add(new ArtworkLeaf(5, "Obra", "Artwork", ArtworkStatus.EXHIBITED, "model.glb", "thumb.jpg"));
        GalleryComposite gallery = new GalleryComposite();
        gallery.add(room);

        GalleryNodeResponse root = gallery.toNode();

        assertThat(root.type()).isEqualTo("GALLERY");
        assertThat(root.artworkCount()).isEqualTo(1);
        GalleryNodeResponse roomNode = root.children().get(0);
        assertThat(roomNode.type()).isEqualTo("ROOM");
        assertThat(roomNode.masterId()).isEqualTo(7);
        assertThat(roomNode.scene3dUrl()).isEqualTo("scene.glb");
        GalleryNodeResponse artworkNode = roomNode.children().get(0);
        assertThat(artworkNode.type()).isEqualTo("ARTWORK");
        assertThat(artworkNode.status()).isEqualTo("EXHIBITED");
        assertThat(artworkNode.glbFileUrl()).isEqualTo("model.glb");
        assertThat(artworkNode.thumbnailUrl()).isEqualTo("thumb.jpg");
        assertThat(artworkNode.children()).isNull();
    }
}
