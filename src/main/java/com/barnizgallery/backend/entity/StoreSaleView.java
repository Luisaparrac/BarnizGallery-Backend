package com.barnizgallery.backend.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.Immutable;

import com.barnizgallery.backend.converter.OrderStatusConverter;
import com.barnizgallery.backend.converter.OrderTypeConverter;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.OrderType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One sold order line with its store and order data (read-only view {@code museo.v_store_sales}).
 */
@Entity
@Immutable
@Table(name = "v_store_sales")
public class StoreSaleView {

    @Id
    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "store_slug")
    private String storeSlug;

    @Column(name = "store_name_es", length = 150)
    private String storeNameEs;

    @Column(name = "master_id")
    private Long masterId;

    @Column(name = "master_name", length = 100)
    private String masterName;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_code")
    private String orderCode;

    @Convert(converter = OrderTypeConverter.class)
    @Column(name = "order_type", length = 20)
    private OrderType orderType;

    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "order_status", length = 30)
    private OrderStatus orderStatus;

    @Column(name = "order_currency", length = 3)
    private String orderCurrency;

    @Column(name = "line_total", precision = 14, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "item_currency", length = 3)
    private String itemCurrency;

    @Column(name = "artisan_gross_amount", precision = 14, scale = 2)
    private BigDecimal artisanGrossAmount;

    @Column(name = "platform_fee_amount", precision = 14, scale = 2)
    private BigDecimal platformFeeAmount;

    @Column(name = "artisan_net_amount", precision = 14, scale = 2)
    private BigDecimal artisanNetAmount;

    @Column(name = "fulfillment_status", length = 30)
    private String fulfillmentStatus;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "tracking_number")
    private String trackingNumber;

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public String getStoreSlug() {
        return storeSlug;
    }

    public String getStoreNameEs() {
        return storeNameEs;
    }

    public Long getMasterId() {
        return masterId;
    }

    public String getMasterName() {
        return masterName;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public String getOrderCurrency() {
        return orderCurrency;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public String getItemCurrency() {
        return itemCurrency;
    }

    public BigDecimal getArtisanGrossAmount() {
        return artisanGrossAmount;
    }

    public BigDecimal getPlatformFeeAmount() {
        return platformFeeAmount;
    }

    public BigDecimal getArtisanNetAmount() {
        return artisanNetAmount;
    }

    public String getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}
