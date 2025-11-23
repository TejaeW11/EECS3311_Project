package booking.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import accounts.model.UserAccount;
import booking.state.CreatedState;
import manager.room.Room;
import notifications.IBookingObserver;
import notifications.IBookingSubject;
import pricing.money.Money;

public class Booking implements IBookingSubject{

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

	public int bookingId;
	public Date startTime;
	public Date endTime;
	public String status;
	public Money totalAmount;
	public Money depositAmount;
	private UserAccount user; // Not listed on diagrams but needed
    private Room room; // Not listed on diagrams but needed
	private BookingState state;
	private List<IBookingObserver> observers;

	
	public Booking(int bookingId, Date startTime, Date endTime, Money totalAmount, Money depositAmount,
			UserAccount user, Room room) {
		
		if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end times cannot be null.");
        }
        if (startTime.after(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        
        if (startTime.after(new Date()) && (startTime.after(endTime) || startTime.equals(endTime))) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
		
		this.bookingId = bookingId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.status = "CREATED";
		this.totalAmount = totalAmount;
		this.depositAmount = depositAmount;
		this.user = user;
		this.room = room;
		this.state = new CreatedState();
		this.observers = new ArrayList<>();
	}

	

	public Booking(UserAccount user, Room room, Date startTime, Date endTime) {
		
		if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end times cannot be null.");
        }
        if (startTime.after(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
		
        this.bookingId = ID_GENERATOR.getAndIncrement(); 
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "CREATED";
        this.user = user;
        this.room = room;
        this.state = new CreatedState();
        this.observers = new ArrayList<>();
	}



	public void setState(BookingState newState) {
		if (newState == null) {
            throw new IllegalArgumentException("State cannot be null.");
        }
		this.state = newState;
		this.status = state.getStateName();
		notifyObservers("Your booking status has changed to: " + this.status);
	}
	
	
	
	public void checkIn() {
		state.checkIn(this);
	}
	
	public void cancel() {
		state.cancel(this);
	}
	
	public void complete() {
		state.complete(this);
	}
	
	public void expire() {
		state.expire(this);
	}

	
	@Override
	public void attach(IBookingObserver observer) {
		if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
		
	}

	@Override
	public void detach(IBookingObserver observer) {
		observers.remove(observer);
		
	}

	@Override
	public void notifyObservers(String message) {
		for (IBookingObserver observer : observers) {
            observer.update(this, message);
        }
	}
	
	public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setDepositAmount(Money depositAmount) {
        this.depositAmount = depositAmount;
    }
    
    public void extendEndTime(Date newEndTime) {
        if (newEndTime == null) {
            throw new IllegalArgumentException("New end time cannot be null.");
        }
        if (newEndTime.before(this.endTime) || newEndTime.equals(this.endTime)) {
            throw new IllegalArgumentException("New end time must be after current end time.");
        }
        this.endTime = newEndTime;
    }


    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }
    
    public int getBookingId() { return bookingId; }
    public Date getStartTime() { return startTime; }
    public Date getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public Money getTotalAmount() { return totalAmount; }
    public Money getDepositAmount() { return depositAmount; }
    public UserAccount getUser() { return user; }
    public Room getRoom() { return room; }
    public BookingState getState() { return state; }
    public List<IBookingObserver> getObservers() { return new ArrayList<>(observers); }
    
    @Override
    public String toString() {
        return String.format("Booking[%d] User: %s, Room: %d, Status: %s, Time: %s - %s",
                bookingId, 
                user != null ? user.getEmail() : "N/A",
                room != null ? room.getRoomId() : -1,
                status, startTime, endTime);
    }
	
	
	

}
