package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.entity.Color;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.repository.AvatarRepository;
import com.barnizgallery.backend.repository.BudgetRangeRepository;
import com.barnizgallery.backend.repository.ColorRepository;
import com.barnizgallery.backend.repository.MotifRepository;
import com.barnizgallery.backend.repository.ShapeRepository;
import com.barnizgallery.backend.repository.StyleRepository;

class CatalogCodeResolverTest {

    private final ColorRepository colorRepository = mock(ColorRepository.class);
    private final BudgetRangeRepository budgetRangeRepository = mock(BudgetRangeRepository.class);
    private final CatalogCodeResolver resolver = new CatalogCodeResolver(colorRepository, mock(MotifRepository.class),
            mock(ShapeRepository.class), mock(StyleRepository.class), budgetRangeRepository,
            mock(AvatarRepository.class));

    private static Color color(String code) {
        Color color = new Color();
        color.setCode(code);
        return color;
    }

    @Test
    void resolvesKnownCodes() {
        when(colorRepository.findByCodeIn(anyCollection())).thenReturn(List.of(color("rojo"), color("azul")));

        assertThat(resolver.colors(List.of("rojo", "azul", "rojo"))).extracting(Color::getCode)
                .containsExactlyInAnyOrder("rojo", "azul");
    }

    @Test
    void rejectsUnknownCodes() {
        when(colorRepository.findByCodeIn(anyCollection())).thenReturn(List.of(color("rojo")));

        assertThatThrownBy(() -> resolver.colors(List.of("rojo", "fucsia")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("fucsia");
    }

    @Test
    void deactivatedCodesAreRejectedLikeUnknownOnes() {
        Color inactive = color("gris");
        inactive.setActive(false);
        when(colorRepository.findByCodeIn(anyCollection())).thenReturn(List.of(color("rojo"), inactive));

        assertThatThrownBy(() -> resolver.colors(List.of("rojo", "gris")))
                .hasMessage("unknown_color_codes [gris]");
    }

    @Test
    void emptyOrMissingCodesGiveAnEmptySetWithoutQueries() {
        assertThat(resolver.colors(null)).isEmpty();
        assertThat(resolver.colors(List.of())).isEmpty();
        verifyNoInteractions(colorRepository);
    }

    @Test
    void budgetRangeIsOptionalButMustExist() {
        when(budgetRangeRepository.findByCode("999+")).thenReturn(Optional.empty());

        assertThat(resolver.budgetRange(null)).isNull();
        assertThatThrownBy(() -> resolver.budgetRange("999+"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("999+");
    }
}
