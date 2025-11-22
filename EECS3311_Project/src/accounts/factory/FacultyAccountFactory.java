package accounts.factory;

import accounts.model.FacultyAccount;
import accounts.model.UserAccount;

public class FacultyAccountFactory implements AccountFactory{

	@Override
	public UserAccount createUserAccount(String email, String password, String organizationId, String studentNumber) {
		
		if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required for account creation.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required for account creation.");
        }
        if (organizationId == null || organizationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Organization ID is required for faculty accounts.");
        }
		return new FacultyAccount(email,password,organizationId,studentNumber);
	}
}
