package notifications;

import booking.model.Booking;

public class AdminDashboard implements IBookingObserver{

	@Override
	public void update(Booking booking, String message) {
		if (booking == null) {
            System.err.println("AdminDashboard: Received null booking.");
            return;
        }
		
		System.out.println("Alert: " + message);
        System.out.println("Booking ID: " + booking.getBookingId());
        System.out.println("User: " + (booking.getUser() != null ? booking.getUser().getEmail() : "N/A"));
        System.out.println("Room: " + (booking.getRoom() != null ? booking.getRoom().getRoomId() : "N/A"));
        System.out.println("Status: " + booking.getStatus());
	}

}
