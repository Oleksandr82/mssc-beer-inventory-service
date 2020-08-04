package tech.nautilus.beer.inventory.service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.nautilus.beer.inventory.service.domain.BeerInventory;
import tech.nautilus.beer.inventory.service.repositories.BeerInventoryRepository;
import tech.nautilus.brewery.model.BeerOrderDto;
import tech.nautilus.brewery.model.BeerOrderLineDto;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class AllocationServiceImpl implements AllocationService {

    private final BeerInventoryRepository beerInventoryRepository;

    @Override
    public Boolean allocateOrder(BeerOrderDto beerOrderDto) {
        log.debug("Allocating Order: {}", beerOrderDto.getId());

        AtomicInteger totalOrdered = new AtomicInteger();
        AtomicInteger totalAllocated = new AtomicInteger();

        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
            Integer orderQuantity = Optional.ofNullable(beerOrderLineDto.getOrderQuantity()).orElse(0);
            Integer allocatedQuantity = Optional.ofNullable(beerOrderLineDto.getQuantityAllocated()).orElse(0);
            if (orderQuantity - allocatedQuantity > 0) {
                allocateBeerOrderLine(beerOrderLineDto);
            }
            totalOrdered.set(totalOrdered.get() + orderQuantity);
            totalAllocated.set(totalAllocated.get() + allocatedQuantity);
        });

        log.debug("Total Ordered: {}, Total Allocated: {}", totalOrdered.get(), totalAllocated.get());

        return totalOrdered.get() == totalAllocated.get();
    }

    private void allocateBeerOrderLine(BeerOrderLineDto beerOrderLine) {
        List<BeerInventory> beerInventoryList = beerInventoryRepository.findAllByUpc(beerOrderLine.getUpc());

        beerInventoryList.forEach(beerInventory -> {
            int inventory = Optional.ofNullable(beerInventory.getQuantityOnHand()).orElse(0);
            int orderQty = Optional.ofNullable(beerOrderLine.getOrderQuantity()).orElse(0);
            int allocatedQty = Optional.ofNullable(beerOrderLine.getQuantityAllocated()).orElse(0);
            int qtyToAllocate = orderQty - allocatedQty;

            if (inventory >= qtyToAllocate) {

                // full allocation
                inventory = inventory - qtyToAllocate;
                beerOrderLine.setQuantityAllocated(orderQty);
                beerInventory.setQuantityOnHand(inventory);

                beerInventoryRepository.save(beerInventory);

            } else if (inventory > 0) {

                //partial allocation
                beerOrderLine.setQuantityAllocated(allocatedQty + inventory);
                beerInventory.setQuantityOnHand(0);
            }

            if (beerInventory.getQuantityOnHand() == 0) {
                beerInventoryRepository.delete(beerInventory);
            }
        });
    }
}
