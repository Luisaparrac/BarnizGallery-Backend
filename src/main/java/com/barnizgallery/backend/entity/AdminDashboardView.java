package com.barnizgallery.backend.entity;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Counters for the admin dashboard (read-only view {@code museo.v_admin_dashboard}).
 * <p>
 * The view is a single row without a key column, so it is read with {@code @Subselect} adding the constant
 * key {@code dashboard_id = 1} instead of {@code @Table}. The query names the {@code museo} schema.
 */
@Entity
@Immutable
@Subselect("select 1::bigint as dashboard_id, d.* from museo.v_admin_dashboard d")
@Synchronize({ "users", "masters", "stores", "artworks", "orders", "custom_requests",
        "content_reports" })
public class AdminDashboardView {

    @Id
    @Column(name = "dashboard_id")
    private Long dashboardId;

    @Column(name = "users_total")
    private long usersTotal;

    @Column(name = "masters_pending")
    private long mastersPending;

    @Column(name = "stores_published")
    private long storesPublished;

    @Column(name = "artworks_public")
    private long artworksPublic;

    @Column(name = "orders_total")
    private long ordersTotal;

    @Column(name = "custom_requests_open")
    private long customRequestsOpen;

    @Column(name = "reports_total")
    private long reportsTotal;

    public Long getDashboardId() {
        return dashboardId;
    }

    public long getUsersTotal() {
        return usersTotal;
    }

    public long getMastersPending() {
        return mastersPending;
    }

    public long getStoresPublished() {
        return storesPublished;
    }

    public long getArtworksPublic() {
        return artworksPublic;
    }

    public long getOrdersTotal() {
        return ordersTotal;
    }

    public long getCustomRequestsOpen() {
        return customRequestsOpen;
    }

    public long getReportsTotal() {
        return reportsTotal;
    }
}
