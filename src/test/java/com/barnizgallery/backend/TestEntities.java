package com.barnizgallery.backend;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Photo;
import com.barnizgallery.backend.entity.Room;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;
import com.barnizgallery.backend.enums.UserStatus;

/**
 * In-memory entities for unit tests. Nothing here is ever saved to a database.
 */
public final class TestEntities {

    private TestEntities() {
    }

    public static Master master(long id) {
        Master master = new Master();
        master.setMasterId(id);
        master.setName("Test master " + id);
        return master;
    }

    public static Room room(long id, Master master) {
        Room room = new Room();
        room.setRoomId(id);
        room.setMaster(master);
        room.setNameEs("Sala " + id);
        room.setNameEn("Room " + id);
        return room;
    }

    public static Artwork artwork(long id, Room room, ArtworkStatus status, String... colorTags) {
        Artwork artwork = new Artwork();
        artwork.setArtworkId(id);
        artwork.setRoom(room);
        artwork.setTitleEs("Obra " + id);
        artwork.setTitleEn("Artwork " + id);
        artwork.setStatus(status);
        artwork.setColorTags(List.of(colorTags));
        return artwork;
    }

    public static Photo photo(long id, Artwork artwork, String url) {
        Photo photo = new Photo();
        photo.setPhotoId(id);
        photo.setArtwork(artwork);
        photo.setFileUrl(url);
        return photo;
    }

    public static User user(long id, Language language) {
        User user = new User();
        user.setUserId(id);
        user.setName("User " + id);
        user.setEmail("user" + id + "@test.local");
        user.setPreferredLanguage(language);
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    public static Auction auction(long id, Artwork artwork, AuctionStatus status, LocalDateTime start,
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
