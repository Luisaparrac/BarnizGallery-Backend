package com.barnizgallery.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.StoreStatus;

/**
 * Spring Data repository for {@link Store}.
 */
public interface StoreRepository extends JpaRepository<Store, Long> {

    /** All stores with their master loaded in the same query (avoids N+1). */
    @Query("select r from Store r join fetch r.master order by r.storeId")
    List<Store> findAllWithMaster();

    @Query("select r from Store r join fetch r.master where r.storeId = :storeId")
    Optional<Store> findByIdWithMaster(Long storeId);

    Optional<Store> findFirstByMasterMasterIdOrderByStoreIdAsc(Long masterId);

    boolean existsByMasterMasterId(Long masterId);

    boolean existsBySlug(String slug);

    /** Stores with the given status whose master has the given verification, with the master loaded. */
    @Query("select s from Store s join fetch s.master m where s.status = :status "
            + "and m.verificationStatus = :verification order by s.publishedAt desc, s.storeId")
    List<Store> findByStatusWithMaster(StoreStatus status, MasterVerificationStatus verification);

    /** A store by slug with its master, when it has the given status and its master the given verification. */
    @Query("select s from Store s join fetch s.master m where s.slug = :slug and s.status = :status "
            + "and m.verificationStatus = :verification")
    Optional<Store> findBySlugAndStatus(String slug, StoreStatus status, MasterVerificationStatus verification);
}
