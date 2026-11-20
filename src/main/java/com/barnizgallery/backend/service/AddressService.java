package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AddressRequest;
import com.barnizgallery.backend.dto.AddressResponse;
import com.barnizgallery.backend.entity.Address;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.AddressMapper;
import com.barnizgallery.backend.repository.AddressRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * Shipping addresses of the logged-in user. Orders keep a copy of the address, so editing or deleting one does not
 * change past orders. An address of another user is answered as not found.
 */
@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository,
            CurrentUser currentUser) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> mine() {
        return addressRepository.findByUserUserIdOrderByAddressIdAsc(currentUser.requireId()).stream()
                .map(AddressMapper::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse create(AddressRequest request) {
        Address address = new Address();
        address.setUser(userRepository.getReferenceById(currentUser.requireId()));
        AddressMapper.apply(request, address);
        return AddressMapper.toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long addressId, AddressRequest request) {
        Address address = myAddress(addressId);
        AddressMapper.apply(request, address);
        return AddressMapper.toResponse(address);
    }

    @Transactional
    public void delete(Long addressId) {
        addressRepository.delete(myAddress(addressId));
    }

    /** An address of the logged-in user (404 otherwise). */
    @Transactional(readOnly = true)
    public Address myAddress(Long addressId) {
        Long userId = currentUser.requireId();
        return addressRepository.findById(addressId)
                .filter(address -> address.getUser().getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ADDRESS_NOT_FOUND, addressId));
    }
}
