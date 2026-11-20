package com.barnizgallery.backend.service;

import java.time.Clock;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AdminArtworkUpdateRequest;
import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.DatabaseErrors;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.repository.ArtworkRepository;

/**
 * Administrators complete the sale data of any artwork (for example the ones loaded by the migrations): price,
 * currency, stock, sale mode and status. Auction and reservation statuses stay managed by auctions and orders;
 * the same sale rules as for artisans apply, and the database keeps the currency once there are orders or
 * auctions. Changes are recorded by the database audit with the administrator's id.
 */
@Service
public class AdminArtworkService {

    private static final Set<ArtworkStatus> SYSTEM_MANAGED = EnumSet.of(ArtworkStatus.IN_AUCTION, ArtworkStatus.RESERVED);

    private final ArtworkRepository artworkRepository;
    private final ArtworkService artworkService;
    private final Clock clock;

    public AdminArtworkService(ArtworkRepository artworkRepository, ArtworkService artworkService, Clock clock) {
        this.artworkRepository = artworkRepository;
        this.artworkService = artworkService;
        this.clock = clock;
    }

    /** Every artwork of a store that is not deleted, drafts included. */
    @Transactional(readOnly = true)
    public List<ArtworkSummaryResponse> ofStore(Long storeId) {
        return artworkRepository.findByStoreStoreIdAndDeletedAtIsNullOrderByArtworkIdAsc(storeId).stream()
                .map(ArtworkMapper::toSummary)
                .toList();
    }

    @Transactional
    public ArtworkSummaryResponse update(Long artworkId, AdminArtworkUpdateRequest request) {
        Artwork artwork = artworkService.getArtwork(artworkId);
        ActiveMasterRule.requireActive(artwork.getStore().getMaster());
        ArtworkStatus status = request.status() == null ? artwork.getStatus() : request.status();
        if (status != artwork.getStatus()
                && (SYSTEM_MANAGED.contains(status) || SYSTEM_MANAGED.contains(artwork.getStatus()))) {
            throw new BusinessRuleException(ErrorCode.ARTWORK_STATUS_CHANGE_INVALID, artwork.getStatus(), status);
        }
        String oldCurrency = artwork.getCurrency();
        if (request.price() != null) {
            artwork.setPrice(request.price());
        }
        if (request.currency() != null) {
            artwork.setCurrency(request.currency());
        }
        if (request.stock() != null) {
            artwork.setStock(request.stock());
        }
        if (request.saleMode() != null) {
            artwork.setSaleMode(request.saleMode());
        }
        ArtworkSaleRules.validate(artwork, status);
        if (status == ArtworkStatus.EXHIBITED && artwork.getPublishedAt() == null) {
            artwork.setPublishedAt(clock.instant());
        }
        artwork.setStatus(status);
        try {
            artworkRepository.saveAndFlush(artwork);
        } catch (DataAccessException ex) {
            if (DatabaseErrors.isRuleViolation(ex) && !Objects.equals(oldCurrency, artwork.getCurrency())) {
                throw new BusinessRuleException(ErrorCode.ARTWORK_CURRENCY_LOCKED, oldCurrency);
            }
            throw ex;
        }
        return ArtworkMapper.toSummary(artwork);
    }
}
