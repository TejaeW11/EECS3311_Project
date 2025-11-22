package partnersystem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PartnerRoomSystem {
	
	private List<PartnerRoomRecord> rooms;


    public PartnerRoomSystem() {
        this.rooms = new ArrayList<>();
    }

    public void addPartnerRoom(PartnerRoomRecord room) {
    	if (room != null) {
            rooms.add(room);
        }
    }
	
	public List<PartnerRoomRecord> queryRooms(Date fromDateTime, Date toDateTime){
		
		if (fromDateTime == null || toDateTime == null) {
            return new ArrayList<>();
        } 
		
		List<PartnerRoomRecord> available = new ArrayList<>();
        for (PartnerRoomRecord room : rooms) {
            if (room.isActive() && !hasConflict(room,fromDateTime,toDateTime)) {
            	available.add(room);
            }
        }
    	return available;
		
	}
	
	private boolean hasConflict(PartnerRoomRecord room, Date fromDateTime, Date toDateTime) {
		// If room has no existing booking times
		if (room.getStartTime() == null || room.getEndTime() == null) {
            return false;
        }
		
		Date roomStart = room.getStartTime();
		Date roomEnd = room.getEndTime();
		    
		   
    	return fromDateTime.before(roomEnd) && roomStart.before(toDateTime);
		
	}
}
