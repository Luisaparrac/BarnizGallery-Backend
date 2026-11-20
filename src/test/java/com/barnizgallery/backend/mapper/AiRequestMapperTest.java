package com.barnizgallery.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.enums.ArtworkStatus;

class AiRequestMapperTest {

    @Test
    void candidatesCarryTheSlugOrOneMadeFromTheTitleWithoutSavingIt() {
        Artwork withSlug = TestEntities.artwork(1, TestEntities.store(1, TestEntities.master(1)), ArtworkStatus.EXHIBITED);
        withSlug.setSlug("jarron-colibri");
        Artwork withoutSlug = TestEntities.artwork(2, withSlug.getStore(), ArtworkStatus.EXHIBITED);
        withoutSlug.setTitleEs("Bargueño Colonial");

        assertThat(AiRequestMapper.toCandidate(withSlug, 0).slug()).isEqualTo("jarron-colibri");
        assertThat(AiRequestMapper.toCandidate(withoutSlug, 0).slug()).isEqualTo("bargueno-colonial");
        assertThat(withoutSlug.getSlug()).isNull();
    }
}
