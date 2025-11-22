package booking.model;

public interface BookingState {
	public void checkIn(Booking booking);
	public void cancel(Booking booking);
	public void complete(Booking booking);
	public void expire(Booking booking);
	
	default String getStateName() {
        return this.getClass().getSimpleName().replace("State", "").toUpperCase();
    }
}
