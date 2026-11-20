package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.CustomOffer;
import com.barnizgallery.backend.entity.CustomRequest;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.entity.Shape;
import com.barnizgallery.backend.entity.Style;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.CustomRequestStatus;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.OfferStatus;
import com.barnizgallery.backend.enums.OfferType;
import com.barnizgallery.backend.repository.ColorRepository;
import com.barnizgallery.backend.repository.CustomOfferRepository;
import com.barnizgallery.backend.repository.CustomRequestDesignRepository;
import com.barnizgallery.backend.repository.CustomRequestRepository;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.MotifRepository;
import com.barnizgallery.backend.repository.ShapeRepository;
import com.barnizgallery.backend.repository.StyleRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/** Custom requests: publishing rules, ownership, and accepting an offer. */
class CustomRequestFlowTest {

    private final CustomRequestRepository requests = mock(CustomRequestRepository.class);
    private final CustomOfferRepository offers = mock(CustomOfferRepository.class);
    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final CustomOrderCreator orderCreator = mock(CustomOrderCreator.class);
    private final CustomRequestService requestService = new CustomRequestService(requests, offers,
            mock(CustomRequestDesignRepository.class), mock(UserRepository.class), mock(MasterRepository.class),
            mock(ShapeRepository.class), mock(StyleRepository.class), mock(ColorRepository.class),
            mock(MotifRepository.class), mock(AppSettingsService.class), currentUser);
    private final CustomOfferService offerService = new CustomOfferService(requests, offers, requestService,
            mock(ApprovedArtisan.class), orderCreator, notifications);

    private final User buyer = TestEntities.user(4, Language.ES);
    private final CustomRequest request = new CustomRequest();
    private final List<CustomOffer> requestOffers = new ArrayList<>();

    @BeforeEach
    void ownDraft() {
        request.setCustomRequestId(1L);
        request.setBuyer(buyer);
        request.setTitle("Plato");
        request.setDescription("  ");
        request.setShape(new Shape());
        request.setStyle(new Style());
        request.setOfferedPrice(new BigDecimal("200"));
        request.setCurrency("USD");
        request.setStatus(CustomRequestStatus.DRAFT);
        when(currentUser.requireId()).thenReturn(4L);
        when(requests.findById(1L)).thenReturn(Optional.of(request));
        when(offers.findByCustomRequestCustomRequestIdOrderByCustomOfferIdAsc(1L)).thenReturn(requestOffers);
    }

    @Test
    void publishingNeedsADescriptionButNoDesign() {
        assertThatThrownBy(() -> requestService.publish(1L)).hasMessage("custom_request_description_required");

        request.setDescription("Un plato con colibríes");
        assertThat(requestService.publish(1L).status()).isEqualTo("PUBLISHED");
        assertThat(request.getStatus()).isEqualTo(CustomRequestStatus.OPEN);
        assertThatThrownBy(() -> requestService.publish(1L)).hasMessage("custom_request_status_invalid [OPEN]");
    }

    @Test
    void aRequestOfSomeoneElseIsNotFound() {
        when(currentUser.requireId()).thenReturn(9L);

        assertThatThrownBy(() -> requestService.get(1L)).hasMessage("custom_request_not_found [1]");
    }

    @Test
    void acceptingAnOfferRejectsTheOthersAndCreatesTheOrder() {
        request.setStatus(CustomRequestStatus.OPEN);
        CustomOffer chosen = offer(10, 7);
        CustomOffer other = offer(11, 8);
        requestOffers.addAll(List.of(chosen, other));
        when(offers.findById(10L)).thenReturn(Optional.of(chosen));
        Order order = new Order();
        order.setOrderId(50L);
        when(orderCreator.create(request, chosen)).thenReturn(order);

        var accepted = offerService.accept(1L, 10L);

        assertThat(accepted.orderId()).isEqualTo(50L);
        assertThat(chosen.getStatus()).isEqualTo(OfferStatus.ACCEPTED);
        assertThat(other.getStatus()).isEqualTo(OfferStatus.REJECTED);
        assertThat(request.getStatus()).isEqualTo(CustomRequestStatus.ACCEPTED);
        assertThat(request.getAcceptedOffer()).isSameAs(chosen);
        verify(notifications).notify(eq(chosen.getMaster().getUser()), eq(CustomOfferService.OFFER_ACCEPTED), anyString(),
                any(), eq("Plato"));
        verify(notifications).notify(eq(other.getMaster().getUser()), eq(CustomOfferService.OFFER_REJECTED), anyString(),
                any(), eq("Plato"));
    }

    @Test
    void cancellingAnOpenRequestRejectsItsActiveOffers() {
        request.setStatus(CustomRequestStatus.OPEN);
        CustomOffer active = offer(10, 7);
        requestOffers.add(active);

        assertThat(requestService.cancel(1L).status()).isEqualTo("CANCELLED");
        assertThat(active.getStatus()).isEqualTo(OfferStatus.REJECTED);
    }

    private CustomOffer offer(long id, long masterId) {
        CustomOffer offer = new CustomOffer();
        offer.setCustomOfferId(id);
        offer.setCustomRequest(request);
        var master = TestEntities.master(masterId);
        master.setUser(TestEntities.user(masterId, Language.ES));
        offer.setMaster(master);
        offer.setStore(TestEntities.store(masterId, master));
        offer.setOfferType(OfferType.COUNTER_OFFER);
        offer.setPrice(new BigDecimal("260"));
        offer.setCurrency("USD");
        offer.setEstimatedDays(15);
        offer.setStatus(OfferStatus.ACTIVE);
        offer.setCreatedAt(Instant.now());
        return offer;
    }
}
