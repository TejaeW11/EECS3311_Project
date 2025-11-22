package partnersystem;

import java.util.Date;

public class PartnerRoomRecord {
	
	public String externalRoomId;
	public String location;
	public int maxPeople;
	public boolean isActive;
	public Date startTime;
	public Date endTime;
	
	public PartnerRoomRecord(String externalRoomId, String location, int maxPeople, boolean isActive, Date startTime,
			Date endTime) {
		this.externalRoomId = externalRoomId;
		this.location = location;
		this.maxPeople = maxPeople;
		this.isActive = isActive;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	// GETTERS
	public String getExternalRoomId() { return externalRoomId; }
    public String getLocation() { return location; }
    public int getMaxPeople() { return maxPeople; }
    public boolean isActive() { return isActive; }
    public Date getStartTime() { return startTime; }
    public Date getEndTime() { return endTime; }
	
}
