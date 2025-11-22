package manager.payment;

import accounts.model.AccountType;
import accounts.model.UserAccount;
import booking.model.Booking;
import pricing.money.Money;
import pricing.strategy.*;

public class PaymentService {

	private PricingStrategy strategy; // Likley redundant now
	private PaymentAdapter adapter;

	public PaymentService(PricingStrategy strategy, PaymentAdapter adapter) {
		this.strategy = strategy;
		this.adapter = adapter != null ? adapter : new PaymentAdapterImpl(PaymentMethod.CREDIT);
	}
	
	public PaymentService() {
        this.strategy = null;
        this.adapter = new PaymentAdapterImpl(PaymentMethod.CREDIT);
    }
	
	public void setAdapter(PaymentAdapter adapter) {
        this.adapter = adapter;
    }
	
	public void setStrategy(PricingStrategy strategy) {
		this.strategy = strategy;
	}
	
	// Internal pricing type checks here 
	public Money calculateBookingPrice(Booking booking, UserAccount account) {
		
		if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }
		
		PricingStrategy strategy = getStrategyForAccountType(account.getAccountType());
		return strategy.calculatePrice(booking);
	}
	
	public Money calculateDeposit(Booking booking, UserAccount account) {
		if(booking == null || account == null) {
			throw new IllegalArgumentException("Booking and account cannot be null.");
		}
		
		
		double hourlyRate = getHourlyRateForAccountType(account.getAccountType());
        return new Money(hourlyRate, "CAD");
	}
	
	
	private PricingStrategy getStrategyForAccountType(AccountType accountType) {
	    switch (accountType) {
	        case STUDENT:
	            return new StudentPricingStrategy();
	        case FACULTY:
	            return new FacultyPricingStrategy();
	        case STAFF:
	            return new StaffPricingStrategy();
	        case PARTNER:
	            return new PartnerPricingStrategy();
	        case ADMIN:
	        default:
	            return new DefaultPricingStrategy();
	    } 
	    
	}
	
	private double getHourlyRateForAccountType(AccountType accountType) {
        switch (accountType) {
            case STUDENT: return 20.0;
            case FACULTY: return 30.0;
            case STAFF: return 40.0;
            case PARTNER: return 50.0;
            case ADMIN:
            default: return 50.0;
        }
    }
	
	public void pay(Booking booking, PaymentMethod method) {
		if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }
        if (booking.getTotalAmount() == null) {
            throw new IllegalStateException("Booking total amount not set.");
        }
        
        if (adapter instanceof PaymentAdapterImpl) {
            ((PaymentAdapterImpl) adapter).setPaymentMethod(method);
        }
		
		double amount = booking.totalAmount.amount;
        adapter.processPayment(amount);
	}
	
	
	public boolean payDeposit(Booking booking, PaymentMethod method) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }
        if (booking.getDepositAmount() == null) {
            throw new IllegalStateException("Booking deposit amount not set.");
        }
        
        if (adapter instanceof PaymentAdapterImpl) {
            ((PaymentAdapterImpl) adapter).setPaymentMethod(method);
        }
        
        System.out.println("Processing deposit payment...");
        double amount = booking.getDepositAmount().getAmount();
        return adapter.processPayment(amount);
    }
    
    
    public boolean payRemainingBalance(Booking booking, PaymentMethod method) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }
        if (booking.getTotalAmount() == null || booking.getDepositAmount() == null) {
            throw new IllegalStateException("Booking amounts not set.");
        }
        
        if (adapter instanceof PaymentAdapterImpl) {
            ((PaymentAdapterImpl) adapter).setPaymentMethod(method);
        }
        
        double remaining = booking.getTotalAmount().getAmount() - booking.getDepositAmount().getAmount();
        if (remaining > 0) {
            System.out.println("Processing remaining balance...");
            return adapter.processPayment(remaining);
        }
        return true; 
    }
	
}
