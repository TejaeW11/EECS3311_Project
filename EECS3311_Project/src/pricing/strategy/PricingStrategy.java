package pricing.strategy;

import booking.model.Booking;
import pricing.money.Money;

public interface PricingStrategy {

	public Money calculatePrice(Booking booking);
}
