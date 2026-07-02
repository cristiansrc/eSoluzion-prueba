package com.esoluzion.pricing.application.port.in;

import com.esoluzion.pricing.application.model.PriceQuery;
import com.esoluzion.pricing.application.model.PriceResult;

public interface GetApplicablePriceUseCase {
    PriceResult getApplicablePrice(PriceQuery query);
}
