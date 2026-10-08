package com.barnizgallery.backend.patterns.adapter;

/**
 * <b>Adapter pattern – Target.</b>
 * <p>
 * The interface the rest of the system uses to talk to an AI text model.
 * Each provider (Gemini, Claude, OpenAI...) has its own REST format; an Adapter
 * per provider translates this simple call into the provider's API.
 * The provider has not been chosen yet, so only {@link DisabledAiClient} exists.
 */
public interface AiTextClient {

    /**
     * Sends a prompt and returns the text answer.
     *
     * @param systemPrompt instructions for the model (role, output format)
     * @param userPrompt   the actual question with the data
     */
    String complete(String systemPrompt, String userPrompt);

    /** True when a provider is configured and can be called. */
    boolean isEnabled();
}
