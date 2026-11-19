package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.ContactMessageRequest;
import com.barnizgallery.backend.dto.ContactMessageResponse;
import com.barnizgallery.backend.entity.ContactMessage;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.repository.ContactMessageRepository;

@ExtendWith(MockitoExtension.class)
class ContactMessageServiceTest {

    @Mock
    private ContactMessageRepository contactMessageRepository;
    @Mock
    private UserService userService;
    @Mock
    private MasterService masterService;
    @Mock
    private ArtworkService artworkService;
    @InjectMocks
    private ContactMessageService contactMessageService;

    private final Master master1 = TestEntities.master(1);
    private final Master master2 = TestEntities.master(2);

    @Test
    void sendWithArtworkOfAnotherMasterIsRejected() {
        when(userService.getUser(5L)).thenReturn(TestEntities.user(5, Language.ES));
        when(masterService.getMaster(1L)).thenReturn(master1);
        when(artworkService.getArtwork(30L)).thenReturn(
                TestEntities.artwork(30, TestEntities.room(2, master2), ArtworkStatus.EXHIBITED));

        assertThatThrownBy(() -> contactMessageService.send(new ContactMessageRequest(5L, 1L, 30L, "Hello")))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT));
        verify(contactMessageRepository, never()).save(any());
    }

    @Test
    void sendWithArtworkOfSameMasterIsSaved() {
        when(userService.getUser(5L)).thenReturn(TestEntities.user(5, Language.ES));
        when(masterService.getMaster(1L)).thenReturn(master1);
        when(artworkService.getArtwork(30L)).thenReturn(
                TestEntities.artwork(30, TestEntities.room(1, master1), ArtworkStatus.EXHIBITED));
        when(contactMessageRepository.save(any(ContactMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        ContactMessageResponse sent = contactMessageService.send(new ContactMessageRequest(5L, 1L, 30L, " Hello "));

        assertThat(sent.artworkId()).isEqualTo(30);
        assertThat(sent.content()).isEqualTo("Hello");
    }

    @Test
    void sendWithoutArtworkIsSaved() {
        when(userService.getUser(5L)).thenReturn(TestEntities.user(5, Language.ES));
        when(masterService.getMaster(1L)).thenReturn(master1);
        when(contactMessageRepository.save(any(ContactMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        ContactMessageResponse sent = contactMessageService.send(new ContactMessageRequest(5L, 1L, null, "Hi"));

        assertThat(sent.artworkId()).isNull();
        assertThat(sent.masterId()).isEqualTo(1);
    }
}
