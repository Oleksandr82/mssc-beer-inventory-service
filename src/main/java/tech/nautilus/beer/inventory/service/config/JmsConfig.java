package tech.nautilus.beer.inventory.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {

    public static final String NEW_INVENTORY_QUEUE = "new-inventory-request";
    public static final String ALLOCATE_ORDER_QUEUE = "allocate-order-request";
    public static final String ALLOCATE_ORDER_RESPONSE = "allocate-order-response";
    public static final String DEALLOCATE_ORDER_EVENT_QUEUE = "deallocate-order-event";


    public final static String TYPE_PROP_NAME = "_type";

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName(TYPE_PROP_NAME);
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
