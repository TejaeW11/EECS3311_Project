package booking.state;

import booking.model.Booking;
import booking.model.BookingState;

public class CompletedState implements BookingState{

	@Override
	public void checkIn(Booking booking) {
        throw new IllegalStateException("Cannot check in a completed booking");
		
	}

	@Override
	public void cancel(Booking booking) {
        throw new IllegalStateException("Cannot cancel a completed booking");
		
	}

	@Override
	public void complete(Booking booking) {
        throw new IllegalStateException("Booking is already completed");
		
	}

	@Override
	public void expire(Booking booking) {
        throw new IllegalStateException("Cannot expire a completed booking");
		
	}

}
