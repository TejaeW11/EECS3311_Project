package pricing.strategy;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import booking.model.Booking;
import pricing.money.Money;

public class StaffPricingStrategy implements PricingStrategy{

	private static final double HOURLY_RATE = 40.0; 
    private static final String DEFAULT_CURRENCY = "CAD";
	
	@Override
	public Money calculatePrice(Booking booking) {
		
		if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }
        if (booking.getStartTime() == null || booking.getEndTime() == null) {
            throw new IllegalArgumentException("Booking times cannot be null.");
        }
		
		LocalDateTime startTime = booking.getStartTime().toInstant().atZone(ZoneId.systemDefault())
		        .toLocalDateTime();
        LocalDateTime endTime = booking.getEndTime().toInstant().atZone(ZoneId.systemDefault())
				        .toLocalDateTime();
		
		long hours = ChronoUnit.HOURS.between(startTime, endTime);
		
		if (hours < 1) {
            hours = 1;
        }
		 
		return new Money(HOURLY_RATE*hours,DEFAULT_CURRENCY);
	}
}
