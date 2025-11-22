package notifications;

import accounts.model.AccountType;
import booking.model.Booking;

public class PartnerPortal implements IBookingObserver{

	@Override
	public void update(Booking booking, String message) {
		if (booking == null) {
            System.err.println("PartnerPortal: Received null booking.");
            return;
        }
		
		if ((booking.getUser() != null) && (booking.getUser().getAccountType() == AccountType.PARTNER)) {
			System.out.println("Alert: " + message);
            System.out.println("Booking ID: " + booking.getBookingId());
            System.out.println("Partner: " + booking.getUser().getEmail());
            System.out.println("Room: " + (booking.getRoom() != null ? booking.getRoom().getRoomId() : "N/A"));
            System.out.println("Status: " + booking.getStatus());
		}
	}

}
