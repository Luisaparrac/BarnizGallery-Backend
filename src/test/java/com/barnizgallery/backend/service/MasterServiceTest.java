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
import com.barnizgallery.backend.dto.request.MasterRequest;
import com.barnizgallery.backend.dto.response.MasterDetailResponse;
import com.barnizgallery.backend.dto.response.MasterResponse;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.model.entity.Master;
import com.barnizgallery.backend.model.entity.Room;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class MasterServiceTest {

    @Mock
    private MasterRepository masterRepository;
    @Mock
    private RoomRepository roomRepository;
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
    void findByIdIncludesRoomWithArtworkCount() {
        Master master = TestEntities.master(1);
        Room room = TestEntities.room(10, master);
        when(masterRepository.findById(1)).thenReturn(Optional.of(master));
        when(roomRepository.findByMasterMasterId(1)).thenReturn(Optional.of(room));
        when(artworkRepository.countByRoomRoomId(10)).thenReturn(4L);

        MasterDetailResponse detail = masterService.findById(1);

        assertThat(detail.masterId()).isEqualTo(1);
        assertThat(detail.room().roomId()).isEqualTo(10);
        assertThat(detail.room().artworkCount()).isEqualTo(4);
    }

    @Test
    void findByIdWithoutRoomReturnsNullRoom() {
        when(masterRepository.findById(1)).thenReturn(Optional.of(TestEntities.master(1)));
        when(roomRepository.findByMasterMasterId(1)).thenReturn(Optional.empty());

        assertThat(masterService.findById(1).room()).isNull();
    }

    @Test
    void findByIdThrows404WhenMissing() {
        when(masterRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> masterService.findById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Master 99 not found");
    }

    @Test
    void updateCopiesRequestFields() {
        Master master = TestEntities.master(1);
        when(masterRepository.findById(1)).thenReturn(Optional.of(master));

        MasterResponse updated = masterService.update(1,
                new MasterRequest("New name", "Bio ES", "Bio EN", "Workshop", "contact"));

        assertThat(updated.name()).isEqualTo("New name");
        assertThat(master.getWorkshop()).isEqualTo("Workshop");
    }
}
