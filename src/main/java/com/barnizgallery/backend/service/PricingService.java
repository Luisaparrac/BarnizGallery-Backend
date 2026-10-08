package com.barnizgallery.backend.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.config.AuctionProperties;
import com.barnizgallery.backend.dto.response.SuggestedPriceResponse;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.patterns.facade.AiFacade;

/**
 * Base price suggestions for auctions (through the {@link AiFacade}).
 */
@Service
public class PricingService {

    private final AiFacade aiFacade;
    private final ArtworkService artworkService;
    private final AuctionProperties auctionProperties;

    public PricingService(AiFacade aiFacade, ArtworkService artworkService, AuctionProperties auctionProperties) {
        this.aiFacade = aiFacade;
        this.artworkService = artworkService;
        this.auctionProperties = auctionProperties;
    }

    /** Suggested base price; 503 when AI is disabled or cannot give a price. */
    @Transactional(readOnly = true)
    public SuggestedPriceResponse suggestedPrice(Integer artworkId) {
        Artwork artwork = artworkService.getArtwork(artworkId);
        if (!aiFacade.isAiEnabled()) {
            throw new FeatureDisabledException("AI is disabled: no price suggestion available");
        }
        BigDecimal price = aiFacade.suggestBasePrice(artwork)
                .orElseThrow(() -> new FeatureDisabledException("AI could not suggest a price for this artwork"));
        return new SuggestedPriceResponse(artworkId, price, auctionProperties.currency());
    }
}
