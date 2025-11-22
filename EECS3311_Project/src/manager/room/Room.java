package manager.room;

public class Room {

	public int roomId;
	public String building;
	public String roomNumber;
	public int capacity;
	public String status;
	
	
	
	public Room(int roomId, String building, String roomNumber, int capacity, String status) {
		
		if (roomId < 0) {
            throw new IllegalArgumentException("Room ID cannot be negative.");
        }
        if (building == null || building.trim().isEmpty()) {
            throw new IllegalArgumentException("Building name is required.");
        }
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number is required.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
		
		this.roomId = roomId;
		this.building = building;
		this.roomNumber = roomNumber;
		this.capacity = capacity;
		this.status = status;
	}

	
	// SETTERS
	public void setStatus(String status) { 
        
        if (status == null || (!status.equals("Enabled") && !status.equals("Disabled") 
                && !status.equals("Maintenance"))) {
            throw new IllegalArgumentException("Status must be 'Enabled', 'Disabled', or 'Maintenance'.");
        }
        this.status = status; 
    }

	public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        this.capacity = capacity;
    }
	
	
	// GETTERS
	public int getRoomId() {
		return roomId;
	}
	public String getBuilding() {
		return building;
	}
	public String getRoomNumber() {
		return roomNumber;
	}
	public int getCapacity() {
		return capacity;
	}
	public String getStatus() {
		return status;
	}

	
	@Override
    public String toString() {
        return String.format("Room[%d] %s-%s (Cap: %d, Status: %s)", 
                roomId, building, roomNumber, capacity, status);
    }
	
	
}
