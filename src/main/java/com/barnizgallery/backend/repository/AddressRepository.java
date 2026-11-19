package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Address;

/**
 * Spring Data repository for {@link Address}.
 */
public interface AddressRepository extends JpaRepository<Address, Long> {
}
