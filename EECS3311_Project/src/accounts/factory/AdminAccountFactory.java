package accounts.factory;

import accounts.model.AdminAccount;
import accounts.model.UserAccount;

public class AdminAccountFactory implements AccountFactory{

	private boolean creatorisSuperAdmin;
	private boolean newAdminIsSuperAdmin;
	
	public AdminAccountFactory(boolean creatorisSuperAdmin, boolean newAdminIsSuperAdmin) {
		this.creatorisSuperAdmin = creatorisSuperAdmin;
		this.newAdminIsSuperAdmin = newAdminIsSuperAdmin;
	}
	
	public AdminAccountFactory(boolean creatorisSuperAdmin) {
		this.creatorisSuperAdmin = creatorisSuperAdmin;
		this.newAdminIsSuperAdmin = false;
	}
	
	@Override
	public UserAccount createUserAccount(String email, String password, String organizationId, String studentNumber) {
		
		// Use an if statement with "creatorisSuperAdmin" to check boolean
		// Work out logic further in implementation
		
		if(!creatorisSuperAdmin) {
			throw new SecurityException("Only a Super Admin can create an Admin account."); 
		}
		if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required for account creation.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required for account creation.");
        }
		return new AdminAccount(email,password,organizationId,studentNumber,newAdminIsSuperAdmin);
		
	}
}
