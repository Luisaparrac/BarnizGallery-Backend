package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.BudgetRangeRequest;
import com.barnizgallery.backend.dto.CatalogEntryRequest;
import com.barnizgallery.backend.dto.CatalogEntryResponse;
import com.barnizgallery.backend.dto.StoreTemplateRequest;
import com.barnizgallery.backend.entity.Avatar;
import com.barnizgallery.backend.entity.CatalogEntry;
import com.barnizgallery.backend.entity.Color;
import com.barnizgallery.backend.entity.Shape;
import com.barnizgallery.backend.entity.StoreTemplate;
import com.barnizgallery.backend.enums.CatalogKind;
import com.barnizgallery.backend.repository.AvatarRepository;
import com.barnizgallery.backend.repository.BudgetRangeRepository;
import com.barnizgallery.backend.repository.ColorRepository;
import com.barnizgallery.backend.repository.MotifRepository;
import com.barnizgallery.backend.repository.ShapeRepository;
import com.barnizgallery.backend.repository.StoreTemplateRepository;
import com.barnizgallery.backend.repository.StyleRepository;

import tools.jackson.databind.json.JsonMapper;

class CatalogAdminServiceTest {

    private final ColorRepository colors = mock(ColorRepository.class);
    private final ShapeRepository shapes = mock(ShapeRepository.class);
    private final AvatarRepository avatars = mock(AvatarRepository.class);
    private final CatalogAdminService service = new CatalogAdminService(colors, shapes, mock(MotifRepository.class),
            mock(StyleRepository.class), avatars);

    @Test
    void createsAColorWithItsHexAndCode() {
        when(colors.findByCode("rojo")).thenReturn(Optional.empty());
        when(colors.save(any(Color.class))).thenAnswer(inv -> inv.getArgument(0));

        CatalogEntryResponse created = service.create(CatalogKind.COLORS,
                new CatalogEntryRequest("rojo", " Rojo ", "Red", 1, null, "#B3261E", null));

        assertThat(created).isEqualTo(new CatalogEntryResponse("rojo", "Rojo", "Red", 1, true, "#B3261E", null, null, true));
    }

    @Test
    void codesAreRequiredAndUnique() {
        when(colors.findByCode("rojo")).thenReturn(Optional.of(TestEntities.color("rojo")));

        assertThatThrownBy(() -> service.create(CatalogKind.COLORS,
                new CatalogEntryRequest(null, "Rojo", "Red", 1, true, null, null))).hasMessage("catalog_code_required");
        assertThatThrownBy(() -> service.create(CatalogKind.COLORS,
                new CatalogEntryRequest("rojo", "Rojo", "Red", 1, true, null, null)))
                .hasMessage("catalog_code_exists [rojo]");
        verify(colors, never()).save(any());
    }

    @Test
    void updateKeepsTheCodeAndCanDeactivate() {
        Shape shape = new Shape();
        shape.setCode("bandeja");
        when(shapes.findByCode("bandeja")).thenReturn(Optional.of(shape));

        CatalogEntryResponse updated = service.update(CatalogKind.SHAPES, "bandeja",
                new CatalogEntryRequest("otro-codigo", "Bandeja", "Tray", 3, false, null, null));

        assertThat(updated.code()).isEqualTo("bandeja");
        assertThat(updated.active()).isFalse();
        assertThat(updated.sortOrder()).isEqualTo(3);
        assertThatThrownBy(() -> service.update(CatalogKind.SHAPES, "nada",
                new CatalogEntryRequest(null, "a", "b", 0, true, null, null)))
                .hasMessage("catalog_entry_not_found [nada]");
    }

    @Test
    void avatarsNeedAnImage() {
        when(avatars.findByCode("colibri")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(CatalogKind.AVATARS,
                new CatalogEntryRequest("colibri", "Colibrí", "Hummingbird", 0, true, null, null)))
                .hasMessage("avatar_image_required");
    }

    @Test
    void listsEveryEntryIncludingInactiveOnes() {
        Color inactive = TestEntities.color("gris");
        inactive.setActive(false);
        when(colors.findAllByOrderBySortOrderAscCodeAsc()).thenReturn(List.of(TestEntities.color("rojo"), inactive));

        assertThat(service.list(CatalogKind.COLORS)).extracting(CatalogEntryResponse::active).containsExactly(true,
                false);
    }

    @Test
    void catalogPathsMatchTheirKinds() {
        assertThat(CatalogKind.fromPath("motifs")).contains(CatalogKind.MOTIFS);
        assertThat(CatalogKind.fromPath("budget-ranges")).isEmpty();
        assertThat((CatalogEntry) new Avatar()).isInstanceOf(CatalogEntry.class);
    }

    @Test
    void budgetRangeMaximumCannotBeBelowMinimum() {
        BudgetRangeRepository repository = mock(BudgetRangeRepository.class);
        BudgetRangeAdminService budgets = new BudgetRangeAdminService(repository);

        assertThatThrownBy(() -> budgets.create(new BudgetRangeRequest("x", new BigDecimal("100"), BigDecimal.TEN,
                "USD"))).hasMessage("budget_range_invalid");
    }

    @Test
    void storeTemplatesKeepJsonObjectsOnly() {
        StoreTemplateRepository repository = mock(StoreTemplateRepository.class);
        StoreTemplateAdminService templates = new StoreTemplateAdminService(repository);
        StoreTemplate template = TestEntities.template(1);
        when(repository.findByCode("vitrina")).thenReturn(Optional.of(template));
        JsonMapper json = JsonMapper.builder().build();

        templates.update("vitrina", new StoreTemplateRequest(null, "Vitrina", "Showcase", null, null, null,
                json.readTree("{\"sections\": [\"hero\", \"gallery\"]}"), null, true));

        assertThat(template.getDefaultLayout()).isEqualTo("{\"sections\":[\"hero\",\"gallery\"]}");
        assertThat(template.getDefaultTheme()).isEqualTo("{}");
        assertThatThrownBy(() -> templates.update("vitrina", new StoreTemplateRequest(null, "Vitrina", "Showcase",
                null, null, null, json.readTree("[1, 2]"), null, true))).hasMessage("template_json_invalid");
    }
}
