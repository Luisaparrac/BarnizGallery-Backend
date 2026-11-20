package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.StoreSummaryResponse;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.StoreStatus;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.StoreArtworkCount;
import com.barnizgallery.backend.repository.StoreRepository;
import com.barnizgallery.backend.repository.StoreSectionRepository;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private ArtworkRepository artworkRepository;
    @Mock
    private StoreSectionRepository sectionRepository;
    @InjectMocks
    private StoreService storeService;

    @Test
    void publishedStoresCarryTheirCountOfPublicArtworks() {
        Store store1 = TestEntities.store(1, TestEntities.master(1));
        Store store2 = TestEntities.store(2, TestEntities.master(2));
        when(storeRepository.findByStatusWithMaster(StoreStatus.PUBLISHED, MasterVerificationStatus.APPROVED))
                .thenReturn(List.of(store1, store2));
        StoreArtworkCount count = count(1, 3);
        when(artworkRepository.countGroupedByStore(ArtworkStatus.PUBLIC)).thenReturn(List.of(count));

        List<StoreSummaryResponse> stores = storeService.findPublished();

        assertThat(stores).extracting(StoreSummaryResponse::artworkCount).containsExactly(3L, 0L);
        assertThat(stores.get(0).masterName()).isEqualTo("Test master 1");
    }

    @Test
    void unpublishedSlugIsNotFound() {
        when(storeRepository.findBySlugAndStatus("taller-ana", StoreStatus.PUBLISHED,
                MasterVerificationStatus.APPROVED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.findPublishedBySlug("taller-ana"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("store_not_found [taller-ana]");
    }

    private static StoreArtworkCount count(long storeId, long artworkCount) {
        StoreArtworkCount count = mock(StoreArtworkCount.class);
        when(count.getStoreId()).thenReturn(storeId);
        when(count.getArtworkCount()).thenReturn(artworkCount);
        return count;
    }
}
