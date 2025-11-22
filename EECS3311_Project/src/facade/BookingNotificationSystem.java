package facade;

import java.util.ArrayList;
import java.util.List;

import booking.model.Booking;
import notifications.*;

public class BookingNotificationSystem {

	private List<IBookingObserver> defaultObservers;
	
	public BookingNotificationSystem() {
        this.defaultObservers = new ArrayList<>();
        // ADDED: Initialize with default notification channels
        initializeDefaultObservers();
    }
	
	private void initializeDefaultObservers() {
        defaultObservers.add(new EmailNotification());
        defaultObservers.add(new AdminDashboard());
        defaultObservers.add(new PartnerPortal());
    }
	
	public void registerBookingForNotifications(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }
        for (IBookingObserver observer : defaultObservers) {
            booking.attach(observer);
        }
    }
	
	public void addDefaultObserver(IBookingObserver observer) {
        if (observer != null && !defaultObservers.contains(observer)) {
            defaultObservers.add(observer);
        }
    }
	
	public void removeDefaultObserver(IBookingObserver observer) {
        defaultObservers.remove(observer);
    }
	
	public void notifyObservers(Booking booking, String message) {
		if (booking != null) {
            booking.notifyObservers(message);
        }
	}
	
	public List<IBookingObserver> getDefaultObservers() {
        return new ArrayList<>(defaultObservers);
    }
}
