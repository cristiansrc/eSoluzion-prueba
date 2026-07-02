package com.esoluzion.pricing.application.service;

import com.esoluzion.pricing.application.model.PriceQuery;
import com.esoluzion.pricing.application.model.PriceResult;
import com.esoluzion.pricing.application.port.in.GetApplicablePriceUseCase;
import com.esoluzion.pricing.application.port.out.PriceRepositoryPort;
import com.esoluzion.pricing.domain.exception.PriceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetApplicablePriceService implements GetApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepository;

    public GetApplicablePriceService(PriceRepositoryPort priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Override
    public PriceResult getApplicablePrice(PriceQuery query) {
        var price = priceRepository.findApplicablePrice(
                query.brandId(),
                query.productId(),
                query.applicationDate()
        ).orElseThrow(PriceNotFoundException::new);

        return new PriceResult(
                price.getProductId(),
                price.getBrandId(),
                price.getPriceList(),
                price.getDateRange().getStartDate(),
                price.getDateRange().getEndDate(),
                price.getPrice(),
                price.getCurrency()
        );
    }
}
