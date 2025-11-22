package facade;

import java.util.Date;
import java.util.List;

import accounts.factory.AccountFactory;
import accounts.factory.AdminAccountFactory;
import accounts.factory.FacultyAccountFactory;
import accounts.factory.PartnerAccountFactory;
import accounts.factory.StaffAccountFactory;
import accounts.factory.StudentAccountFactory;
import accounts.model.*;
import booking.model.Booking;
import manager.*;
import manager.payment.PaymentMethod;
import manager.payment.PaymentService;
import manager.room.Room;
import partnersystem.RoomAvailabilityService;
import pricing.money.Money;

public class SystemFacade {

	private BookingManager bookingManager;
    private PaymentService paymentService;
    private BookingNotificationSystem notificationSystem;
    private UserAccount currentUser;
    
    public SystemFacade() {
        this.bookingManager = BookingManager.getInstance();
        this.paymentService = new PaymentService();
        this.notificationSystem = new BookingNotificationSystem();
    }
    
    
    
    
    //====================//					  
    // ACCOUNT MANAGEMENT //
    //====================//
    
    
    public UserAccount createUserAccount(String email, String password, 
            AccountType accountType, String organizationId, String studentNumber) {
        
        // Verify email param 
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        
        // Can skips for testing purposes and project simplicity
        // Validate password strenth
        if (!isPasswordStrong(password)) {
            throw new IllegalArgumentException(
                "Password must contain uppercase, lowercase, numbers, and symbols.");
        }
        
        // Get factory for account
        AccountFactory factory = getFactoryForAccountType(accountType);
        
        // Create account
        UserAccount account = factory.createUserAccount(email, password, organizationId, studentNumber);
        
        // Register booking
        bookingManager.registerUser(account);
        
        return account;
    }
    
    // Overloaded method
    public UserAccount createUserAccount(String email, String password, AccountType accountType) {
        return createUserAccount(email, password, accountType, null, null);
    }
	
    // CREATION SPECIFICALLY FOR ADMIN ACCOUNTS
    public UserAccount createAdminAccount(String email, String password, 
            String organizationId, boolean newAdminisSuperAdmin) {
        
        
        if (currentUser == null) {
            throw new SecurityException("Must be logged in to create admin accounts.");
        }
        
        boolean isAuthorized = false;
        if (currentUser.getAccountType() == AccountType.ADMIN) {
            
            if (currentUser instanceof accounts.model.AdminAccount) {
                isAuthorized = ((accounts.model.AdminAccount) currentUser).isSuperAdmin();
            }
        }
        
        if (!isAuthorized) {
            throw new SecurityException("Only a Super Admin can create admin accounts.");
        }
        
        
        AdminAccountFactory factory = new AdminAccountFactory(true, newAdminisSuperAdmin);
        UserAccount adminAccount = factory.createUserAccount(email, password, organizationId, null);
        
        bookingManager.registerUser(adminAccount);
        return adminAccount;
    }
    
    // HELPER METHOD
    private AccountFactory getFactoryForAccountType(AccountType accountType) {
        switch (accountType) {
            case STUDENT:
                return new StudentAccountFactory();
            case FACULTY:
                return new FacultyAccountFactory();
            case STAFF:
                return new StaffAccountFactory();
            case PARTNER:
                return new PartnerAccountFactory();
            case ADMIN:
                // Admin accounts need to use createAdminAccount
                throw new SecurityException("Use createAdminAccount() for admin accounts.");
            default:
                throw new IllegalArgumentException("Unknown account type: " + accountType);
        }
    }
    
    // PASSWORD TETSING HELPER CLASS - CAN SKIP FOR SIMLICITY AND TESTING
    // HELPER METHOD
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSymbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSymbol = true;
        }
        return hasUpper && hasLower && hasDigit && hasSymbol;
    }
    
    
    //=================//					  
    // ROOM MANAGEMENT //
    //=================//
    
    
    public void addRoom(int roomId, String building, String roomNumber, int capacity) {
        
        if (currentUser == null || currentUser.getAccountType() != AccountType.ADMIN) {
            throw new SecurityException("Only administrators can add rooms.");
        }
        
        Room room = new Room(roomId, building, roomNumber, capacity, "Enabled");
        bookingManager.addRoom(room);
    }
    
    
    public void updateRoomStatus(int roomId, String status) {
        if (currentUser == null || currentUser.getAccountType() != AccountType.ADMIN) {
            throw new SecurityException("Only administrators can update room status.");
        }
        bookingManager.updateRoomStatus(roomId, status);
    }
    
    
    public List<Room> checkAvailability(Date startTime, Date endTime, int capacity) {
        return bookingManager.findAvailableRooms(startTime, endTime, capacity);
    }
    
    
    //====================//					  
    // BOOKING MANAGEMENT //
    //====================//
    
    
    public Booking requestRoomBooking(int userId, int roomId, Date startTime, Date endTime) {
        // Get user and room
        UserAccount user = bookingManager.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + userId + " not found.");
        }
        
        Room room = bookingManager.getRoomById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room with ID " + roomId + " not found.");
        }
        
        // Create booking
        Booking booking = bookingManager.createBooking(user, room, startTime, endTime);
        
        // Calculate and set pricing 
        Money totalAmount = paymentService.calculateBookingPrice(booking, user);
        Money depositAmount = paymentService.calculateDeposit(booking, user);
        booking.setTotalAmount(totalAmount);
        booking.setDepositAmount(depositAmount);
        
        // Register notifications
        notificationSystem.registerBookingForNotifications(booking);
        
        // Notify booking creation
        booking.notifyObservers("Booking created. Deposit of " + depositAmount + " required.");
        
        return booking;
    }
    
    
    public boolean payBookingDeposit(int bookingId, PaymentMethod method) {
        Booking booking = bookingManager.getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking with ID " + bookingId + " not found.");
        }
        
        boolean success = paymentService.payDeposit(booking, method);
        if (success) {
            booking.notifyObservers("Deposit payment of " + booking.getDepositAmount() + " received.");
        }
        return success;
    }
    
    
    public void checkInBooking(int bookingId) {
        Booking booking = bookingManager.getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking with ID " + bookingId + " not found.");
        }
        booking.checkIn(); // State pattern handles transitions internally
    }
    
    
    public void completeBooking(int bookingId, PaymentMethod method) {
        Booking booking = bookingManager.getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking with ID " + bookingId + " not found.");
        }
        
        // Process remaining balance
        paymentService.payRemainingBalance(booking, method);
        
        booking.complete();
    }
    
    
    public void cancelBooking(int bookingId) {
        bookingManager.cancelBooking(bookingId);
    }
    
    
    // METHOD TO EXTEND BOOKING
    public void extendBooking(int bookingId, Date newEndTime) {
        Booking booking = bookingManager.getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking with ID " + bookingId + " not found.");
        }
        
        // Store old end time for price calculation
        Date oldEndTime = booking.getEndTime();
        
        // Extends the booking (BookingManager validates availability)
        bookingManager.extendBooking(bookingId, newEndTime);
        
        
        // Additional cost calculation + temp Booking to calculate
        UserAccount user = booking.getUser();
        long oldHours = (oldEndTime.getTime() - booking.getStartTime().getTime()) / (1000 * 60 * 60);
        long newHours = (newEndTime.getTime() - booking.getStartTime().getTime()) / (1000 * 60 * 60);
        long additionalHours = newHours - oldHours;
        
        if (additionalHours > 0) {
            // New total cost recalculation
            Money newTotal = paymentService.calculateBookingPrice(booking, user);
            booking.setTotalAmount(newTotal);
            booking.notifyObservers("Booking extended. New total: " + newTotal);
        }
    }
    
    
    //====================//
    // PRICING MANAGEMENT //
    //====================//
    
    
    public Money calculateBookingPrice(int bookingId) {
        Booking booking = bookingManager.getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking with ID " + bookingId + " not found.");
        }
        return paymentService.calculateBookingPrice(booking, booking.getUser());
    }
    
    
    //===================//
    // GETTERS & SETTERS //
    //===================//
    
    
    // SETTERS //
    public void setPartnerAvailabilityService(RoomAvailabilityService service) {
        bookingManager.setAvailabilityService(service);
    }
    
    public void setCurrentUser(UserAccount user) {
        this.currentUser = user;
    }
    
    
    // GETTERS //
    public UserAccount getCurrentUser() {
        return currentUser;
    }
    
    public Booking getBooking(int bookingId) {
        return bookingManager.getBookingById(bookingId);
    }
    
    public List<Booking> getUserBookings(int userId) {
        return bookingManager.getBookingsForUser(userId);
    }
    
    public List<Room> getAllRooms() {
        return bookingManager.getAllRooms();
    }
    
    public BookingNotificationSystem getNotificationSystem() {
        return notificationSystem;
    }
    
    public PaymentService getPaymentService() {
        return paymentService;
    }
    
    public BookingManager getBookingManager() {
        return bookingManager;
    }
    
}
    
    
	

