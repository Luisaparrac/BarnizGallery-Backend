package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.barnizgallery.backend.dto.RegisterArtisanRequest;
import com.barnizgallery.backend.dto.RegisterRequest;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;

/** Sign-up by role as the frontend sends it. */
class SignUpServiceTest {

    private final RegistrationService customers = mock(RegistrationService.class);
    private final ArtisanRegistrationService artisans = mock(ArtisanRegistrationService.class);
    private final SignUpService service = new SignUpService(customers, artisans);

    @Test
    void withoutRoleOrAsCustomerACustomerIsCreated() {
        RegisterRequest none = request(null);
        RegisterRequest customer = request(UserRole.CUSTOMER);

        service.register(none);
        service.register(customer);

        verify(customers).register(none);
        verify(customers).register(customer);
        verify(artisans, never()).register(any());
    }

    @Test
    void anArtisanGetsTheArtisanSignUpWithWorkshopAndCity() {
        service.register(request(UserRole.ARTISAN));

        ArgumentCaptor<RegisterArtisanRequest> sent = ArgumentCaptor.forClass(RegisterArtisanRequest.class);
        verify(artisans).register(sent.capture());
        assertThat(sent.getValue().workshop()).isEqualTo("Taller Ana");
        assertThat(sent.getValue().city()).isEqualTo("Pasto");
        assertThat(sent.getValue().account().displayName()).isEqualTo("Ana Maestra");
        verify(customers, never()).register(any());
    }

    @Test
    void administratorsAreNeverCreatedBySignUp() {
        assertThatThrownBy(() -> service.register(request(UserRole.ADMIN))).hasMessage("forbidden");
        verify(customers, never()).register(any());
        verify(artisans, never()).register(any());
    }

    private static RegisterRequest request(UserRole role) {
        return new RegisterRequest(null, "ana@taller.test", "una-clave-segura", "CO", Language.ES, "Ana Maestra", role,
                null, "Taller Ana", "Pasto");
    }
}
