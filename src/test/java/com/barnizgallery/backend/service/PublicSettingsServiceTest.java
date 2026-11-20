package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class PublicSettingsServiceTest {

    @Test
    void onlyTheListedHarmlessSettingsArePublic() {
        AppSettingsService settings = mock(AppSettingsService.class);
        JsonMapper json = JsonMapper.builder().build();
        when(settings.find(anyString())).thenReturn(Optional.empty());
        when(settings.find("currency.default")).thenReturn(Optional.of(json.readTree("\"USD\"")));
        when(settings.find("shipping.fixed_amount.COP")).thenReturn(Optional.of(json.readTree("15000")));
        when(settings.keysStartingWith("shipping.fixed_amount.")).thenReturn(List.of("shipping.fixed_amount.COP"));

        Map<String, JsonNode> values = new PublicSettingsService(settings).values();

        assertThat(values).containsOnlyKeys("currency.default", "languages.available", "auction.min_increment",
                "cart.reservation_minutes", "shipping.fixed_amount", "shipping.fixed_amount.COP");
        assertThat(values.get("currency.default").asString()).isEqualTo("USD");
        assertThat(values.get("cart.reservation_minutes")).isNull();
        assertThat(values).doesNotContainKeys("payments.active_gateway", "platform.commission_rate", "ai.providers");
    }
}
