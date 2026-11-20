package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.BudgetRangeRequest;
import com.barnizgallery.backend.dto.BudgetRangeResponse;
import com.barnizgallery.backend.entity.BudgetRange;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.CatalogMapper;
import com.barnizgallery.backend.repository.BudgetRangeRepository;

/**
 * Administration of the budget ranges of the taste questionnaire. They have no active flag, so an unused range
 * can be deleted; a range chosen in some taste profile is protected by the database (409).
 */
@Service
public class BudgetRangeAdminService {

    private final BudgetRangeRepository repository;

    public BudgetRangeAdminService(BudgetRangeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<BudgetRangeResponse> list() {
        return repository.findAllByOrderByMinAmountAscCodeAsc().stream().map(CatalogMapper::toResponse).toList();
    }

    @Transactional
    public BudgetRangeResponse create(BudgetRangeRequest request) {
        if (request.code() == null) {
            throw BusinessRuleException.unprocessable(ErrorCode.CATALOG_CODE_REQUIRED);
        }
        if (repository.findByCode(request.code()).isPresent()) {
            throw new BusinessRuleException(ErrorCode.CATALOG_CODE_EXISTS, request.code());
        }
        BudgetRange range = new BudgetRange();
        range.setCode(request.code());
        apply(range, request);
        return CatalogMapper.toResponse(repository.save(range));
    }

    @Transactional
    public BudgetRangeResponse update(String code, BudgetRangeRequest request) {
        BudgetRange range = get(code);
        apply(range, request);
        return CatalogMapper.toResponse(range);
    }

    @Transactional
    public void delete(String code) {
        repository.delete(get(code));
        repository.flush();
    }

    private BudgetRange get(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATALOG_ENTRY_NOT_FOUND, code));
    }

    private static void apply(BudgetRange range, BudgetRangeRequest request) {
        if (request.maxAmount() != null && request.maxAmount().compareTo(request.minAmount()) < 0) {
            throw BusinessRuleException.unprocessable(ErrorCode.BUDGET_RANGE_INVALID);
        }
        range.setMinAmount(request.minAmount());
        range.setMaxAmount(request.maxAmount());
        range.setCurrency(request.currency());
    }
}
