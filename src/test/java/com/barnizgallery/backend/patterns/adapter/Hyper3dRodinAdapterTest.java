package com.barnizgallery.backend.patterns.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.config.Hyper3dProperties;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.model.enums.GenerationStatus;

/**
 * Tests of the adapter logic that does not need the network.
 */
class Hyper3dRodinAdapterTest {

    private static Hyper3dRodinAdapter adapter(String apiKey) {
        return new Hyper3dRodinAdapter(new Hyper3dProperties(apiKey, "https://api.hyper3d.com"));
    }

    @Test
    void isDisabledWithoutApiKey() {
        Hyper3dRodinAdapter adapter = adapter("");

        assertThat(adapter.isEnabled()).isFalse();
        assertThatThrownBy(() -> adapter.submit(List.of("https://img/1.jpg")))
                .isInstanceOf(FeatureDisabledException.class);
    }

    @Test
    void isEnabledWithApiKey() {
        assertThat(adapter("secret").isEnabled()).isTrue();
    }

    @Test
    void mapsHyper3dStatusesToOurEnum() {
        assertThat(Hyper3dRodinAdapter.mapStatuses(List.of("Done", "Done"))).isEqualTo(GenerationStatus.COMPLETED);
        assertThat(Hyper3dRodinAdapter.mapStatuses(List.of("Done", "Generating")))
                .isEqualTo(GenerationStatus.PROCESSING);
        assertThat(Hyper3dRodinAdapter.mapStatuses(List.of("Waiting"))).isEqualTo(GenerationStatus.PROCESSING);
        assertThat(Hyper3dRodinAdapter.mapStatuses(List.of("Done", "Failed"))).isEqualTo(GenerationStatus.FAILED);
        assertThat(Hyper3dRodinAdapter.mapStatuses(List.of())).isEqualTo(GenerationStatus.PROCESSING);
    }

    @Test
    void ticketIsStoredInOneColumnAndReadBack() {
        GenerationTicket ticket = new GenerationTicket("task-1", "key-1");

        assertThat(ticket.encode()).isEqualTo("task-1|key-1");
        assertThat(GenerationTicket.decode(ticket.encode())).isEqualTo(ticket);
    }

    @Test
    void ticketKeepsOnlyTheUuidWhenTheKeyDoesNotFit() {
        GenerationTicket ticket = new GenerationTicket("task-1", "k".repeat(200));

        assertThat(ticket.encode()).isEqualTo("task-1");
        assertThat(GenerationTicket.decode("task-1").subscriptionKey()).isNull();
    }
}
