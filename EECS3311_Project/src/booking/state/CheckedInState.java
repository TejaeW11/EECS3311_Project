package booking.state;

import booking.model.Booking;
import booking.model.BookingState;

public class CheckedInState implements BookingState{

	@Override
	public void checkIn(Booking booking) {
        throw new IllegalStateException("Booking is already checked in");
		
	}

	@Override
	public void cancel(Booking booking) {
        throw new IllegalStateException("Cannot cancel a booking that has been checked in");
		
	}

	@Override
	public void complete(Booking booking) {
		booking.setState(new CompletedState());
		booking.notifyObservers("Booking Completed");
	}

	@Override
	public void expire(Booking booking) {
        throw new IllegalStateException("Cannot expire a checked-in booking");
		
	}

}
