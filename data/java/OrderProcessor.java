public class OrderProcessor {

    private String customerName;
    private String customerAddress;
    private String customerEmail;
    private String productId;
    private int quantity;
    private double unitPrice;
    private double discountRate;
    private String shippingMethod;
    private String paymentMethod;
    private boolean isRushOrder;
    private String specialInstructions;
    private String trackingNumber; // Only used after processing

    public OrderProcessor(String customerName, String customerAddress, String customerEmail, String productId,
                          int quantity, double unitPrice, double discountRate, String shippingMethod,
                          String paymentMethod, boolean isRushOrder, String specialInstructions) {
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerEmail = customerEmail;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountRate = discountRate;
        this.shippingMethod = shippingMethod;
        this.paymentMethod = paymentMethod;
        this.isRushOrder = isRushOrder;
        this.specialInstructions = specialInstructions;
        this.trackingNumber = null; // Initially null
    }

    public void processOrder(String couponCode, String giftMessage) {
        double totalPrice = quantity * unitPrice;
        totalPrice -= totalPrice * discountRate;

        if (couponCode != null && !couponCode.isEmpty()) {
            if (couponCode.equals("SUMMER20")) {
                totalPrice *= 0.8; // Apply 20% off
            } else if (couponCode.equals("FREESHIP")) {
                this.shippingMethod = "Free";
            }
        }

        double shippingCost = 0;
        switch (shippingMethod) {
            case "Standard":
                shippingCost = 5.0;
                break;
            case "Express":
                shippingCost = 15.0;
                break;
            case "Priority":
                shippingCost = 10.0;
                break;
            case "Free":
                shippingCost = 0.0;
                break;
            default:
                shippingCost = 7.0;
        }

        if (isRushOrder) {
            shippingCost += 10.0;
        }

        totalPrice += shippingCost;

        if (paymentMethod.equals("CreditCard")) {
            // Perform credit card processing (simplified)
            System.out.println("Processing credit card payment...");
        } else if (paymentMethod.equals("PayPal")) {
            // Perform PayPal processing (simplified)
            System.out.println("Processing PayPal payment...");
        }

        if (giftMessage != null && !giftMessage.isEmpty()) {
            System.out.println("Adding gift message: " + giftMessage);
        }

        if (specialInstructions != null && !specialInstructions.isEmpty()) {
            System.out.println("Special instructions: " + specialInstructions);
        }

        // Generate a tracking number (very basic example)
        this.trackingNumber = "TRACK-" + System.currentTimeMillis();
        System.out.println("Order processed for: " + customerName);
        System.out.println("Total Price: $" + String.format("%.2f", totalPrice));
        System.out.println("Shipping via: " + shippingMethod + " (Cost: $" + String.format("%.2f", shippingCost) + ")");
        System.out.println("Tracking Number: " + this.trackingNumber);
    }

    public void sendConfirmationEmail() {
        String subject = "Order Confirmation";
        String body = "Dear " + customerName + ",\n\n" +
                      "Your order for product " + productId + " (quantity: " + quantity + ") has been processed.\n" +
                      "Tracking number: " + this.trackingNumber + "\n\n" +
                      "Thank you for your order!";
        System.out.println("Sending email to: " + customerEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Body:\n" + body);
    }

    // A method that doesn't do much
    public String getCustomerDetails() {
        return "Customer: " + customerName + ", Email: " + customerEmail;
    }

    public static void main(String[] args) {
        OrderProcessor order = new OrderProcessor(
                "John Doe",
                "123 Main St",
                "john.doe@example.com",
                "PRODUCT456",
                2,
                25.00,
                0.10,
                "Standard",
                "CreditCard",
                false,
                "Leave at front door"
        );
        order.processOrder("SUMMER20", "Happy Birthday!");
        order.sendConfirmationEmail();
        System.out.println(order.getCustomerDetails());
    }
}