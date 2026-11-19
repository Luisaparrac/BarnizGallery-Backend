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
import com.barnizgallery.backend.enums.AuthTokenPurpose;
import com.barnizgallery.backend.enums.BidReviewStatus;
import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.enums.InventoryReservationStatus;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.OfferStatus;
import com.barnizgallery.backend.enums.OfferType;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.OrderType;
import com.barnizgallery.backend.enums.PaymentObligationStage;
import com.barnizgallery.backend.enums.PaymentObligationStatus;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.enums.SaleMode;
import com.barnizgallery.backend.enums.StoreStatus;
import com.barnizgallery.backend.enums.UserRole;
import com.barnizgallery.backend.enums.UserStatus;
import com.barnizgallery.backend.enums.VerificationStep;
import com.barnizgallery.backend.enums.VerificationStepStatus;

class EnumConvertersTest {

    static Stream<Arguments> mappings() {
        return Stream.of(
                Arguments.of(new ArtworkStatusConverter(), ArtworkStatus.DRAFT, "borrador"),
                Arguments.of(new ArtworkStatusConverter(), ArtworkStatus.EXHIBITED, "exhibida"),
                Arguments.of(new ArtworkStatusConverter(), ArtworkStatus.IN_AUCTION, "subasta"),
                Arguments.of(new ArtworkStatusConverter(), ArtworkStatus.RESERVED, "reservada"),
                Arguments.of(new ArtworkStatusConverter(), ArtworkStatus.SOLD, "vendida"),
                Arguments.of(new ArtworkStatusConverter(), ArtworkStatus.ARCHIVED, "archivada"),
                Arguments.of(new AuctionStatusConverter(), AuctionStatus.SCHEDULED, "programada"),
                Arguments.of(new AuctionStatusConverter(), AuctionStatus.ACTIVE, "activa"),
                Arguments.of(new AuctionStatusConverter(), AuctionStatus.FINISHED, "finalizada"),
                Arguments.of(new AuctionStatusConverter(), AuctionStatus.CANCELLED, "cancelada"),
                Arguments.of(new InteractionActionConverter(), InteractionAction.VIEW, "ver"),
                Arguments.of(new InteractionActionConverter(), InteractionAction.ZOOM, "ampliar"),
                Arguments.of(new InteractionActionConverter(), InteractionAction.BID, "ofertar"),
                Arguments.of(new UserRoleConverter(), UserRole.ADMIN, "administrador"),
                Arguments.of(new UserRoleConverter(), UserRole.ARTISAN, "artesano"),
                Arguments.of(new UserRoleConverter(), UserRole.CUSTOMER, "cliente"),
                Arguments.of(new UserStatusConverter(), UserStatus.PENDING, "pendiente"),
                Arguments.of(new UserStatusConverter(), UserStatus.ACTIVE, "activo"),
                Arguments.of(new UserStatusConverter(), UserStatus.SUSPENDED, "suspendido"),
                Arguments.of(new UserStatusConverter(), UserStatus.DELETED, "eliminado"),
                Arguments.of(new AuthTokenPurposeConverter(), AuthTokenPurpose.EMAIL_VERIFICATION, "verificar_correo"),
                Arguments.of(new AuthTokenPurposeConverter(), AuthTokenPurpose.PASSWORD_RESET, "recuperar_clave"),
                Arguments.of(new AuthTokenPurposeConverter(), AuthTokenPurpose.REFRESH, "refresco"),
                Arguments.of(new MasterVerificationStatusConverter(), MasterVerificationStatus.PENDING, "pendiente"),
                Arguments.of(new MasterVerificationStatusConverter(), MasterVerificationStatus.IN_REVIEW, "en_revision"),
                Arguments.of(new MasterVerificationStatusConverter(), MasterVerificationStatus.APPROVED, "aprobado"),
                Arguments.of(new MasterVerificationStatusConverter(), MasterVerificationStatus.REJECTED, "rechazado"),
                Arguments.of(new VerificationStepConverter(), VerificationStep.EMAIL, "correo"),
                Arguments.of(new VerificationStepConverter(), VerificationStep.IDENTITY, "identidad"),
                Arguments.of(new VerificationStepConverter(), VerificationStep.WORKSHOP, "taller"),
                Arguments.of(new VerificationStepConverter(), VerificationStep.SAMPLES, "muestras"),
                Arguments.of(new VerificationStepConverter(), VerificationStep.ADMIN_APPROVAL, "aprobacion_admin"),
                Arguments.of(new VerificationStepStatusConverter(), VerificationStepStatus.PENDING, "pendiente"),
                Arguments.of(new VerificationStepStatusConverter(), VerificationStepStatus.SUBMITTED, "enviado"),
                Arguments.of(new VerificationStepStatusConverter(), VerificationStepStatus.IN_REVIEW, "en_revision"),
                Arguments.of(new VerificationStepStatusConverter(), VerificationStepStatus.APPROVED, "aprobado"),
                Arguments.of(new VerificationStepStatusConverter(), VerificationStepStatus.REJECTED, "rechazado"),
                Arguments.of(new StoreStatusConverter(), StoreStatus.DRAFT, "borrador"),
                Arguments.of(new StoreStatusConverter(), StoreStatus.PUBLISHED, "publicada"),
                Arguments.of(new StoreStatusConverter(), StoreStatus.SUSPENDED, "suspendida"),
                Arguments.of(new SaleModeConverter(), SaleMode.EXHIBITION, "exhibicion"),
                Arguments.of(new SaleModeConverter(), SaleMode.SALE, "venta"),
                Arguments.of(new SaleModeConverter(), SaleMode.AUCTION, "subasta"),
                Arguments.of(new OrderTypeConverter(), OrderType.PURCHASE, "compra"),
                Arguments.of(new OrderTypeConverter(), OrderType.AUCTION, "subasta"),
                Arguments.of(new OrderTypeConverter(), OrderType.CUSTOM, "encargo"),
                Arguments.of(new OrderStatusConverter(), OrderStatus.PENDING_PAYMENT, "pendiente_pago"),
                Arguments.of(new OrderStatusConverter(), OrderStatus.PAID, "pagado"),
                Arguments.of(new OrderStatusConverter(), OrderStatus.IN_PREPARATION, "en_preparacion"),
                Arguments.of(new OrderStatusConverter(), OrderStatus.SHIPPED, "enviado"),
                Arguments.of(new OrderStatusConverter(), OrderStatus.DELIVERED, "entregado"),
                Arguments.of(new OrderStatusConverter(), OrderStatus.CANCELLED, "cancelado"),
                Arguments.of(new OrderStatusConverter(), OrderStatus.REFUNDED, "reembolsado"),
                Arguments.of(new PaymentStatusConverter(), PaymentStatus.PENDING, "pendiente"),
                Arguments.of(new PaymentStatusConverter(), PaymentStatus.APPROVED, "aprobado"),
                Arguments.of(new PaymentStatusConverter(), PaymentStatus.DECLINED, "rechazado"),
                Arguments.of(new PaymentStatusConverter(), PaymentStatus.EXPIRED, "expirado"),
                Arguments.of(new PaymentStatusConverter(), PaymentStatus.REFUNDED, "reembolsado"),
                Arguments.of(new BidReviewStatusConverter(), BidReviewStatus.VALID, "valida"),
                Arguments.of(new BidReviewStatusConverter(), BidReviewStatus.SUSPICIOUS, "sospechosa"),
                Arguments.of(new PaymentObligationStageConverter(), PaymentObligationStage.SINGLE_PAYMENT, "pago_unico"),
                Arguments.of(new PaymentObligationStageConverter(), PaymentObligationStage.DEPOSIT, "anticipo"),
                Arguments.of(new PaymentObligationStageConverter(), PaymentObligationStage.BALANCE, "saldo"),
                Arguments.of(new PaymentObligationStatusConverter(), PaymentObligationStatus.PENDING, "pendiente"),
                Arguments.of(new PaymentObligationStatusConverter(), PaymentObligationStatus.PAID, "pagada"),
                Arguments.of(new PaymentObligationStatusConverter(), PaymentObligationStatus.CANCELLED, "cancelada"),
                Arguments.of(new PaymentObligationStatusConverter(), PaymentObligationStatus.OVERDUE, "vencida"),
                Arguments.of(new InventoryReservationStatusConverter(), InventoryReservationStatus.ACTIVE, "activa"),
                Arguments.of(new InventoryReservationStatusConverter(), InventoryReservationStatus.RELEASED, "liberada"),
                Arguments.of(new InventoryReservationStatusConverter(), InventoryReservationStatus.CONSUMED, "consumida"),
                Arguments.of(new InventoryReservationStatusConverter(), InventoryReservationStatus.EXPIRED, "expirada"),
                Arguments.of(new OfferTypeConverter(), OfferType.ACCEPTS_PRICE, "acepta_precio"),
                Arguments.of(new OfferTypeConverter(), OfferType.COUNTER_OFFER, "contraoferta"),
                Arguments.of(new OfferStatusConverter(), OfferStatus.ACTIVE, "activa"),
                Arguments.of(new OfferStatusConverter(), OfferStatus.ACCEPTED, "aceptada"),
                Arguments.of(new OfferStatusConverter(), OfferStatus.REJECTED, "rechazada"),
                Arguments.of(new OfferStatusConverter(), OfferStatus.EXPIRED, "expirada"),
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
