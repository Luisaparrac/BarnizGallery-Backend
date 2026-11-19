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
import com.barnizgallery.backend.dto.RoomRequest;
import com.barnizgallery.backend.dto.RoomSummaryResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Room;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.RoomArtworkCount;
import com.barnizgallery.backend.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ArtworkRepository artworkRepository;
    @Mock
    private MasterService masterService;
    @InjectMocks
    private RoomService roomService;

    @Test
    void findAllAddsArtworkCountPerRoom() {
        Room room1 = TestEntities.room(1, TestEntities.master(1));
        Room room2 = TestEntities.room(2, TestEntities.master(2));
        when(roomRepository.findAllWithMaster()).thenReturn(List.of(room1, room2));
        RoomArtworkCount count = count(1, 3);
        when(artworkRepository.countGroupedByRoom()).thenReturn(List.of(count));

        List<RoomSummaryResponse> rooms = roomService.findAll();

        assertThat(rooms).extracting(RoomSummaryResponse::artworkCount).containsExactly(3L, 0L);
        assertThat(rooms.get(0).masterName()).isEqualTo("Test master 1");
    }

    @Test
    void createFailsWhenMasterAlreadyHasRoom() {
        Master master = TestEntities.master(1);
        when(masterService.getMaster(1L)).thenReturn(master);
        when(roomRepository.existsByMasterMasterId(1L)).thenReturn(true);

        assertThatThrownBy(() -> roomService.create(request(1)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already has a room");
        verify(roomRepository, never()).save(any());
    }

    @Test
    void createSavesRoomLinkedToMaster() {
        Master master = TestEntities.master(1);
        when(masterService.getMaster(1L)).thenReturn(master);
        when(roomRepository.existsByMasterMasterId(1L)).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> {
            Room saved = inv.getArgument(0);
            saved.setRoomId(5L);
            return saved;
        });

        RoomSummaryResponse created = roomService.create(request(1));

        assertThat(created.roomId()).isEqualTo(5);
        assertThat(created.masterId()).isEqualTo(1);
        assertThat(created.nameEn()).isEqualTo("Room");
    }

    @Test
    void updateToAnotherMasterWithRoomFails() {
        Room room = TestEntities.room(1, TestEntities.master(1));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(masterService.getMaster(2L)).thenReturn(TestEntities.master(2));
        when(roomRepository.existsByMasterMasterId(2L)).thenReturn(true);

        assertThatThrownBy(() -> roomService.update(1L, request(2)))
                .isInstanceOf(BusinessRuleException.class);
    }

    private static RoomRequest request(long masterId) {
        return new RoomRequest(masterId, "Sala", "Room", null, null);
    }

    private static RoomArtworkCount count(long roomId, long artworkCount) {
        RoomArtworkCount count = mock(RoomArtworkCount.class);
        when(count.getRoomId()).thenReturn(roomId);
        when(count.getArtworkCount()).thenReturn(artworkCount);
        return count;
    }
}
