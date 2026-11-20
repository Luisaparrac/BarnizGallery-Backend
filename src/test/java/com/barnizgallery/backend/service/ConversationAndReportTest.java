package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.ReportRequest;
import com.barnizgallery.backend.dto.StartConversationRequest;
import com.barnizgallery.backend.entity.ContentReport;
import com.barnizgallery.backend.entity.Conversation;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Message;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.ReportStatus;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.ContentReportRepository;
import com.barnizgallery.backend.repository.ConversationRepository;
import com.barnizgallery.backend.repository.CustomRequestRepository;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.MessageRepository;
import com.barnizgallery.backend.repository.OrderRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/** Conversations are only visible to their two participants; reports wait for the administrators. */
class ConversationAndReportTest {

    private final ConversationRepository conversations = mock(ConversationRepository.class);
    private final MessageRepository messages = mock(MessageRepository.class);
    private final MasterRepository masters = mock(MasterRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final ConversationService service = new ConversationService(conversations, messages, masters, users,
            mock(ArtworkRepository.class), mock(CustomRequestRepository.class), mock(OrderRepository.class), currentUser,
            Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneOffset.UTC), notifications);

    private final User customer = TestEntities.user(4, Language.ES);
    private final User artisan = TestEntities.user(7, Language.EN);
    private final Master master = TestEntities.master(70);

    @Test
    void onlyTheTwoParticipantsSeeAConversationAndTheOtherOneIsNotified() {
        master.setUser(artisan);
        Conversation conversation = new Conversation();
        conversation.setConversationId(3L);
        conversation.setCustomer(customer);
        conversation.setMaster(master);
        when(conversations.findById(3L)).thenReturn(Optional.of(conversation));
        when(messages.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));
        when(users.getReferenceById(7L)).thenReturn(artisan);
        when(users.findById(7L)).thenReturn(Optional.of(artisan));

        when(currentUser.requireId()).thenReturn(9L);
        assertThatThrownBy(() -> service.messages(3L)).hasMessage("conversation_not_found [3]");

        when(currentUser.requireId()).thenReturn(7L);
        service.send(3L, "  Sí, con envío  ");

        verify(notifications).notify(eq(customer), eq(ConversationService.NEW_MESSAGE), eq("/messages/3"), any(),
                anyString());
    }

    @Test
    void aConversationHasOneContextAtMostAndNeverWithYourself() {
        when(currentUser.requireId()).thenReturn(7L);
        master.setUser(artisan);
        when(masters.findById(70L)).thenReturn(Optional.of(master));

        assertThatThrownBy(() -> service.start(new StartConversationRequest(70L, null, "Hola", 1L, null, 2L)))
                .hasMessage("conversation_context_invalid");
        assertThatThrownBy(() -> service.start(new StartConversationRequest(70L, null, "Hola", null, null, null)))
                .hasMessage("conversation_context_invalid");
        master.setActive(false);
        assertThatThrownBy(() -> service.start(new StartConversationRequest(70L, null, "Hola", null, null, null)))
                .hasMessage("master_not_found [70]");
    }

    @Test
    void aReportIsSavedPendingForTheAdministrators() {
        ContentReportRepository reports = mock(ContentReportRepository.class);
        when(currentUser.requireId()).thenReturn(4L);
        ContentReportService reportService = new ContentReportService(reports, users, currentUser);

        assertThat(reportService.report(new ReportRequest("ARTWORK", 7L, " Duplicada ")).status())
                .isEqualTo(ReportStatus.PENDING);

        ArgumentCaptor<ContentReport> saved = ArgumentCaptor.forClass(ContentReport.class);
        verify(reports).save(saved.capture());
        assertThat(saved.getValue().getTargetId()).isEqualTo("7");
        assertThat(saved.getValue().getReason()).isEqualTo("Duplicada");
    }
}
