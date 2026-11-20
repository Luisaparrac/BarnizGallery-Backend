package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.AiDesignImage;
import com.barnizgallery.backend.dto.AiDesignRequest;
import com.barnizgallery.backend.dto.AiDesignResponse;
import com.barnizgallery.backend.entity.AiUsageLog;
import com.barnizgallery.backend.entity.CustomRequest;
import com.barnizgallery.backend.entity.CustomRequestDesign;
import com.barnizgallery.backend.entity.Shape;
import com.barnizgallery.backend.entity.Style;
import com.barnizgallery.backend.enums.CustomRequestStatus;
import com.barnizgallery.backend.enums.DesignStatus;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.AiServiceException;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;
import com.barnizgallery.backend.repository.AiUsageLogRepository;
import com.barnizgallery.backend.repository.CustomRequestDesignRepository;

/** AI designs: the limit, a saved image as a data URI, a failure saved as FAILED, and choosing a design. */
class CustomDesignServiceTest {

    private final CustomRequestService requestService = mock(CustomRequestService.class);
    private final CustomRequestDesignRepository designs = mock(CustomRequestDesignRepository.class);
    private final AiUsageLogRepository usage = mock(AiUsageLogRepository.class);
    private final AiServiceGateway ai = mock(AiServiceGateway.class);
    private final AppSettingsService settings = mock(AppSettingsService.class);
    private final CustomDesignService service = new CustomDesignService(requestService, designs, usage, ai, settings);
    private final CustomRequest request = new CustomRequest();

    @BeforeEach
    void draft() {
        Shape shape = new Shape();
        shape.setNameEs("Bandeja");
        Style style = new Style();
        style.setNameEs("Tradicional");
        request.setCustomRequestId(1L);
        request.setBuyer(TestEntities.user(4, Language.ES));
        request.setTitle("Bandeja para mi abuela");
        request.setDescription("");
        request.setShape(shape);
        request.setStyle(style);
        request.setColors(Set.of(TestEntities.color("rojo")));
        request.setOfferedPrice(BigDecimal.TEN);
        request.setStatus(CustomRequestStatus.DRAFT);
        when(requestService.myRequest(1L)).thenReturn(request);
        when(ai.isAvailable()).thenReturn(true);
        when(settings.integer(CustomDesignService.MAX_DESIGNS)).thenReturn(4);
    }

    @Test
    void aGeneratedImageIsSavedAsADataUri() {
        when(ai.generateDesigns(any())).thenReturn(new AiDesignResponse("fake", "fake-1", "p",
                List.of(new AiDesignImage("UklGRl", "image/webp", null, "Bandeja roja"))));

        service.generate(1L);

        ArgumentCaptor<AiDesignRequest> sent = ArgumentCaptor.forClass(AiDesignRequest.class);
        verify(ai).generateDesigns(sent.capture());
        assertThat(sent.getValue().count()).isEqualTo(1);
        assertThat(sent.getValue().shape()).isEqualTo("Bandeja");
        assertThat(sent.getValue().description()).isNull();
        CustomRequestDesign saved = savedDesign();
        assertThat(saved.getStatus()).isEqualTo(DesignStatus.COMPLETED);
        assertThat(saved.getImageUrl()).isEqualTo("data:image/webp;base64,UklGRl");
        assertThat(saved.getPrompt()).isEqualTo("Bandeja roja");
        assertThat(savedUsage().isSuccess()).isTrue();
    }

    @Test
    void aFailureIsSavedAsFailedWithTheReason() {
        when(ai.generateDesigns(any())).thenThrow(new AiServiceException(429, "budget_exceeded", "Sin cupo"));

        service.generate(1L);

        CustomRequestDesign saved = savedDesign();
        assertThat(saved.getStatus()).isEqualTo(DesignStatus.FAILED);
        assertThat(saved.getImageUrl()).isNull();
        assertThat(saved.getPrompt()).contains("budget_exceeded");
        assertThat(savedUsage().isSuccess()).isFalse();
    }

    @Test
    void theLimitStopsTheCallBeforeSpending() {
        when(designs.countByCustomRequestCustomRequestId(1L)).thenReturn(4L);

        assertThatThrownBy(() -> service.generate(1L)).hasMessage("custom_design_limit_reached [4]");
        verify(ai, never()).generateDesigns(any());
    }

    @Test
    void onlyACompletedDesignCanBeChosen() {
        CustomRequestDesign failed = design(5, DesignStatus.FAILED);
        CustomRequestDesign done = design(6, DesignStatus.COMPLETED);
        CustomRequestDesign before = design(7, DesignStatus.COMPLETED);
        before.setSelected(true);
        when(designs.findByCustomRequestCustomRequestIdOrderByDesignIdAsc(1L))
                .thenReturn(new ArrayList<>(List.of(failed, done, before)));

        assertThatThrownBy(() -> service.select(1L, 5L)).hasMessage("custom_design_not_found [5]");
        service.select(1L, 6L);

        assertThat(done.isSelected()).isTrue();
        assertThat(before.isSelected()).isFalse();
    }

    private CustomRequestDesign design(long id, DesignStatus status) {
        CustomRequestDesign design = new CustomRequestDesign();
        design.setDesignId(id);
        design.setCustomRequest(request);
        design.setStatus(status);
        return design;
    }

    private CustomRequestDesign savedDesign() {
        ArgumentCaptor<CustomRequestDesign> captor = ArgumentCaptor.forClass(CustomRequestDesign.class);
        verify(designs).save(captor.capture());
        return captor.getValue();
    }

    private AiUsageLog savedUsage() {
        ArgumentCaptor<AiUsageLog> captor = ArgumentCaptor.forClass(AiUsageLog.class);
        verify(usage).save(captor.capture());
        return captor.getValue();
    }
}
