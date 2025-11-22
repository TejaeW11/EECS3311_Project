package booking.state;

import booking.model.Booking;
import booking.model.BookingState;

public class ExpiredState implements BookingState{

	@Override
	public void checkIn(Booking booking) {
        throw new IllegalStateException("Cannot check in an expired booking");
		
	}

	@Override
	public void cancel(Booking booking) {
        throw new IllegalStateException("Cannot cancel an expired booking");
		
	}

	@Override
	public void complete(Booking booking) {
        throw new IllegalStateException("Cannot complete an expired booking");
		
	}

	@Override
	public void expire(Booking booking) {
		throw new IllegalStateException("Booking is already expired");		
	}

}
