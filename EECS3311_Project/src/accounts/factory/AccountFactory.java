package accounts.factory;

import accounts.model.UserAccount;

public interface AccountFactory {

	public UserAccount createUserAccount(String email, String password, String organizationId,
			String studentNumber);
}
