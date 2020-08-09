package tech.nautilus.beer.inventory.service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tech.nautilus.beer.inventory.service.config.JmsConfig;
import tech.nautilus.brewery.model.events.AllocateOrderRequest;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeallocationListener {

    private final AllocationService allocationService;

    @JmsListener(destination = JmsConfig.DEALLOCATE_ORDER_EVENT_QUEUE)
    public void listen(AllocateOrderRequest allocateOrderRequest) {
        allocationService.deallocateOrder(allocateOrderRequest.getBeerOrderDto());
    }
}
