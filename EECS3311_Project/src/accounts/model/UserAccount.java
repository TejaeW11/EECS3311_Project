package accounts.model;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class UserAccount {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1000);
	
	public int userId;
	public String email;
	public String password;
	public AccountType accountType;
	public boolean verified;
	public String status;
	public String organizationId;
	public String studentNumber;
	
	public UserAccount(int userId, String email, String password, AccountType accountType, boolean verified,
			String status, String organizationId, String studentNumber) {
		
		this.userId = userId;
		this.email = email;
		this.password = password;
		this.accountType = accountType;
		this.verified = verified;
		this.status = status;
		this.organizationId = organizationId;
		this.studentNumber = studentNumber;
	}
	
	public UserAccount(String email, String password, AccountType accountType, 
	             String organizationId, String studentNumber) {
		this.userId = ID_GENERATOR.getAndIncrement();
		this.email = email;
		this.password = password;
		this.accountType = accountType;
		this.organizationId = organizationId;
		this.studentNumber = studentNumber;
		this.verified = false; 
		this.status = "ACTIVE"; 
	}

	public UserAccount(String email, String password, String organizationId,
                 String studentNumber) {
		this.userId = ID_GENERATOR.getAndIncrement();
		this.email = email;
		this.password = password;
		this.organizationId = organizationId;
		this.studentNumber = studentNumber;
		this.verified = false;
		this.status = "ACTIVE";
	}
	
	public String getEmail() { return email; }
    public int getUserId() { return userId; }
    public AccountType getAccountType() { return accountType; }
    public String getOrganizationId() { return organizationId; }
    public String getStudentNumber() { return studentNumber; }
    public boolean isVerified() { return verified; } 
    public String getStatus() { return status; } 
    
    
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setStatus(String status) { this.status = status; }
}
