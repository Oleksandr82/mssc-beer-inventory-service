package tech.nautilus.beer.inventory.service.services;

import tech.nautilus.brewery.model.BeerOrderDto;

public interface AllocationService {

    Boolean allocateOrder(BeerOrderDto beerOrderDto);
}
