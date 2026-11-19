package com.barnizgallery.backend.patterns.composite;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.dto.GalleryNodeResponse;
import com.barnizgallery.backend.enums.ArtworkStatus;

class GalleryCompositeTest {

    @Test
    void leafCountsItself() {
        ArtworkLeaf leaf = new ArtworkLeaf(1L, "Obra", "Artwork", ArtworkStatus.IN_AUCTION, null);

        assertThat(leaf.countArtworks()).isEqualTo(1);
        assertThat(leaf.countAvailableForAuction()).isEqualTo(1);
    }

    @Test
    void compositesAddTheCountsOfTheirChildren() {
        StoreComposite store1 = new StoreComposite(1L, "Tienda 1", "Store 1", 1L, "Master 1");
        store1.add(new ArtworkLeaf(1L, "A", "A", ArtworkStatus.EXHIBITED, null));
        store1.add(new ArtworkLeaf(2L, "B", "B", ArtworkStatus.IN_AUCTION, null));
        StoreComposite store2 = new StoreComposite(2L, "Tienda 2", "Store 2", 2L, "Master 2");
        store2.add(new ArtworkLeaf(3L, "C", "C", ArtworkStatus.SOLD, null));
        StoreComposite emptyStore = new StoreComposite(3L, "Tienda 3", "Store 3", 3L, "Master 3");
        GalleryComposite gallery = new GalleryComposite();
        gallery.add(store1);
        gallery.add(store2);
        gallery.add(emptyStore);

        assertThat(store1.countArtworks()).isEqualTo(2);
        assertThat(emptyStore.countArtworks()).isZero();
        assertThat(gallery.countArtworks()).isEqualTo(3);
        assertThat(gallery.countAvailableForAuction()).isEqualTo(1);
    }

    @Test
    void toNodeBuildsTheTreeRecursively() {
        StoreComposite store = new StoreComposite(1L, "Tienda", "Store", 7L, "Master");
        store.add(new ArtworkLeaf(5L, "Obra", "Artwork", ArtworkStatus.EXHIBITED, "thumb.jpg"));
        GalleryComposite gallery = new GalleryComposite();
        gallery.add(store);

        GalleryNodeResponse root = gallery.toNode();

        assertThat(root.type()).isEqualTo("GALLERY");
        assertThat(root.artworkCount()).isEqualTo(1);
        GalleryNodeResponse storeNode = root.children().get(0);
        assertThat(storeNode.type()).isEqualTo("STORE");
        assertThat(storeNode.masterId()).isEqualTo(7);
        GalleryNodeResponse artworkNode = storeNode.children().get(0);
        assertThat(artworkNode.type()).isEqualTo("ARTWORK");
        assertThat(artworkNode.status()).isEqualTo("EXHIBITED");
        assertThat(artworkNode.thumbnailUrl()).isEqualTo("thumb.jpg");
        assertThat(artworkNode.children()).isNull();
    }
}
