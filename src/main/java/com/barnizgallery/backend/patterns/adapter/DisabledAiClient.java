package com.barnizgallery.backend.patterns.adapter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.exception.FeatureDisabledException;

/**
 * <b>Adapter pattern – default implementation of the Target.</b>
 * <p>
 * Used while {@code AI_PROVIDER} is {@code none} (the default). It never calls an
 * external service and never invents answers. When the group chooses a provider,
 * a real adapter (for example {@code GeminiAdapter}) is added and activated with
 * {@code @ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")}.
 */
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "none", matchIfMissing = true)
public class DisabledAiClient implements AiTextClient {

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        throw new FeatureDisabledException("AI is disabled: no AI provider is configured");
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
