package com.barnizgallery.backend;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.Auction;
import com.barnizgallery.backend.model.entity.Master;
import com.barnizgallery.backend.model.entity.Photo;
import com.barnizgallery.backend.model.entity.Room;
import com.barnizgallery.backend.model.entity.Visitor;
import com.barnizgallery.backend.model.enums.ArtworkStatus;
import com.barnizgallery.backend.model.enums.AuctionStatus;
import com.barnizgallery.backend.model.enums.CameraMode;
import com.barnizgallery.backend.model.enums.Language;

/**
 * In-memory entities for unit tests. Nothing here is ever saved to a database.
 */
public final class TestEntities {

    private TestEntities() {
    }

    public static Master master(int id) {
        Master master = new Master();
        master.setMasterId(id);
        master.setName("Test master " + id);
        return master;
    }

    public static Room room(int id, Master master) {
        Room room = new Room();
        room.setRoomId(id);
        room.setMaster(master);
        room.setNameEs("Sala " + id);
        room.setNameEn("Room " + id);
        return room;
    }

    public static Artwork artwork(int id, Room room, ArtworkStatus status, String... colorTags) {
        Artwork artwork = new Artwork();
        artwork.setArtworkId(id);
        artwork.setRoom(room);
        artwork.setTitleEs("Obra " + id);
        artwork.setTitleEn("Artwork " + id);
        artwork.setStatus(status);
        artwork.setColorTags(List.of(colorTags));
        return artwork;
    }

    public static Photo photo(int id, Artwork artwork, String url) {
        Photo photo = new Photo();
        photo.setPhotoId(id);
        photo.setArtwork(artwork);
        photo.setFileUrl(url);
        return photo;
    }

    public static Visitor visitor(int id, Language language) {
        Visitor visitor = new Visitor();
        visitor.setVisitorId(id);
        visitor.setName("Visitor " + id);
        visitor.setEmail("visitor" + id + "@test.local");
        visitor.setPreferredLanguage(language);
        visitor.setCameraMode(CameraMode.FIRST_PERSON);
        return visitor;
    }

    public static Auction auction(int id, Artwork artwork, AuctionStatus status, LocalDateTime start,
            LocalDateTime end, String basePrice) {
        Auction auction = new Auction();
        auction.setAuctionId(id);
        auction.setArtwork(artwork);
        auction.setStatus(status);
        auction.setStartDate(start);
        auction.setEndDate(end);
        auction.setBasePrice(new BigDecimal(basePrice));
        return auction;
    }
}
