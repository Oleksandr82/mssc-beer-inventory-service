package tech.nautilus.beer.inventory.service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import tech.nautilus.beer.inventory.service.config.JmsConfig;
import tech.nautilus.brewery.model.events.AllocateOrderRequest;
import tech.nautilus.brewery.model.events.AllocateOrderResult;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationListener {

    private final AllocationService allocationService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateOrderRequest allocateOrderRequest) {
        AllocateOrderResult.AllocateOrderResultBuilder builder = AllocateOrderResult.builder();
        builder.beerOrderDto(allocateOrderRequest.getBeerOrderDto());

        try {
            Boolean allocationResult = allocationService.allocateOrder(allocateOrderRequest.getBeerOrderDto());
            builder.pendingInventory(!allocationResult);
            builder.allocationError(false);
            log.debug("Result of the order allocation is: {}", allocationResult);
        } catch (Exception ex) {
            log.debug("An exception was thrown during order allocation: {}", ex);
            builder.allocationError(true);
        }

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE, builder.build());
    }
}
