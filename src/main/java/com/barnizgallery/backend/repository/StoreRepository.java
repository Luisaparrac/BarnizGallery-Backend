package com.barnizgallery.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Store;

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
}
