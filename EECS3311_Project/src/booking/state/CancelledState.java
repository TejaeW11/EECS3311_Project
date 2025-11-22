package booking.state;

import booking.model.Booking;
import booking.model.BookingState;

public class CancelledState implements BookingState{

	@Override
	public void checkIn(Booking booking) {
        throw new IllegalStateException("Cannot check in a cancelled booking");
		
	}

	@Override
	public void cancel(Booking booking) {
        throw new IllegalStateException("Booking is already cancelled");
		
	}

	@Override
	public void complete(Booking booking) {
        throw new IllegalStateException("Cannot complete a cancelled booking");
		
	}

	@Override
	public void expire(Booking booking) {
        throw new IllegalStateException("Cannot expire a cancelled booking");
		
	}

}
