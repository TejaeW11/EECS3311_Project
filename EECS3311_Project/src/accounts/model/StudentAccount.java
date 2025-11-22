package accounts.model;

public class StudentAccount extends UserAccount{

	public StudentAccount(String email, String password,
            String organizationId, String studentNumber) {
		super(email, password, AccountType.STUDENT, organizationId, studentNumber);
		
	}

	
}
