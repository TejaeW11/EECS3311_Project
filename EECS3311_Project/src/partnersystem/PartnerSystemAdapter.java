package partnersystem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import manager.room.Room;

public class PartnerSystemAdapter implements RoomAvailabilityService{

	private PartnerRoomSystem adaptee;
	
	public PartnerSystemAdapter(PartnerRoomSystem adaptee) {
		if (adaptee == null) {
            throw new IllegalArgumentException("Partner room system cannot be null.");
        }
		
		this.adaptee = adaptee;
	}


	@Override
	public List<Room> findAvailableRooms(Date startTime, Date endTime, int capacity) {
		
		List<PartnerRoomRecord> partnerRooms = adaptee.queryRooms(startTime, endTime);
	    List<Room> rooms = new ArrayList<>();
		
	    for (PartnerRoomRecord partnerRoom : partnerRooms) {
            // TAKE ANOTHER LOOK AT LOGIC HERE
	    	if (partnerRoom.getMaxPeople() >= capacity) {
	    		Room room = convertToRoom(partnerRoom);
	            rooms.add(room);
	    	}
	    	
        }
	    return rooms;
	}
	
	private Room convertToRoom(PartnerRoomRecord partnerRoom) {
        String[] locationParts = partnerRoom.getLocation().split("-");
        String building = locationParts.length > 0 ? locationParts[0] : "Partner";
        String roomNumber = locationParts.length > 1 ? locationParts[1] : partnerRoom.getExternalRoomId();

        String onlyNum = partnerRoom.getExternalRoomId().replaceAll("[^0-9]", "");
        int roomId = onlyNum.isEmpty() ? 0 : Integer.parseInt(onlyNum);
        
        roomId = roomId +1000; // NEW ID CONVENTION FOR PARTNER ROOMS TO AVOID CONFLICT WITH NON-PARTNER ROOMS
        
        return new Room(roomId,building,roomNumber,partnerRoom.getMaxPeople(),"Available");
    }
	

}
