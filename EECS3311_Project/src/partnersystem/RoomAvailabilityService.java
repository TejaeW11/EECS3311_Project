package partnersystem;

import java.util.Date;
import java.util.List;

import manager.room.Room;

public interface RoomAvailabilityService {
	public List<Room> findAvailableRooms(Date startTime, Date endTime, int capacity);
}
