package com.esoluzion.pricing.infrastructure.adapter.out.persistence.mapper;

import com.esoluzion.pricing.domain.model.DateRange;
import com.esoluzion.pricing.domain.model.Price;
import com.esoluzion.pricing.infrastructure.adapter.out.persistence.entity.PriceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PriceInfrastructureMapper {

    @Mapping(target = "dateRange", expression = "java(new DateRange(entity.getStartDate(), entity.getEndDate()))")
    Price toDomain(PriceEntity entity);
}
