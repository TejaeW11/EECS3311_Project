package manager.payment;


// LOOK THROUGH THIS CLASS 
// NOT IN DESIGNS
// NOT ENTIRELY NEED BUT GIVES PaymentAdapter Interface use as it is otherwise unused.

public class PaymentAdapterImpl implements PaymentAdapter {
    private PaymentMethod paymentMethod;

    public PaymentAdapterImpl(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.CREDIT;
    }

    @Override
    public boolean processPayment(double amount) {
        
        if (amount <= 0) {
            System.err.println("Payment failed: Amount must be positive.");
            return false;
        }
        
        
        System.out.println("=== PROCESSING PAYMENT ===");
        System.out.println("Method: " + paymentMethod);
        System.out.println("Amount: $" + String.format("%.2f", amount));
        
        // WHERE ACTUAL PAYMENTS WOULD BE MADE/GATEWAY APIs
        switch (paymentMethod) {
            case CREDIT:
                System.out.println("Processing credit card payment...");
                break;
            case DEBIT:
                System.out.println("Processing debit card payment...");
                break;
            case INSTITUTIONAL:
                System.out.println("Processing institutional billing...");
                break;
        }
        
        System.out.println("Payment successful!");
        System.out.println("==========================");
        return true; 
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
