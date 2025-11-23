package manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import accounts.model.UserAccount;
import booking.model.Booking;
import manager.room.Room;
import partnersystem.RoomAvailabilityService;
import storage.IStorageService;

public class BookingManager {
	private static BookingManager instance;
    private List<Room> rooms;
    private List<Booking> bookings;
    private RoomAvailabilityService availabilityService;
    private List<UserAccount> users;
    private IStorageService storageService;

    private BookingManager() {
        this.rooms = new ArrayList<>();
        this.bookings = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public static synchronized BookingManager getInstance() {
        if (instance == null) {
            instance = new BookingManager();
        }
        return instance;
    }
    
    // PURELY FOR TESTING CAN BE REMOVED //
    public static synchronized void resetInstance() {
        instance = null;
    }

    public void setAvailabilityService(RoomAvailabilityService service) {
        this.availabilityService = service;
    }
    
    public List<Room> findAvailableRooms(Date startTime, Date endTime, int capacity) {
        
    	if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end times cannot be null.");
        }
        if (startTime.after(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
    	
    	List<Room> availableRooms = new ArrayList<>();
        
        // Check internal rooms
        for (Room room : rooms) {
            if (room.getCapacity() >= capacity && "Enabled".equals(room.getStatus())) {
                boolean isAvailable = true;
                
                for (Booking booking : bookings) {
                	
                	String status = booking.getStatus();
                	if (status.equals("CANCELLED")||status.equals("EXPIRED")||status.equals("COMPLETED")){
                		continue;
                	}
                	
                    if (booking.getRoom().getRoomId() == room.getRoomId()) {
                    	if (booking.getStartTime().before(endTime) && booking.getEndTime().after(startTime)) {
	                        isAvailable = false;
	                        break;
                    	}
                	}
                }
                if (isAvailable) {
                    availableRooms.add(room);
                }
            }
        }
        
        // Check partner rooms if available
        if (availabilityService != null) {
        	 List<Room> partnerRooms = availabilityService.findAvailableRooms(startTime, endTime, capacity);
             availableRooms.addAll(partnerRooms);        
         }
        
        return availableRooms;
    }

    public Booking createBooking(UserAccount user, Room room, Date startTime, Date endTime) {
        
    	if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }
        
        List<Room> available = findAvailableRooms(startTime, endTime, 1);
        boolean roomAvailable = false;
        for (Room r : available) {
            if (r.getRoomId() == room.getRoomId()) {
                roomAvailable = true;
                break;
            }
        }
        
        if (!roomAvailable) {
            throw new IllegalStateException("Room is not available for the requested time period.");
        }
    	
    	Booking booking = new Booking(user, room, startTime, endTime);
        bookings.add(booking);
        
        // Saves to storage
        if (storageService != null) {
            storageService.saveBooking(booking);
        }
        
        return booking;
    }

    public void addRoom(Room room) {
    	if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }
        // Check for duplicate room ID
        for (Room r : rooms) {
            if (r.getRoomId() == room.getRoomId()) {
                throw new IllegalArgumentException("Room with ID " + room.getRoomId() + " already exists.");
            }
        }
        rooms.add(room);
        
        // Saves to storage
        if (storageService != null) {
            storageService.saveRoom(room);
        }
    }

    public void updateRoomStatus(int roomId, String status) {
    	 Room foundRoom = null;
         for (Room room : rooms) {
             if (room.getRoomId() == roomId) {
                 foundRoom = room;
                 break;
             }
         }
         
         if (foundRoom == null) {
             throw new IllegalArgumentException("Room with ID " + roomId + " not found.");
         }
         
         foundRoom.setStatus(status);
         
         // Saves to storage
         if (storageService != null) {
             storageService.updateRoom(foundRoom);
         }
    }
    
    public Booking getBookingById(int bookingId) {
        for (Booking booking : bookings) {
            if (booking.getBookingId() == bookingId) {
                return booking;
            }
        }
        return null;
    }
    
    //ALL EXTRA METHODS BELOW//
    // Extra Method
    public Room getRoomById(int roomId) {
        for (Room room : rooms) {
            if (room.getRoomId() == roomId) {
                return room;
            }
        }
        return null;
    }
    
    public UserAccount getUserById(int userId) {
        for (UserAccount user : users) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }
    
    public void registerUser(UserAccount user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        // Check for duplicate email
        for (UserAccount u : users) {
            if (u.getEmail().equals(user.getEmail())) {
                throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists.");
            }
        }
        users.add(user);
        
        // Saves to storage
        if (storageService != null) {
            storageService.saveUser(user);
        }
    }
    
    public void cancelBooking(int bookingId) {
        Booking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking with ID " + bookingId + " not found.");
        }
        booking.cancel(); 
        
        // Updates storage
        if (storageService != null) {
            storageService.updateBooking(booking);
        }
    }
    
    public void extendBooking(int bookingId, Date newEndTime) {
        Booking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking with ID " + bookingId + " not found.");
        }
        
        // Check if room is available for the extension period
        Date extensionStart = booking.getEndTime();
        List<Room> available = findAvailableRooms(extensionStart, newEndTime, 1);
        
        boolean roomAvailable = false;
        for (Room r : available) {
            if (r.getRoomId() == booking.getRoom().getRoomId()) {
                roomAvailable = true;
                break;
            }
        }
        
        if (!roomAvailable) {
            throw new IllegalStateException("Room is not available for the extension period.");
        }
        
        booking.extendEndTime(newEndTime);
        booking.notifyObservers("Booking extended until " + newEndTime);
        
        // Updates storage
        if (storageService != null) {
            storageService.updateBooking(booking);
        }
    }
    
    public List<Booking> getBookingsForUser(int userId) {
        List<Booking> userBookings = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getUser().getUserId() == userId) {
                userBookings.add(booking);
            }
        }
        return userBookings;
    }
    
    public UserAccount getUserByEmail(String email) {
        for (UserAccount user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }
    
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms);
    }
    
    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookings);
    }
    
    public List<UserAccount> getAllUsers() {
        return new ArrayList<>(users);
    }
    
    
    // =========== //
    // CSV METHODS //
    // =========== //
    
    //Sets the storage service and loads existing data
    public void setStorageService(IStorageService storageService) {
        this.storageService = storageService;
        if (storageService != null) {
            loadDataFromStorage();
        }
    }
    
    // Gets the current storage service
    public IStorageService getStorageService() {
        return storageService;
    }
    
    // Loads all data from storage into memory
    private void loadDataFromStorage() {
        if (storageService == null) return;
        
        System.out.println("Loading data from storage...");
        
        // Load users first (bookings depend on them)
        List<UserAccount> loadedUsers = storageService.loadAllUsers();
        this.users = new ArrayList<>(loadedUsers);
        System.out.println("  Loaded " + users.size() + " users");
        
        // Load rooms (bookings depend on them)
        List<Room> loadedRooms = storageService.loadAllRooms();
        this.rooms = new ArrayList<>(loadedRooms);
        System.out.println("  Loaded " + rooms.size() + " rooms");
        
        // Load bookings
        List<Booking> loadedBookings = storageService.loadAllBookings();
        this.bookings = new ArrayList<>(loadedBookings);
        System.out.println("  Loaded " + bookings.size() + " bookings");
    }
    
    // Saves all in-memory data to storage
    public void saveAllDataToStorage() {
        if (storageService == null) {
            System.err.println("No storage service configured.");
            return;
        }
        
        System.out.println("Saving all data to storage...");
        
        storageService.clearAll();
        
        for (UserAccount user : users) {
            storageService.saveUser(user);
        }
        
        for (Room room : rooms) {
            storageService.saveRoom(room);
        }
        
        for (Booking booking : bookings) {
            storageService.saveBooking(booking);
        }
        
        System.out.println("All data saved to storage.");
    }
    
    // Updates a booking's state in storage
    public void persistBookingUpdate(Booking booking) {
        if (storageService != null && booking != null) {
            storageService.updateBooking(booking);
        }
    }

}
