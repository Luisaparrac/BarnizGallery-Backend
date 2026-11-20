package com.barnizgallery.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuctionMapperTest {

    @Test
    void bidderNamesAreMaskedInThePublicHistory() {
        assertThat(AuctionMapper.maskName("Ana Cabrera")).isEqualTo("A*** C.");
        assertThat(AuctionMapper.maskName("Emma  María Wilson")).isEqualTo("E*** W.");
        assertThat(AuctionMapper.maskName("Ana")).isEqualTo("A***");
        assertThat(AuctionMapper.maskName(" ")).isEqualTo("***");
    }
}
