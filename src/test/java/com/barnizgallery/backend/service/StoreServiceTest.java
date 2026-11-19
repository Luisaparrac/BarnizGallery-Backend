package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.StoreRequest;
import com.barnizgallery.backend.dto.StoreSummaryResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.StoreStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.StoreArtworkCount;
import com.barnizgallery.backend.repository.StoreRepository;
import com.barnizgallery.backend.repository.StoreTemplateRepository;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private StoreTemplateRepository templateRepository;
    @Mock
    private ArtworkRepository artworkRepository;
    @Mock
    private MasterService masterService;
    @InjectMocks
    private StoreService storeService;

    @Test
    void findAllAddsArtworkCountPerStore() {
        Store store1 = TestEntities.store(1, TestEntities.master(1));
        Store store2 = TestEntities.store(2, TestEntities.master(2));
        when(storeRepository.findAllWithMaster()).thenReturn(List.of(store1, store2));
        StoreArtworkCount count = count(1, 3);
        when(artworkRepository.countGroupedByStore()).thenReturn(List.of(count));

        List<StoreSummaryResponse> stores = storeService.findAll();

        assertThat(stores).extracting(StoreSummaryResponse::artworkCount).containsExactly(3L, 0L);
        assertThat(stores.get(0).masterName()).isEqualTo("Test master 1");
    }

    @Test
    void createFailsWhenMasterAlreadyHasStore() {
        Master master = TestEntities.master(1);
        when(masterService.getMaster(1L)).thenReturn(master);
        when(storeRepository.existsByMasterMasterId(1L)).thenReturn(true);

        assertThatThrownBy(() -> storeService.create(request(1)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already has a store");
        verify(storeRepository, never()).save(any());
    }

    @Test
    void createSavesStoreLinkedToMaster() {
        Master master = TestEntities.master(1);
        when(masterService.getMaster(1L)).thenReturn(master);
        when(storeRepository.existsByMasterMasterId(1L)).thenReturn(false);
        when(templateRepository.findById(1L)).thenReturn(Optional.of(TestEntities.template(1)));
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> {
            Store saved = inv.getArgument(0);
            saved.setStoreId(5L);
            return saved;
        });

        StoreSummaryResponse created = storeService.create(request(1));

        assertThat(created.storeId()).isEqualTo(5);
        assertThat(created.masterId()).isEqualTo(1);
        assertThat(created.nameEn()).isEqualTo("Store");
        assertThat(created.status()).isEqualTo(StoreStatus.DRAFT);
    }

    @Test
    void updateToAnotherMasterWithStoreFails() {
        Store store = TestEntities.store(1, TestEntities.master(1));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(masterService.getMaster(2L)).thenReturn(TestEntities.master(2));
        when(storeRepository.existsByMasterMasterId(2L)).thenReturn(true);

        assertThatThrownBy(() -> storeService.update(1L, request(2)))
                .isInstanceOf(BusinessRuleException.class);
    }

    private static StoreRequest request(long masterId) {
        return new StoreRequest(masterId, 1L, "Tienda", "Store", null, null);
    }

    private static StoreArtworkCount count(long storeId, long artworkCount) {
        StoreArtworkCount count = mock(StoreArtworkCount.class);
        when(count.getStoreId()).thenReturn(storeId);
        when(count.getArtworkCount()).thenReturn(artworkCount);
        return count;
    }
}
