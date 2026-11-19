package com.barnizgallery.backend;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Color;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Photo;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.StoreTemplate;
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

    public static Store store(long id, Master master) {
        Store store = new Store();
        store.setStoreId(id);
        store.setMaster(master);
        store.setNameEs("Tienda " + id);
        store.setNameEn("Store " + id);
        return store;
    }

    public static StoreTemplate template(long id) {
        StoreTemplate template = new StoreTemplate();
        template.setTemplateId(id);
        template.setCode("template-" + id);
        return template;
    }

    public static Color color(String code) {
        Color color = new Color();
        color.setCode(code);
        return color;
    }

    public static Artwork artwork(long id, Store store, ArtworkStatus status, String... colorCodes) {
        Artwork artwork = new Artwork();
        artwork.setArtworkId(id);
        artwork.setStore(store);
        artwork.setTitleEs("Obra " + id);
        artwork.setTitleEn("Artwork " + id);
        artwork.setStatus(status);
        for (String code : colorCodes) {
            artwork.getColors().add(color(code));
        }
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
