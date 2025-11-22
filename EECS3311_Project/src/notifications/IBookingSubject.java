package notifications;

public interface IBookingSubject {

	public void attach(IBookingObserver observer);
    public void detach(IBookingObserver observer);
    public void notifyObservers(String message);
}
