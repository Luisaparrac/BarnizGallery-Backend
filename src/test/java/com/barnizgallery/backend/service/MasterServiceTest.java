package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.MasterDetailResponse;
import com.barnizgallery.backend.dto.MasterRequest;
import com.barnizgallery.backend.dto.MasterResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class MasterServiceTest {

    @Mock
    private MasterRepository masterRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private ArtworkRepository artworkRepository;
    @InjectMocks
    private MasterService masterService;

    @Test
    void findAllReturnsEmptyListWhenTableIsEmpty() {
        when(masterRepository.findAll(any(Sort.class))).thenReturn(List.of());

        assertThat(masterService.findAll()).isEmpty();
    }

    @Test
    void findByIdIncludesStoreWithArtworkCount() {
        Master master = TestEntities.master(1);
        Store store = TestEntities.store(10, master);
        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));
        when(storeRepository.findFirstByMasterMasterIdOrderByStoreIdAsc(1L)).thenReturn(Optional.of(store));
        when(artworkRepository.countByStoreStoreId(10L)).thenReturn(4L);

        MasterDetailResponse detail = masterService.findById(1L);

        assertThat(detail.masterId()).isEqualTo(1);
        assertThat(detail.store().storeId()).isEqualTo(10);
        assertThat(detail.store().artworkCount()).isEqualTo(4);
    }

    @Test
    void findByIdWithoutStoreReturnsNullStore() {
        when(masterRepository.findById(1L)).thenReturn(Optional.of(TestEntities.master(1)));
        when(storeRepository.findFirstByMasterMasterIdOrderByStoreIdAsc(1L)).thenReturn(Optional.empty());

        assertThat(masterService.findById(1L).store()).isNull();
    }

    @Test
    void findByIdThrows404WhenMissing() {
        when(masterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> masterService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Master 99 not found");
    }

    @Test
    void updateCopiesRequestFields() {
        Master master = TestEntities.master(1);
        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));

        MasterResponse updated = masterService.update(1L,
                new MasterRequest("New name", "Bio ES", "Bio EN", "Workshop", "contact"));

        assertThat(updated.name()).isEqualTo("New name");
        assertThat(master.getWorkshop()).isEqualTo("Workshop");
    }
}
