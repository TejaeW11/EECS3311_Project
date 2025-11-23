package storage;

import java.util.List;
import accounts.model.UserAccount;
import booking.model.Booking;
import manager.room.Room;

/**
 * Interface for storage services - allows swapping between CSV, SQL, etc.
 * All storage implementations must implement these methods.
 */
public interface IStorageService {
    
    
     //Saves a user account to storage.
    void saveUser(UserAccount user);
    
    
     // Retrieves all users from storage.
    List<UserAccount> loadAllUsers();
    
    
     //  Finds a user by their email.
    UserAccount findUserByEmail(String email);
    
    
    // Finds a user by their ID.
    UserAccount findUserById(int userId);
    
    
    // Deletes a user from storage.
    boolean deleteUser(int userId);
    
    
    // Saves a room to storage.
    void saveRoom(Room room);
    
    // Retrieves all rooms from storage.
    List<Room> loadAllRooms();
    
    
    // Finds a room by its ID.
    Room findRoomById(int roomId);
    
    // Updates a room's information.
    void updateRoom(Room room);
    
    
    // Deletes a room from storage.
    boolean deleteRoom(int roomId);
    
    
    // Saves a booking to storage.
    void saveBooking(Booking booking);
    
    // Retrieves all bookings from storage.
    List<Booking> loadAllBookings();
    
    // Finds a booking by its ID.
    Booking findBookingById(int bookingId);
    
    // Finds all bookings for a specific user.
    List<Booking> findBookingsByUserId(int userId);
    
    
    // Updates a booking's information.
    void updateBooking(Booking booking);
    
    // Deletes a booking from storage.
    boolean deleteBooking(int bookingId);
    
   
     // Initializes storage (creates files/tables if needed).
    void initialize();
    
   
    // Clears all data from storage (useful for testing).
    void clearAll();
}