package tech.nautilus.brewery.events;

import lombok.NoArgsConstructor;
import tech.nautilus.brewery.model.BeerDto;

@NoArgsConstructor
public class NewInventoryEvent extends BeerEvent {
    public NewInventoryEvent(BeerDto beerDto) {
        super(beerDto);
    }
}
