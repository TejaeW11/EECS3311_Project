package accounts.model;

public class PartnerAccount extends UserAccount{

	public PartnerAccount(String email, String password,
            String organizationId, String studentNumber) {
		super(email, password, AccountType.PARTNER, organizationId, studentNumber);
		
	}
}
