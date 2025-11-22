package accounts.model;

public class FacultyAccount extends UserAccount{

	public FacultyAccount(String email, String password,
            String organizationId, String studentNumber) {
		super(email, password, AccountType.FACULTY, organizationId, studentNumber);
		
	}
}
