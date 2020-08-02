package tech.nautilus.beer.inventory.service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import tech.nautilus.beer.inventory.service.domain.BeerInventory;
import tech.nautilus.beer.inventory.service.repositories.BeerInventoryRepository;
import tech.nautilus.brewery.model.events.NewInventoryEvent;

import static tech.nautilus.beer.inventory.service.config.JmsConfig.NEW_INVENTORY_QUEUE;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewInventoryListener {

    private final BeerInventoryRepository beerInventoryRepository;

    @JmsListener(destination = NEW_INVENTORY_QUEUE)
    public void listen(NewInventoryEvent newInventoryEvent) {
        log.debug("Got new inventory: {}", newInventoryEvent);
        beerInventoryRepository.save(BeerInventory.builder()
                .beerId(newInventoryEvent.getBeerDto().getId())
                .upc(newInventoryEvent.getBeerDto().getUpc())
                .quantityOnHand(newInventoryEvent.getBeerDto().getQuantityOnHand())
                .build());
    }
}
