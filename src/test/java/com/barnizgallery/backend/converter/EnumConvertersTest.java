package com.barnizgallery.backend.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.enums.CameraMode;
import com.barnizgallery.backend.enums.GenerationStatus;
import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.enums.Language;

class EnumConvertersTest {

    static Stream<Arguments> mappings() {
        return Stream.of(
                Arguments.of(new ArtworkStatusConverter(), ArtworkStatus.EXHIBITED, "exhibida"),
                Arguments.of(new ArtworkStatusConverter(), ArtworkStatus.IN_AUCTION, "subasta"),
                Arguments.of(new ArtworkStatusConverter(), ArtworkStatus.SOLD, "vendida"),
                Arguments.of(new AuctionStatusConverter(), AuctionStatus.SCHEDULED, "programada"),
                Arguments.of(new AuctionStatusConverter(), AuctionStatus.ACTIVE, "activa"),
                Arguments.of(new AuctionStatusConverter(), AuctionStatus.FINISHED, "finalizada"),
                Arguments.of(new AuctionStatusConverter(), AuctionStatus.CANCELLED, "cancelada"),
                Arguments.of(new InteractionActionConverter(), InteractionAction.VIEW, "ver"),
                Arguments.of(new InteractionActionConverter(), InteractionAction.TOUCH, "tocar"),
                Arguments.of(new InteractionActionConverter(), InteractionAction.ROTATE, "girar"),
                Arguments.of(new InteractionActionConverter(), InteractionAction.BID, "ofertar"),
                Arguments.of(new GenerationStatusConverter(), GenerationStatus.PENDING, "pendiente"),
                Arguments.of(new GenerationStatusConverter(), GenerationStatus.PROCESSING, "procesando"),
                Arguments.of(new GenerationStatusConverter(), GenerationStatus.COMPLETED, "completado"),
                Arguments.of(new GenerationStatusConverter(), GenerationStatus.FAILED, "fallido"),
                Arguments.of(new CameraModeConverter(), CameraMode.FIRST_PERSON, "primera_persona"),
                Arguments.of(new CameraModeConverter(), CameraMode.THIRD_PERSON, "tercera_persona"),
                Arguments.of(new LanguageConverter(), Language.ES, "es"),
                Arguments.of(new LanguageConverter(), Language.EN, "en"));
    }

    @ParameterizedTest
    @MethodSource("mappings")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void convertsBothWays(DbValueEnumConverter converter, Enum value, String dbValue) {
        assertThat(converter.convertToDatabaseColumn(value)).isEqualTo(dbValue);
        assertThat(converter.convertToEntityAttribute(dbValue)).isEqualTo(value);
    }

    @Test
    void nullIsKeptAsNull() {
        ArtworkStatusConverter converter = new ArtworkStatusConverter();

        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void unknownDatabaseValueFails() {
        AuctionStatusConverter converter = new AuctionStatusConverter();

        assertThatThrownBy(() -> converter.convertToEntityAttribute("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AuctionStatus");
    }
}
