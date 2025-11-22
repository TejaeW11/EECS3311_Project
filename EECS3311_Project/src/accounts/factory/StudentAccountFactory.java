package accounts.factory;

import accounts.model.StudentAccount;
import accounts.model.UserAccount;

public class StudentAccountFactory implements AccountFactory{

	@Override
	public UserAccount createUserAccount(String email, String password, String organizationId, String studentNumber) {
		
		if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required for account creation.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required for account creation.");
        }
        if (studentNumber == null || studentNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Student number is required for student accounts.");
        }
		return new StudentAccount(email,password,organizationId,studentNumber);
	}

}
