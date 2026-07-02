package com.esoluzion.pricing.infrastructure.adapter.in.web;

import com.esoluzion.pricing.application.model.PriceQuery;
import com.esoluzion.pricing.application.port.in.GetApplicablePriceUseCase;
import com.esoluzion.pricing.infrastructure.adapter.in.web.api.PricesApi;
import com.esoluzion.pricing.infrastructure.adapter.in.web.dto.PriceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
public class PriceController implements PricesApi {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    public PriceController(GetApplicablePriceUseCase getApplicablePriceUseCase) {
        this.getApplicablePriceUseCase = getApplicablePriceUseCase;
    }

    @Override
    public ResponseEntity<PriceResponse> getApplicablePrice(
            LocalDateTime applicationDate, Long productId, Long brandId) {

        var query = new PriceQuery(applicationDate, productId, brandId);
        var result = getApplicablePriceUseCase.getApplicablePrice(query);

        var response = new PriceResponse()
            .productId(result.productId())
            .brandId(result.brandId())
            .priceList(result.priceList())
            .startDate(result.startDate())
            .endDate(result.endDate())
            .price(result.price())
            .currency(result.currency());

        return ResponseEntity.ok(response);
    }
}
