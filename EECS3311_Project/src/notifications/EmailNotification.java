package notifications;

import booking.model.Booking;

public class EmailNotification implements IBookingObserver{

	@Override
	public void update(Booking booking, String message) {
		
		if (booking == null) {
            System.err.println("EmailNotification: Received null booking.");
            return;
        }
		
		String email = booking.getUser() != null ? booking.getUser().getEmail() : "unknown";
		String room = booking.getRoom() != null ? String.valueOf(booking.getRoom().getRoomId()) : "unknown";
        
		System.out.println("Sending email to " + email + ": " + message);
        System.out.println("Booking ID: " + booking.getBookingId());
        System.out.println("Room: " + room);
        System.out.println("Status: " + booking.getState());
		
	}

}
