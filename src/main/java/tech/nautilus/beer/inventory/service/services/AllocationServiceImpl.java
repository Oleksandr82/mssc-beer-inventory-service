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

        AtomicInteger totalOrdered = new AtomicInteger(0);
        AtomicInteger totalAllocated = new AtomicInteger(0);

        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
            Integer orderQuantity = Optional.ofNullable(beerOrderLineDto.getOrderQuantity()).orElse(0);
            Integer allocatedQuantity = Optional.ofNullable(beerOrderLineDto.getQuantityAllocated()).orElse(0);
            if (orderQuantity - allocatedQuantity > 0) {
                allocatedQuantity = allocateBeerOrderLine(beerOrderLineDto);
            }
            totalOrdered.set(totalOrdered.get() + orderQuantity);
            totalAllocated.set(totalAllocated.get() + allocatedQuantity);
        });

        log.debug("Total Ordered: {}, Total Allocated: {}", totalOrdered.get(), totalAllocated.get());

        return totalOrdered.get() == totalAllocated.get();
    }

    @Override
    public void deallocateOrder(BeerOrderDto beerOrderDto) {

        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
            BeerInventory beerInventory = BeerInventory.builder()
                    .beerId(beerOrderLineDto.getBeerId())
                    .upc(beerOrderLineDto.getUpc())
                    .quantityOnHand(beerOrderLineDto.getQuantityAllocated())
                    .build();

            BeerInventory inventory = beerInventoryRepository.save(beerInventory);

            log.debug("Saved Inventory for beer UPC: {} inventory id: {}", inventory.getUpc(), inventory.getId());
        });

    }

    private int allocateBeerOrderLine(BeerOrderLineDto beerOrderLine) {
        List<BeerInventory> beerInventoryList = beerInventoryRepository.findAllByUpc(beerOrderLine.getUpc());
        if (beerInventoryList.size() == 0 ) {
            return 0;
        }

        BeerInventory beerInventory = beerInventoryList.get(0);

        int inventory = Optional.ofNullable(beerInventory.getQuantityOnHand()).orElse(0);
        int orderQty = Optional.ofNullable(beerOrderLine.getOrderQuantity()).orElse(0);
        int alreadyAllocatedQty = Optional.ofNullable(beerOrderLine.getQuantityAllocated()).orElse(0);
        int qtyToAllocate = orderQty - alreadyAllocatedQty;

        int allocatedQty = 0;
        if (inventory >= qtyToAllocate) {

            // full allocation
            inventory = inventory - qtyToAllocate;
            beerOrderLine.setQuantityAllocated(orderQty);
            beerInventory.setQuantityOnHand(inventory);

            beerInventoryRepository.save(beerInventory);

            allocatedQty = qtyToAllocate;

        } else if (inventory > 0) {

            //partial allocation
            beerOrderLine.setQuantityAllocated(alreadyAllocatedQty + inventory);
            beerInventory.setQuantityOnHand(0);

            allocatedQty = inventory;
        }

        if (beerInventory.getQuantityOnHand() == 0) {
            beerInventoryRepository.delete(beerInventory);
        }

        return allocatedQty;
    }
}
