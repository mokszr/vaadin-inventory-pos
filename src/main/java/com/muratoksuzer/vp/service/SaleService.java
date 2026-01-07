package com.muratoksuzer.vp.service;

import com.muratoksuzer.vp.dto.SaleDto;
import com.muratoksuzer.vp.entity.domain.Sale;
import com.muratoksuzer.vp.repository.SaleRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
@Service
public class SaleService {

    private SaleRepository saleRepository;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<SaleDto> findPaginated(Pageable pageable, String searchTerm) {
        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(saleRepository.findAll(pageable));
        }

        return mapToDto(saleRepository.findBySaleNoContainingIgnoreCase(searchTerm, pageable));

    }

    private Page<SaleDto> mapToDto(Page<Sale> page) {
        return page.map(sale -> toDto(sale));
    }

    private SaleDto toDto(Sale sale) {
        if(sale == null) {
            return null;
        }
        SaleDto dto = new SaleDto();
        dto.setId(sale.getId());
        dto.setDateCreated(sale.getDateCreated());
        dto.setTotal(sale.getTotal());
        dto.setSaleNo(sale.getSaleNo());

        return dto;
    }

}
