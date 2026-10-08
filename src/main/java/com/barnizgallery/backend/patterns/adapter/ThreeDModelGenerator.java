package com.barnizgallery.backend.patterns.adapter;

import java.util.List;
import java.util.Optional;

import com.barnizgallery.backend.enums.GenerationStatus;

/**
 * <b>Adapter pattern – Target.</b>
 * <p>
 * What the system needs from a photo-to-3D service, in its own terms: send photos,
 * ask for the status (using our {@link GenerationStatus} enum) and get the GLB URL.
 * {@link Hyper3dRodinAdapter} translates these calls to the Hyper3D Rodin REST API.
 */
public interface ThreeDModelGenerator {

    /** Sends the photos (public URLs) and starts the generation. */
    GenerationTicket submit(List<String> photoUrls);

    GenerationStatus checkStatus(GenerationTicket ticket);

    /** URL of the generated GLB file, if it is ready. */
    Optional<String> fetchGlbUrl(GenerationTicket ticket);

    /** False when no API key is configured. */
    boolean isEnabled();
}
