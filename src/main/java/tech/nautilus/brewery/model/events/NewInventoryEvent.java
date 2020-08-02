package tech.nautilus.brewery.model.events;

import lombok.NoArgsConstructor;
import tech.nautilus.brewery.model.BeerDto;

@NoArgsConstructor
public class NewInventoryEvent extends BeerEvent {
    public NewInventoryEvent(BeerDto beerDto) {
        super(beerDto);
    }
}
