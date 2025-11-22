package notifications;

import booking.model.Booking;

public interface IBookingObserver {
	public void update(Booking booking, String message);
}
