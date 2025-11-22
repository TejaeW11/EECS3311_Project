package accounts.model;

public class AdminAccount extends UserAccount{

	public boolean isSuperAdmin;
	
	public AdminAccount(String email, String password,
            String organizationId, String studentNumber, boolean isSuperAdmin) {
		super(email, password, AccountType.ADMIN, organizationId, studentNumber);
		this.isSuperAdmin = isSuperAdmin;
		
	}
	
	public boolean isSuperAdmin() {
		return isSuperAdmin;
	}
	
	
}
