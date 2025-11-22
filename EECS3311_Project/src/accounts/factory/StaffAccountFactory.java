package accounts.factory;

import accounts.model.StaffAccount;
import accounts.model.UserAccount;

public class StaffAccountFactory implements AccountFactory{

	@Override
	public UserAccount createUserAccount(String email, String password, String organizationId, String studentNumber) {
		if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required for account creation.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required for account creation.");
        }
        if (organizationId == null || organizationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Organization ID is required for staff accounts.");
        }
		return new StaffAccount(email,password,organizationId,studentNumber);
	}
}
