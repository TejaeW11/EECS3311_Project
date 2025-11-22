package accounts.model;

public class StaffAccount extends UserAccount{

	
	public StaffAccount(String email, String password,
            String organizationId, String studentNumber) {
		super(email, password, AccountType.STAFF, organizationId, studentNumber);
		
	}
}
