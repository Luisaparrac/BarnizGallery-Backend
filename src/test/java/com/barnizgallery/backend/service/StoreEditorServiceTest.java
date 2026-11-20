package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.CreateStoreRequest;
import com.barnizgallery.backend.dto.StoreEditorResponse;
import com.barnizgallery.backend.dto.StoreSectionResponse;
import com.barnizgallery.backend.dto.UpdateStoreRequest;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.StoreSection;
import com.barnizgallery.backend.entity.StoreTemplate;
import com.barnizgallery.backend.enums.StoreStatus;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.StoreRepository;
import com.barnizgallery.backend.repository.StoreSectionRepository;
import com.barnizgallery.backend.repository.StoreTemplateRepository;
import com.barnizgallery.backend.security.CurrentUser;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Store editor of the logged-in artisan, with repositories in memory (mocks). */
class StoreEditorServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

    private final StoreRepository stores = mock(StoreRepository.class);
    private final StoreTemplateRepository templates = mock(StoreTemplateRepository.class);
    private final StoreSectionRepository sections = mock(StoreSectionRepository.class);
    private final MasterRepository masters = mock(MasterRepository.class);
    private final StoreEditorService editor = new StoreEditorService(stores, templates, sections, masters,
            new CurrentUser(), Clock.fixed(NOW, ZoneOffset.UTC));
    private final StoreSectionService sectionEditor = new StoreSectionService(sections, editor);

    private final Master master = TestEntities.master(70);
    private final Store store = TestEntities.store(5, master);
    private final List<StoreSection> saved = new ArrayList<>();

    @BeforeEach
    void loginAsArtisan() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("7").claim("role", "ARTISAN")
                .issuedAt(NOW).expiresAt(NOW.plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        when(masters.findByUserUserId(7L)).thenReturn(Optional.of(master));
        store.setTemplate(TestEntities.template(1));
        when(stores.findFirstByMasterMasterIdOrderByStoreIdAsc(70L)).thenReturn(Optional.of(store));
        when(sections.findByStoreStoreIdOrderBySortOrderAscStoreSectionIdAsc(5L)).thenReturn(saved);
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createCopiesTheTemplateThemeAndSectionsWithAFreeSlug() {
        StoreTemplate template = TestEntities.template(2);
        template.setActive(true);
        template.setDefaultTheme("{\"primary\":\"#8a1c1c\"}");
        template.setDefaultLayout("{\"sections\":[{\"type\":\"hero\",\"titleEs\":\"Bienvenidos\"},"
                + "{\"titleEs\":\"sin tipo\"},{\"type\":\"story\",\"config\":{\"columns\":2}}]}");
        when(stores.existsByMasterMasterId(70L)).thenReturn(false);
        when(templates.findByCode("clasica")).thenReturn(Optional.of(template));
        when(stores.existsBySlug("taller-narino")).thenReturn(true);
        when(stores.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sections.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        StoreEditorResponse created = editor.create(new CreateStoreRequest("clasica", "Taller Nariño", "Nariño Workshop",
                null, null, null, null));

        assertThat(created.status()).isEqualTo(StoreStatus.DRAFT);
        assertThat(created.slug()).isEqualTo("taller-narino-2");
        assertThat(created.theme()).isEqualTo("{\"primary\":\"#8a1c1c\"}");
        assertThat(created.sections()).extracting(StoreSectionResponse::sectionType).containsExactly("hero", "story");
        assertThat(created.sections()).extracting(StoreSectionResponse::sortOrder).containsExactly(0, 1);
        assertThat(created.sections().get(1).config()).isEqualTo("{\"columns\":2}");
    }

    @Test
    void createFailsWhenTheMasterAlreadyHasAStore() {
        when(stores.existsByMasterMasterId(70L)).thenReturn(true);

        assertThatThrownBy(() -> editor.create(new CreateStoreRequest("clasica", "A", "A", null, null, null, null)))
                .hasMessage("master_already_has_store [70]");
    }

    @Test
    void updateRejectsATakenSlugAndAThemeThatIsNotAnObject() {
        when(stores.existsBySlug("ocupado")).thenReturn(true);

        assertThatThrownBy(() -> editor.update(update("ocupado", null)))
                .hasMessage("slug_taken [ocupado]");
        assertThatThrownBy(() -> editor.update(update(null, JsonMapper.builder().build().readTree("[1]"))))
                .hasMessage("json_object_required");
    }

    @Test
    void publishSetsTheDateAndTranslatesTheDatabaseRuleTo409() {
        SQLException raised = new SQLException("La tienda solo puede publicarse cuando el maestro está aprobado",
                "P0001");
        when(stores.saveAndFlush(store)).thenThrow(new JpaSystemException(new RuntimeException(raised)));

        assertThatThrownBy(editor::publish).hasMessage("store_master_not_approved");

        doReturn(store).when(stores).saveAndFlush(store);
        store.setStatus(StoreStatus.DRAFT);
        StoreEditorResponse published = editor.publish();

        assertThat(published.status()).isEqualTo(StoreStatus.PUBLISHED);
        assertThat(published.publishedAt()).isEqualTo(NOW);
        store.setStatus(StoreStatus.SUSPENDED);
        assertThatThrownBy(editor::publish).hasMessage("store_suspended [5]");
    }

    @Test
    void reorderNeedsEverySectionOnceAndSectionsOfOtherStoresAreNotFound() {
        saved.add(section(1, store, 0));
        saved.add(section(2, store, 1));
        when(sections.findById(9L)).thenReturn(Optional.of(section(9, TestEntities.store(6, master), 0)));

        assertThatThrownBy(() -> sectionEditor.reorder(List.of(2L)))
                .hasMessage("section_order_invalid");
        assertThatThrownBy(() -> sectionEditor.reorder(List.of(2L, 2L))).hasMessage("section_order_invalid");
        List<StoreSectionResponse> ordered = sectionEditor.reorder(List.of(2L, 1L));
        assertThat(ordered).extracting(StoreSectionResponse::sectionId).containsExactly(2L, 1L);
        assertThat(ordered).extracting(StoreSectionResponse::sortOrder).containsExactly(0, 1);
        assertThatThrownBy(() -> sectionEditor.setVisible(9L, false)).hasMessage("store_section_not_found [9]");
    }

    private static UpdateStoreRequest update(String slug, JsonNode theme) {
        return new UpdateStoreRequest(null, null, null, null, slug, null, null, theme, null);
    }

    private static StoreSection section(long id, Store owner, int order) {
        StoreSection section = new StoreSection();
        section.setStoreSectionId(id);
        section.setStore(owner);
        section.setSectionType("story");
        section.setSortOrder(order);
        section.setVisible(true);
        return section;
    }
}
