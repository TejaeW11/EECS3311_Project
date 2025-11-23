package storage;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import accounts.model.*;
import booking.model.Booking;
import booking.state.*;
import manager.room.Room;
import pricing.money.Money;


public class CSVStorageService implements IStorageService {
    
    // File paths for CSV storage
    private static final String DATA_DIRECTORY = "data";
    private static final String USERS_FILE = DATA_DIRECTORY + "/users.csv";
    private static final String ROOMS_FILE = DATA_DIRECTORY + "/rooms.csv";
    private static final String BOOKINGS_FILE = DATA_DIRECTORY + "/bookings.csv";
    
    // Date format for storing dates in CSV
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // CSV delimiters
    private static final String DELIMITER = ",";
    private static final String NULL_PLACEHOLDER = "NULL";
    
    
    public CSVStorageService() {
        initialize();
    }
    
    // ============== //
    // INITIALIZATION
    // ============== //
    
    @Override
    public void initialize() {
        // Create data directory if it doesn't exist
        File directory = new File(DATA_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Created data directory: " + DATA_DIRECTORY);
        }
        
        // Create CSV files with headers if they don't exist
        createFileWithHeader(USERS_FILE, getUserCSVHeader());
        createFileWithHeader(ROOMS_FILE, getRoomCSVHeader());
        createFileWithHeader(BOOKINGS_FILE, getBookingCSVHeader());
    }
    
    
    // Creates a CSV file with header if it doesn't exist.
    private void createFileWithHeader(String filePath, String header) {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println(header);
                System.out.println("Created file: " + filePath);
            } catch (IOException e) {
                System.err.println("Error creating file " + filePath + ": " + e.getMessage());
            }
        }
    }
    
    
    // Returns the CSV header for users file.
    private String getUserCSVHeader() {
        return "userId,email,password,accountType,verified,status,organizationId,studentNumber,isSuperAdmin";
    }
    
    
    // Returns the CSV header for rooms file.
    private String getRoomCSVHeader() {
        return "roomId,building,roomNumber,capacity,status";
    }
    
   
    // Returns the CSV header for bookings file.
    private String getBookingCSVHeader() {
        return "bookingId,userId,roomId,startTime,endTime,status,totalAmount,totalCurrency,depositAmount,depositCurrency";
    }
    
    // =============== //
    // USER OPERATIONS //
    // =============== //
    
    @Override
    public void saveUser(UserAccount user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        // Check if user already exists (update instead of duplicate)
        if (findUserById(user.getUserId()) != null) {
            updateUser(user);
            return;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, true))) {
            writer.println(userToCSV(user));
            System.out.println("Saved user: " + user.getEmail());
        } catch (IOException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }
    

    // Converts a UserAccount to CSV format.
    private String userToCSV(UserAccount user) {
        StringBuilder sb = new StringBuilder();
        sb.append(user.getUserId()).append(DELIMITER);
        sb.append(escapeCSV(user.getEmail())).append(DELIMITER);
        sb.append(escapeCSV(user.password)).append(DELIMITER);
        sb.append(user.getAccountType().name()).append(DELIMITER);
        sb.append(user.isVerified()).append(DELIMITER);
        sb.append(escapeCSV(user.getStatus())).append(DELIMITER);
        sb.append(nullSafe(user.getOrganizationId())).append(DELIMITER);
        sb.append(nullSafe(user.getStudentNumber())).append(DELIMITER);
        
        // Handle super admin flag for admin accounts
        if (user instanceof AdminAccount) {
            sb.append(((AdminAccount) user).isSuperAdmin());
        } else {
            sb.append("false");
        }
        
        return sb.toString();
    }
    
    @Override
    public List<UserAccount> loadAllUsers() {
        List<UserAccount> users = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Skip header row
                }
                
                if (!line.trim().isEmpty()) {
                    UserAccount user = csvToUser(line);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        
        return users;
    }
    
    
    // Converts a CSV line to a UserAccount object.
    private UserAccount csvToUser(String csvLine) {
        try {
            String[] parts = parseCSVLine(csvLine);
            
            if (parts.length < 9) {
                System.err.println("Invalid user CSV line: " + csvLine);
                return null;
            }
            
            int userId = Integer.parseInt(parts[0].trim());
            String email = unescapeCSV(parts[1]);
            String password = unescapeCSV(parts[2]);
            AccountType accountType = AccountType.valueOf(parts[3].trim());
            boolean verified = Boolean.parseBoolean(parts[4].trim());
            String status = unescapeCSV(parts[5]);
            String organizationId = nullRestore(parts[6]);
            String studentNumber = nullRestore(parts[7]);
            boolean isSuperAdmin = Boolean.parseBoolean(parts[8].trim());
            
            // Create appropriate account type
            UserAccount user;
            switch (accountType) {
                case STUDENT:
                    user = new StudentAccount(email, password, organizationId, studentNumber);
                    break;
                case FACULTY:
                    user = new FacultyAccount(email, password, organizationId, studentNumber);
                    break;
                case STAFF:
                    user = new StaffAccount(email, password, organizationId, studentNumber);
                    break;
                case PARTNER:
                    user = new PartnerAccount(email, password, organizationId, studentNumber);
                    break;
                case ADMIN:
                    user = new AdminAccount(email, password, organizationId, studentNumber, isSuperAdmin);
                    break;
                default:
                    return null;
            }
            
            // Set additional fields using reflection or direct access
            user.userId = userId;
            user.verified = verified;
            user.status = status;
            user.accountType = accountType;
            
            return user;
            
        } catch (Exception e) {
            System.err.println("Error parsing user CSV: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public UserAccount findUserByEmail(String email) {
        if (email == null) return null;
        
        for (UserAccount user : loadAllUsers()) {
            if (email.equals(user.getEmail())) {
                return user;
            }
        }
        return null;
    }
    
    @Override
    public UserAccount findUserById(int userId) {
        for (UserAccount user : loadAllUsers()) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }
    
    
    // Updates an existing user in storage.
    private void updateUser(UserAccount updatedUser) {
        List<UserAccount> users = loadAllUsers();
        
        // Rewrite file with updated user
        rewriteUsersFile(users, updatedUser);
    }
    
    
    // Rewrites the users file, replacing the user with matching ID.
    private void rewriteUsersFile(List<UserAccount> users, UserAccount updatedUser) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            writer.println(getUserCSVHeader());
            
            for (UserAccount user : users) {
                if (user.getUserId() == updatedUser.getUserId()) {
                    writer.println(userToCSV(updatedUser));
                } else {
                    writer.println(userToCSV(user));
                }
            }
        } catch (IOException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteUser(int userId) {
        List<UserAccount> users = loadAllUsers();
        boolean found = false;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            writer.println(getUserCSVHeader());
            
            for (UserAccount user : users) {
                if (user.getUserId() == userId) {
                    found = true; // Skip this user (delete)
                } else {
                    writer.println(userToCSV(user));
                }
            }
        } catch (IOException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
        
        return found;
    }
    
    // =============== //
    // ROOM OPERATIONS //
    // =============== //
    
    @Override
    public void saveRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }
        
        // Check if room already exists (update instead)
        if (findRoomById(room.getRoomId()) != null) {
            updateRoom(room);
            return;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(ROOMS_FILE, true))) {
            writer.println(roomToCSV(room));
            System.out.println("Saved room: " + room.getRoomId());
        } catch (IOException e) {
            System.err.println("Error saving room: " + e.getMessage());
        }
    }
    
    
    // Converts a Room to CSV format.
    private String roomToCSV(Room room) {
        StringBuilder sb = new StringBuilder();
        sb.append(room.getRoomId()).append(DELIMITER);
        sb.append(escapeCSV(room.getBuilding())).append(DELIMITER);
        sb.append(escapeCSV(room.getRoomNumber())).append(DELIMITER);
        sb.append(room.getCapacity()).append(DELIMITER);
        sb.append(escapeCSV(room.getStatus()));
        return sb.toString();
    }
    
    @Override
    public List<Room> loadAllRooms() {
        List<Room> rooms = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(ROOMS_FILE))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                if (!line.trim().isEmpty()) {
                    Room room = csvToRoom(line);
                    if (room != null) {
                        rooms.add(room);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading rooms: " + e.getMessage());
        }
        
        return rooms;
    }
    
    
    // Converts a CSV line to a Room object.
    private Room csvToRoom(String csvLine) {
        try {
            String[] parts = parseCSVLine(csvLine);
            
            if (parts.length < 5) {
                System.err.println("Invalid room CSV line: " + csvLine);
                return null;
            }
            
            int roomId = Integer.parseInt(parts[0].trim());
            String building = unescapeCSV(parts[1]);
            String roomNumber = unescapeCSV(parts[2]);
            int capacity = Integer.parseInt(parts[3].trim());
            String status = unescapeCSV(parts[4]);
            
            return new Room(roomId, building, roomNumber, capacity, status);
            
        } catch (Exception e) {
            System.err.println("Error parsing room CSV: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Room findRoomById(int roomId) {
        for (Room room : loadAllRooms()) {
            if (room.getRoomId() == roomId) {
                return room;
            }
        }
        return null;
    }
    
    @Override
    public void updateRoom(Room updatedRoom) {
        List<Room> rooms = loadAllRooms();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(ROOMS_FILE))) {
            writer.println(getRoomCSVHeader());
            
            for (Room room : rooms) {
                if (room.getRoomId() == updatedRoom.getRoomId()) {
                    writer.println(roomToCSV(updatedRoom));
                } else {
                    writer.println(roomToCSV(room));
                }
            }
        } catch (IOException e) {
            System.err.println("Error updating room: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteRoom(int roomId) {
        List<Room> rooms = loadAllRooms();
        boolean found = false;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(ROOMS_FILE))) {
            writer.println(getRoomCSVHeader());
            
            for (Room room : rooms) {
                if (room.getRoomId() == roomId) {
                    found = true;
                } else {
                    writer.println(roomToCSV(room));
                }
            }
        } catch (IOException e) {
            System.err.println("Error deleting room: " + e.getMessage());
            return false;
        }
        
        return found;
    }
    
    // ================== //
    // BOOKING OPERATIONS //
    // ================== //
    
    @Override
    public void saveBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        
        // Check if booking already exists (update instead)
        if (findBookingById(booking.getBookingId()) != null) {
            updateBooking(booking);
            return;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE, true))) {
            writer.println(bookingToCSV(booking));
            System.out.println("Saved booking: " + booking.getBookingId());
        } catch (IOException e) {
            System.err.println("Error saving booking: " + e.getMessage());
        }
    }
    
    // Converts a Booking to CSV format.
    private String bookingToCSV(Booking booking) {
        StringBuilder sb = new StringBuilder();
        sb.append(booking.getBookingId()).append(DELIMITER);
        sb.append(booking.getUser().getUserId()).append(DELIMITER);
        sb.append(booking.getRoom().getRoomId()).append(DELIMITER);
        sb.append(DATE_FORMAT.format(booking.getStartTime())).append(DELIMITER);
        sb.append(DATE_FORMAT.format(booking.getEndTime())).append(DELIMITER);
        sb.append(escapeCSV(booking.getStatus())).append(DELIMITER);
        
        // Handle nullable money fields
        if (booking.getTotalAmount() != null) {
            sb.append(booking.getTotalAmount().getAmount()).append(DELIMITER);
            sb.append(escapeCSV(booking.getTotalAmount().getCurrency())).append(DELIMITER);
        } else {
            sb.append("0.0").append(DELIMITER);
            sb.append("CAD").append(DELIMITER);
        }
        
        if (booking.getDepositAmount() != null) {
            sb.append(booking.getDepositAmount().getAmount()).append(DELIMITER);
            sb.append(escapeCSV(booking.getDepositAmount().getCurrency()));
        } else {
            sb.append("0.0").append(DELIMITER);
            sb.append("CAD");
        }
        
        return sb.toString();
    }
    
    @Override
    public List<Booking> loadAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        
        // Need users and rooms loaded first to reconstruct bookings
        List<UserAccount> users = loadAllUsers();
        List<Room> rooms = loadAllRooms();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                if (!line.trim().isEmpty()) {
                    Booking booking = csvToBooking(line, users, rooms);
                    if (booking != null) {
                        bookings.add(booking);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
        
        return bookings;
    }
    
    
    // Converts a CSV line to a Booking object.
    // Requires users and rooms lists to reconstruct relationships.
    private Booking csvToBooking(String csvLine, List<UserAccount> users, List<Room> rooms) {
        try {
            String[] parts = parseCSVLine(csvLine);
            
            if (parts.length < 10) {
                System.err.println("Invalid booking CSV line: " + csvLine);
                return null;
            }
            
            int bookingId = Integer.parseInt(parts[0].trim());
            int userId = Integer.parseInt(parts[1].trim());
            int roomId = Integer.parseInt(parts[2].trim());
            Date startTime = DATE_FORMAT.parse(parts[3].trim());
            Date endTime = DATE_FORMAT.parse(parts[4].trim());
            String status = unescapeCSV(parts[5]);
            double totalAmount = Double.parseDouble(parts[6].trim());
            String totalCurrency = unescapeCSV(parts[7]);
            double depositAmount = Double.parseDouble(parts[8].trim());
            String depositCurrency = unescapeCSV(parts[9]);
            
            // Find the user and room
            UserAccount user = null;
            for (UserAccount u : users) {
                if (u.getUserId() == userId) {
                    user = u;
                    break;
                }
            }
            
            Room room = null;
            for (Room r : rooms) {
                if (r.getRoomId() == roomId) {
                    room = r;
                    break;
                }
            }
            
            if (user == null || room == null) {
                System.err.println("Could not find user or room for booking: " + bookingId);
                return null;
            }
            
            // Create booking with full constructor
            Money total = new Money(totalAmount, totalCurrency);
            Money deposit = new Money(depositAmount, depositCurrency);
            Booking booking = new Booking(bookingId, startTime, endTime, total, deposit, user, room);
            
            // Restore the correct state based on status
            restoreBookingState(booking, status);
            
            return booking;
            
        } catch (ParseException e) {
            System.err.println("Error parsing booking date: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing booking CSV: " + e.getMessage());
            return null;
        }
    }
    
    
    // Restores the booking's state based on the saved status string.
    private void restoreBookingState(Booking booking, String status) {
        switch (status.toUpperCase()) {
            case "CREATED":
                // Already in created state by default
                break;
            case "CHECKEDIN":
                booking.setState(new CheckedInState());
                break;
            case "COMPLETED":
                booking.setState(new CompletedState());
                break;
            case "CANCELLED":
                booking.setState(new CancelledState());
                break;
            case "EXPIRED":
                booking.setState(new ExpiredState());
                break;
            default:
                System.err.println("Unknown booking status: " + status);
        }
    }
    
    @Override
    public Booking findBookingById(int bookingId) {
        for (Booking booking : loadAllBookings()) {
            if (booking.getBookingId() == bookingId) {
                return booking;
            }
        }
        return null;
    }
    
    @Override
    public List<Booking> findBookingsByUserId(int userId) {
        List<Booking> userBookings = new ArrayList<>();
        
        for (Booking booking : loadAllBookings()) {
            if (booking.getUser().getUserId() == userId) {
                userBookings.add(booking);
            }
        }
        
        return userBookings;
    }
    
    @Override
    public void updateBooking(Booking updatedBooking) {
        List<UserAccount> users = loadAllUsers();
        List<Room> rooms = loadAllRooms();
        List<Booking> bookings = loadAllBookings();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE))) {
            writer.println(getBookingCSVHeader());
            
            for (Booking booking : bookings) {
                if (booking.getBookingId() == updatedBooking.getBookingId()) {
                    writer.println(bookingToCSV(updatedBooking));
                } else {
                    writer.println(bookingToCSV(booking));
                }
            }
        } catch (IOException e) {
            System.err.println("Error updating booking: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteBooking(int bookingId) {
        List<Booking> bookings = loadAllBookings();
        boolean found = false;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE))) {
            writer.println(getBookingCSVHeader());
            
            for (Booking booking : bookings) {
                if (booking.getBookingId() == bookingId) {
                    found = true;
                } else {
                    writer.println(bookingToCSV(booking));
                }
            }
        } catch (IOException e) {
            System.err.println("Error deleting booking: " + e.getMessage());
            return false;
        }
        
        return found;
    }
    
    // ================== //
    // UTILITY OPERATIONS //
    // ================== //
    
    @Override
    public void clearAll() {
        // Rewrite all files with headers
    	try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            writer.println(getUserCSVHeader());
        } catch (IOException e) {
            System.err.println("Error clearing users file: " + e.getMessage());
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(ROOMS_FILE))) {
            writer.println(getRoomCSVHeader());
        } catch (IOException e) {
            System.err.println("Error clearing rooms file: " + e.getMessage());
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE))) {
            writer.println(getBookingCSVHeader());
        } catch (IOException e) {
            System.err.println("Error clearing bookings file: " + e.getMessage());
        }
        
        System.out.println("All storage cleared.");
    }
    
    // ================== //
    // CSV HELPER METHODS //
    // ================== //
    
    // Escapes a string for CSV format (handles commas and quotes).
    private String escapeCSV(String value) {
        if (value == null) {
            return NULL_PLACEHOLDER;
        }
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    
    // Unescapes a CSV value.
    private String unescapeCSV(String value) {
        if (value == null || value.equals(NULL_PLACEHOLDER)) {
            return null;
        }
        value = value.trim();
        // Remove surrounding quotes and unescape internal quotes
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\"\"", "\"");
        }
        return value;
    }
    
    
    // Handles null values for CSV.
    private String nullSafe(String value) {
        return value == null ? NULL_PLACEHOLDER : escapeCSV(value);
    }
    

    // Restores null from placeholder.
    private String nullRestore(String value) {
        if (value == null || value.trim().equals(NULL_PLACEHOLDER)) {
            return null;
        }
        return unescapeCSV(value);
    }
    

    // Parses a CSV line handling quoted fields.
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // Check for escaped quote
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString()); // Add last field
        
        return result.toArray(new String[0]);
    }
}