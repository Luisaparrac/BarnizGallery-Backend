package com.barnizgallery.backend.service;

import java.util.List;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AiDesignColor;
import com.barnizgallery.backend.dto.AiDesignImage;
import com.barnizgallery.backend.dto.AiDesignRequest;
import com.barnizgallery.backend.dto.AiDesignResponse;
import com.barnizgallery.backend.dto.CustomRequestResponse;
import com.barnizgallery.backend.entity.AiUsageLog;
import com.barnizgallery.backend.entity.CustomRequest;
import com.barnizgallery.backend.entity.CustomRequestDesign;
import com.barnizgallery.backend.enums.CustomRequestStatus;
import com.barnizgallery.backend.enums.DesignStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.AiServiceException;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;
import com.barnizgallery.backend.repository.AiUsageLogRepository;
import com.barnizgallery.backend.repository.CustomRequestDesignRepository;

/**
 * AI designs of a draft custom request. Each call asks the AI service for one image and always saves a design:
 * {@code COMPLETED} with the image, or {@code FAILED} with the reason, so a failure never blocks the customer
 * (publishing does not need a design). The limit per request is {@code custom_orders.max_ai_designs}, counted
 * with every saved design; nothing is retried.
 * <p>
 * Cloudinary is not configured yet, so the image is stored as a {@code data:} URI in {@code image_url} (a text
 * column without a limit). The table has no column for the failure reason, so a failed design keeps it in
 * {@code prompt}.
 */
@Service
public class CustomDesignService {

    static final String MAX_DESIGNS = "custom_orders.max_ai_designs";
    static final String USAGE_FUNCTION = "custom_design";
    static final String UNKNOWN = "unknown";
    private static final int IMAGES_PER_CALL = 1;

    private final CustomRequestService requestService;
    private final CustomRequestDesignRepository designRepository;
    private final AiUsageLogRepository usageRepository;
    private final AiServiceGateway aiService;
    private final AppSettingsService settings;

    public CustomDesignService(CustomRequestService requestService, CustomRequestDesignRepository designRepository,
            AiUsageLogRepository usageRepository, AiServiceGateway aiService, AppSettingsService settings) {
        this.requestService = requestService;
        this.designRepository = designRepository;
        this.usageRepository = usageRepository;
        this.aiService = aiService;
        this.settings = settings;
    }

    /** Asks for one more design of an own draft request (409 when the limit is reached or not configured). */
    @Transactional
    public CustomRequestResponse generate(Long id) {
        CustomRequest request = requestService.myRequest(id);
        CustomRequestService.requireStatus(request, CustomRequestStatus.DRAFT);
        if (!aiService.isAvailable()) {
            throw new FeatureDisabledException(ErrorCode.AI_DISABLED);
        }
        int max = settings.integer(MAX_DESIGNS);
        if (designRepository.countByCustomRequestCustomRequestId(id) >= max) {
            throw new BusinessRuleException(ErrorCode.CUSTOM_DESIGN_LIMIT_REACHED, max);
        }
        CustomRequestDesign design = new CustomRequestDesign();
        design.setCustomRequest(request);
        long start = System.nanoTime();
        try {
            AiDesignResponse answer = aiService.generateDesigns(toAiRequest(request));
            AiDesignImage image = firstImage(answer);
            design.setProvider(answer.provider() == null ? UNKNOWN : answer.provider());
            design.setModel(answer.model() == null ? UNKNOWN : answer.model());
            design.setPrompt(image.prompt() != null ? image.prompt() : String.valueOf(answer.prompt()));
            design.setImageUrl("data:" + image.mimeType() + ";base64," + image.imageBase64());
            design.setStatus(DesignStatus.COMPLETED);
        } catch (AiServiceException ex) {
            design.setProvider(UNKNOWN);
            design.setModel(UNKNOWN);
            design.setPrompt("error " + ex.getCode() + ": " + ex.getMessage());
            design.setStatus(DesignStatus.FAILED);
        }
        logUsage(design, start);
        designRepository.save(design);
        return requestService.toResponse(request);
    }

    /** Marks one completed design of an own draft request as the chosen one. */
    @Transactional
    public CustomRequestResponse select(Long id, Long designId) {
        CustomRequest request = requestService.myRequest(id);
        CustomRequestService.requireStatus(request, CustomRequestStatus.DRAFT);
        List<CustomRequestDesign> designs = designRepository.findByCustomRequestCustomRequestIdOrderByDesignIdAsc(id);
        CustomRequestDesign chosen = designs.stream()
                .filter(d -> d.getDesignId().equals(designId) && d.getStatus() == DesignStatus.COMPLETED)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CUSTOM_DESIGN_NOT_FOUND, designId));
        designs.forEach(d -> d.setSelected(false));
        designRepository.flush();
        chosen.setSelected(true);
        return requestService.toResponse(request);
    }

    private static AiDesignImage firstImage(AiDesignResponse answer) {
        if (answer.images() == null || answer.images().isEmpty() || answer.images().get(0).imageBase64() == null
                || answer.images().get(0).mimeType() == null) {
            throw new AiServiceException(502, "invalid_response", "The AI service sent no image");
        }
        return answer.images().get(0);
    }

    static AiDesignRequest toAiRequest(CustomRequest request) {
        List<AiDesignColor> colors = request.getColors().stream()
                .filter(c -> c.getHex() != null)
                .limit(8)
                .map(c -> new AiDesignColor(c.getNameEs(), c.getHex()))
                .toList();
        List<String> motifs = request.getMotifs().stream().limit(10).map(m -> m.getNameEs()).toList();
        String language = Locale.ENGLISH.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage()) ? "en" : "es";
        return new AiDesignRequest(request.getCustomRequestId(), cut(request.getTitle(), 120),
                cut(request.getDescription(), 1000), cut(request.getShape().getNameEs(), 60),
                cut(request.getStyle().getNameEs(), 60), colors, motifs, cut(request.getSizeNotes(), 200), language,
                IMAGES_PER_CALL);
    }

    private static String cut(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.strip();
        return text.length() <= max ? text : text.substring(0, max);
    }

    private void logUsage(CustomRequestDesign design, long start) {
        AiUsageLog usage = new AiUsageLog();
        usage.setFunctionName(USAGE_FUNCTION);
        usage.setProvider(design.getProvider());
        usage.setModel(design.getModel());
        usage.setSuccess(design.getStatus() == DesignStatus.COMPLETED);
        usage.setLatencyMs((int) Math.min(Integer.MAX_VALUE, (System.nanoTime() - start) / 1_000_000));
        usageRepository.save(usage);
    }
}
