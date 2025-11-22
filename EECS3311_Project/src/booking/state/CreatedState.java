package booking.state;

import java.time.LocalDateTime;
import java.time.ZoneId;

import booking.model.Booking;
import booking.model.BookingState;

public class CreatedState implements BookingState{

	@Override
	public void checkIn(Booking booking) {
		
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startTime = booking.getStartTime().toInstant().atZone(ZoneId.systemDefault())
		        .toLocalDateTime();
        LocalDateTime cutoffTime = startTime.plusMinutes(30);
        
		if(now.isBefore(startTime)) {
			// Cannot check in time is not yet
			long timeUntilStart = java.time.Duration.between(now, startTime).toMinutes();
			booking.notifyObservers("Too early to check-in."+timeUntilStart+" still remain"); // Work in progress for correct time
			return;
		}else if(now.isAfter(cutoffTime)) {
			// Time to checkin has passed
			booking.setState(new ExpiredState());
			booking.notifyObservers("Booking expired. Check-in too late."); 
		}else {
			booking.setState(new CheckedInState());
			booking.notifyObservers("Booking checked in successfully");
		}
		
	}

	@Override
	public void cancel(Booking booking) {
		 booking.setState(new CancelledState());
	     booking.notifyObservers("Booking cancelled");
		
	}

	@Override
	public void complete(Booking booking) {
        throw new IllegalStateException("Cannot complete a booking that hasn't been checked in");
		
	}

	@Override
	public void expire(Booking booking) {
		booking.setState(new ExpiredState());
        booking.notifyObservers("Booking expired. No check-in.");
		
	}

}
