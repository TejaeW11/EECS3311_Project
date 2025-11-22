package pricing.money;

public class Money {

	public double amount;
	public String currency;
	
	public Money(double amount, String currency) {
		
		if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency must be specified.");
        }
		
		this.amount = amount;
		this.currency = currency;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public Money add(Money newMoney) {
        if (!this.currency.equals(newMoney.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies.");
        }
        return new Money(this.amount + newMoney.amount, this.currency);
    }
	
	public Money subtract(Money subMoney) {
        if (!this.currency.equals(subMoney.currency)) {
            throw new IllegalArgumentException("Cannot subtract money with different currencies.");
        }
        return new Money(this.amount - subMoney.amount, this.currency);
    }

    @Override
    public String toString() {
        return String.format("%.2f %s", amount, currency);
    }
	
	
}
