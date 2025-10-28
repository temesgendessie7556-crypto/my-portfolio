import java.util.*;

// Custom exception for domain-specific errors
class ShoppingException extends Exception {
    public ShoppingException(String message) { super(message); }
}

// Payment record to track payment method and amount
class PaymentRecord {
    private PaymentMethod method;
    private double amount;
    public PaymentRecord(PaymentMethod method, double amount) {
        this.method = method; this.amount = amount;
    }
    public String getMethodName() { return method.getClass().getSimpleName(); }
    public double getAmount() { return amount; }
}

// =================== Product and Subclasses ===================

abstract class Product {
    private String id, name;
    private double price;
    private int stock;

    public Product(String id, String name, double price, int stock) throws ShoppingException {
        if (price < 0 || stock < 0) throw new ShoppingException("Price and stock must be non-negative.");
        this.id = id; this.name = name; this.price = price; this.stock = stock;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public void setStock(int stock) throws ShoppingException {
        if (stock < 0) throw new ShoppingException("Stock cannot be negative.");
        this.stock = stock;
    }
    public boolean isAvailable() { return stock > 0; }
    public void decreaseStock(int qty) throws ShoppingException {
        if (qty > stock) throw new ShoppingException("Insufficient stock for " + name);
        this.stock -= qty;
    }
    public abstract void displayDetails();
}

class Electronics extends Product {
    private String brand;
    public Electronics(String id, String name, double price, int stock, String brand) throws ShoppingException {
        super(id, name, price, stock); this.brand = brand;
    }
    @Override
    public void displayDetails() {
        System.out.printf("ID: %s | Electronics: %s (Brand: %s) - $%.2f | Stock: %d%s\n",
            getId(), getName(), brand, getPrice(), getStock(), getStock() == 0 ? " [SOLD OUT]" : "");
    }
}

class Clothing extends Product {
    private String size;
    public Clothing(String id, String name, double price, int stock, String size) throws ShoppingException {
        super(id, name, price, stock); this.size = size;
    }
    @Override
    public void displayDetails() {
        System.out.printf("ID: %s | Clothing: %s (Size: %s) - $%.2f | Stock: %d%s\n",
            getId(), getName(), size, getPrice(), getStock(), getStock() == 0 ? " [SOLD OUT]" : "");
    }
}

// =================== Payment and Cart ===================

interface PaymentMethod {
    boolean pay(double amount) throws ShoppingException;
    String getDetails();
    String getBalance();
}

class CreditCard implements PaymentMethod {
    private String cardNumber;
    private double balance;
    public CreditCard(String cardNumber, double balance) throws ShoppingException {
        if (!cardNumber.matches("\\d{16}")) throw new ShoppingException("Invalid credit card number (must be 16 digits).");
        if (balance < 0) throw new ShoppingException("Credit card balance cannot be negative.");
        this.cardNumber = cardNumber; this.balance = balance;
    }
    @Override
    public boolean pay(double amount) throws ShoppingException {
        if (amount > balance) throw new ShoppingException("Insufficient funds on credit card ending in " + cardNumber.substring(12));
        balance -= amount;
        System.out.printf("Paid $%.2f using Credit Card ending in %s. New balance: $%.2f\n", amount, cardNumber.substring(12), balance);
        return true;
    }
    @Override
    public String getDetails() { return "Credit Card ending in " + cardNumber.substring(12); }
    @Override
    public String getBalance() { return String.format("$%.2f", balance); }
    public void addFunds(double amount) throws ShoppingException {
        if (amount <= 0) throw new ShoppingException("Amount to add must be positive.");
        balance += amount;
        System.out.printf("Added $%.2f to Credit Card ending in %s. New balance: $%.2f\n", amount, cardNumber.substring(12), balance);
    }
}

class PayPal implements PaymentMethod {
    private String email;
    private double balance;
    public PayPal(String email, double balance) throws ShoppingException {
        if (!email.matches(".+@.+\\..+")) throw new ShoppingException("Invalid PayPal email format.");
        if (balance < 0) throw new ShoppingException("PayPal balance cannot be negative.");
        this.email = email; this.balance = balance;
    }
    @Override
    public boolean pay(double amount) throws ShoppingException {
        if (amount > balance) throw new ShoppingException("Insufficient funds in PayPal account (Email: " + email + ")");
        balance -= amount;
        System.out.printf("Paid $%.2f using PayPal (Email: %s). New balance: $%.2f\n", amount, email, balance);
        return true;
    }
    @Override
    public String getDetails() { return "PayPal (Email: " + email + ")"; }
    @Override
    public String getBalance() { return String.format("$%.2f", balance); }
    public void addFunds(double amount) throws ShoppingException {
        if (amount <= 0) throw new ShoppingException("Amount to add must be positive.");
        balance += amount;
        System.out.printf("Added $%.2f to PayPal (Email: %s). New balance: $%.2f\n", amount, email, balance);
    }
}

class CartItem {
    private Product product;
    private int quantity;
    public CartItem(Product product, int quantity) {
        this.product = product; this.quantity = quantity;
    }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

class Cart {
    private List<CartItem> items = new ArrayList<>();
    public void addProduct(Product p, int qty) throws ShoppingException {
        if (qty <= 0) throw new ShoppingException("Quantity must be positive.");
        if (qty > p.getStock()) throw new ShoppingException("Requested quantity exceeds stock for " + p.getName());
        for (CartItem ci : items) {
            if (ci.getProduct().getId().equals(p.getId())) {
                ci.setQuantity(ci.getQuantity() + qty);
                return;
            }
        }
        items.add(new CartItem(p, qty));
    }
    public void removeProduct(String productId) {
        items.removeIf(ci -> ci.getProduct().getId().equals(productId));
    }
    public void displayCart() {
        if (items.isEmpty()) { System.out.println("Cart is empty."); return; }
        System.out.println("Your Cart:");
        for (CartItem ci : items)
            System.out.printf("%s x%d ($%.2f each)\n", ci.getProduct().getName(), ci.getQuantity(), ci.getProduct().getPrice());
    }
    public double getTotal() {
        double total = 0;
        for (CartItem ci : items) total += ci.getProduct().getPrice() * ci.getQuantity();
        return total;
    }
    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }
    public boolean isEmpty() { return items.isEmpty(); }
    public void clear() { items.clear(); }
}

// =================== Customer and Order History ===================

class Order {
    private final List<CartItem> items;
    private final double totalPaid;
    private final Date date;
    private final List<PaymentRecord> payments;
    public Order(List<CartItem> items, double totalPaid, List<PaymentRecord> payments) {
        this.items = new ArrayList<>(items);
        this.totalPaid = totalPaid;
        this.date = new Date();
        this.payments = new ArrayList<>(payments);
    }
    public void displayOrder() {
        System.out.printf("Order Date: %s | Total Paid: $%.2f\n", date, totalPaid);
        System.out.println("Items:");
        for (CartItem ci : items)
            System.out.printf("  %s x%d ($%.2f each)\n", ci.getProduct().getName(), ci.getQuantity(), ci.getProduct().getPrice());
        System.out.println("Payments:");
        for (PaymentRecord pr : payments)
            System.out.printf("  %s: $%.2f\n", pr.getMethodName(), pr.getAmount());
    }
}

class Customer {
    private String name;
    private Cart cart = new Cart();
    private List<Order> orderHistory = new ArrayList<>();
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    public Customer(String name) throws ShoppingException {
        if (name == null || name.trim().isEmpty()) throw new ShoppingException("Customer name cannot be empty.");
        this.name = name;
    }
    public Cart getCart() { return cart; }
    public String getName() { return name; }
    public void addOrder(Order order) { orderHistory.add(order); }
    public void showOrderHistory() {
        if (orderHistory.isEmpty()) { System.out.println("No orders yet."); return; }
        System.out.println("Order History for " + name + ":");
        for (Order o : orderHistory) o.displayOrder();
    }
    public void addPaymentMethod(PaymentMethod method) { paymentMethods.add(method); }
    public void removePaymentMethod(int index) throws ShoppingException {
        if (index < 0 || index >= paymentMethods.size()) throw new ShoppingException("Invalid payment method index.");
        paymentMethods.remove(index);
    }
    public List<PaymentMethod> getPaymentMethods() { return Collections.unmodifiableList(paymentMethods); }
    public void displayPaymentMethods() {
        if (paymentMethods.isEmpty()) { System.out.println("No payment methods registered."); return; }
        System.out.println("Registered Payment Methods:");
        for (int i = 0; i < paymentMethods.size(); i++)
            System.out.printf("%d. %s | Balance: %s\n", i + 1, paymentMethods.get(i).getDetails(), paymentMethods.get(i).getBalance());
    }
}

// =================== Admin Authentication ===================

class AdminSession {
    private static final Map<String, String> ADMINS = Map.of("admin", "1234");
    private boolean loggedIn = false;

    public boolean isLoggedIn() { return loggedIn; }

    public boolean login(String username, String password) {
        if (ADMINS.containsKey(username) && ADMINS.get(username).equals(password)) {
            loggedIn = true;
            System.out.println("Admin login successful.");
            return true;
        }
        System.out.println("Invalid admin credentials.");
        return false;
    }

    public void logout() {
        loggedIn = false;
        System.out.println("Admin logged out.");
    }
}

// =================== Store Management ===================

class StoreManager {
    private Map<String, Product> store = new HashMap<>();

    public void addProduct(Product product) throws ShoppingException {
        if (store.containsKey(product.getId())) throw new ShoppingException("Product ID " + product.getId() + " already exists.");
        store.put(product.getId(), product);
    }

    public Product getProduct(String id) { return store.get(id); }

    public Collection<Product> getAllProducts() { return store.values(); }

    public boolean isProductIdUnique(String id) { return !store.containsKey(id); }
}

// =================== Main Application ===================

public class OnlineShoppingApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AdminSession adminSession = new AdminSession();
        StoreManager storeManager = new StoreManager();

        System.out.println("======================================");
        System.out.println(" Welcome to the Simple Online Shop!");
        System.out.println("======================================");

        // Initialize store
        try {
            storeManager.addProduct(new Electronics("E01", "Smartphone", 299.99, 5, "Samsung"));
            storeManager.addProduct(new Electronics("E02", "Laptop", 799.99, 2, "Dell"));
            storeManager.addProduct(new Clothing("C01", "T-shirt", 19.99, 10, "M"));
            storeManager.addProduct(new Clothing("C02", "Jeans", 39.99, 7, "L"));
        } catch (ShoppingException e) {
            System.out.println("Error initializing store: " + e.getMessage());
            return;
        }

        System.out.print("Enter your name: ");
        Customer customer;
        try {
            customer = new Customer(sc.nextLine());
            // Add default payment methods for demo
            customer.addPaymentMethod(new CreditCard("1234567890123456", 1000.00));
            customer.addPaymentMethod(new PayPal("user@example.com", 500.00));
        } catch (ShoppingException e) {
            System.out.println("Error: " + e.getMessage());
            sc.close();
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println("\nMenu:");
            System.out.println("1. View Products");
            System.out.println("2. Add to Cart");
            System.out.println("3. Remove from Cart");
            System.out.println("4. View Cart");
            System.out.println("5. Checkout");
            System.out.println("6. Admin Login");
            System.out.println("7. Add Product (Admin Only)");
            System.out.println("8. Admin Logout");
            System.out.println("9. Order History");
            System.out.println("10. Manage Payment Methods");
            System.out.println("11. Exit");
            System.out.print("Choose an option: ");
            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        System.out.println("Available Products:");
                        for (Product p : storeManager.getAllProducts()) p.displayDetails();
                        break;
                    case 2:
                        System.out.print("Enter Product ID to add: ");
                        String addId = sc.nextLine();
                        Product toAdd = storeManager.getProduct(addId);
                        if (toAdd == null) throw new ShoppingException("Product not found.");
                        if (!toAdd.isAvailable()) throw new ShoppingException("Sorry, this product is SOLD OUT.");
                        System.out.print("Enter quantity: ");
                        int qty = Integer.parseInt(sc.nextLine());
                        customer.getCart().addProduct(toAdd, qty);
                        System.out.println("Added to cart.");
                        break;
                    case 3:
                        System.out.print("Enter Product ID to remove from cart: ");
                        String remId = sc.nextLine();
                        customer.getCart().removeProduct(remId);
                        System.out.println("Removed from cart.");
                        break;
                    case 4:
                        customer.getCart().displayCart();
                        break;
                    case 5:
                        if (customer.getCart().isEmpty()) throw new ShoppingException("Cart is empty. Add items before checkout.");
                        double total = customer.getCart().getTotal();
                        double discount = (total > 100) ? total * 0.10 : 0;
                        double finalTotal = total - discount;
                        System.out.printf("Subtotal: $%.2f\n", total);
                        if (discount > 0)
                            System.out.printf("Discount: -$%.2f\n", discount);
                        System.out.printf("Total after discount: $%.2f\n", finalTotal);

                        List<PaymentRecord> payments = new ArrayList<>();
                        double remaining = finalTotal;
                        System.out.println("\nAvailable Payment Methods:");
                        customer.displayPaymentMethods();
                        while (remaining > 0) {
                            System.out.print("Select payment method (index) or 0 to cancel: ");
                            int payIndex = Integer.parseInt(sc.nextLine());
                            if (payIndex == 0) throw new ShoppingException("Checkout cancelled.");
                            if (payIndex < 1 || payIndex > customer.getPaymentMethods().size())
                                throw new ShoppingException("Invalid payment method index.");
                            PaymentMethod payment = customer.getPaymentMethods().get(payIndex - 1);
                            System.out.printf("Selected %s (Balance: %s)\n", payment.getDetails(), payment.getBalance());
                            System.out.printf("Enter amount to pay (max $%.2f): ", remaining);
                            double amount = Double.parseDouble(sc.nextLine());
                            if (amount <= 0 || amount > remaining)
                                throw new ShoppingException("Invalid payment amount.");
                            try {
                                payment.pay(amount);
                                payments.add(new PaymentRecord(payment, amount));
                                remaining -= amount;
                            } catch (ShoppingException e) {
                                System.out.println("Payment failed: " + e.getMessage());
                            }
                            if (remaining > 0)
                                System.out.printf("Remaining balance to pay: $%.2f\n", remaining);
                        }
                        // Display final balances
                        System.out.println("\nPayment Method Balances After Checkout:");
                        customer.displayPaymentMethods();
                        // Decrease stock
                        for (CartItem ci : customer.getCart().getItems()) {
                            ci.getProduct().decreaseStock(ci.getQuantity());
                            if (ci.getProduct().getStock() == 0)
                                System.out.printf("%s is now SOLD OUT!\n", ci.getProduct().getName());
                        }
                        // Save order history
                        customer.addOrder(new Order(customer.getCart().getItems(), finalTotal, payments));
                        customer.getCart().clear();
                        System.out.println("Order placed! Thank you, " + customer.getName());
                        break;
                    case 6: // Admin Login
                        if (adminSession.isLoggedIn()) {
                            System.out.println("Already logged in as admin.");
                            break;
                        }
                        System.out.print("Admin username: ");
                        String uname = sc.nextLine();
                        System.out.print("Admin password: ");
                        String upass = sc.nextLine();
                        adminSession.login(uname, upass);
                        break;
                    case 7: // Add Product (Admin Only)
                        if (!adminSession.isLoggedIn()) throw new ShoppingException("Admin privileges required. Please login as admin first.");
                        System.out.print("Enter type (1=Electronics, 2=Clothing): ");
                        int type = Integer.parseInt(sc.nextLine());
                        System.out.print("Enter Product ID: ");
                        String pid = sc.nextLine();
                        if (!storeManager.isProductIdUnique(pid)) throw new ShoppingException("Product ID already exists.");
                        System.out.print("Enter Name: ");
                        String pname = sc.nextLine();
                        System.out.print("Enter Price: ");
                        double pprice = Double.parseDouble(sc.nextLine());
                        System.out.print("Enter Stock Quantity: ");
                        int pstock = Integer.parseInt(sc.nextLine());
                        if (type == 1) {
                            System.out.print("Enter Brand: ");
                            String brand = sc.nextLine();
                            storeManager.addProduct(new Electronics(pid, pname, pprice, pstock, brand));
                        } else {
                            System.out.print("Enter Size: ");
                            String size = sc.nextLine();
                            storeManager.addProduct(new Clothing(pid, pname, pprice, pstock, size));
                        }
                        System.out.println("Product added.");
                        break;
                    case 8: // Admin Logout
                        if (adminSession.isLoggedIn()) adminSession.logout();
                        else System.out.println("Not logged in as admin.");
                        break;
                    case 9:
                        customer.showOrderHistory();
                        break;
                    case 10: // Manage Payment Methods
                        System.out.println("\nPayment Method Management:");
                        System.out.println("1. View Payment Methods and Balances");
                        System.out.println("2. Add Payment Method");
                        System.out.println("3. Remove Payment Method");
                        System.out.println("4. Add Funds to Payment Method");
                        System.out.println("5. Back");
                        System.out.print("Choose an option: ");
                        int pmChoice = Integer.parseInt(sc.nextLine());
                        switch (pmChoice) {
                            case 1:
                                customer.displayPaymentMethods();
                                break;
                            case 2:
                                System.out.print("Enter type (1=Credit Card, 2=PayPal): ");
                                int pmType = Integer.parseInt(sc.nextLine());
                                if (pmType == 1) {
                                    System.out.print("Enter 16-digit card number: ");
                                    String cardNumber = sc.nextLine();
                                    System.out.print("Enter available balance: ");
                                    double balance = Double.parseDouble(sc.nextLine());
                                    customer.addPaymentMethod(new CreditCard(cardNumber, balance));
                                    System.out.println("Credit Card added.");
                                } else if (pmType == 2) {
                                    System.out.print("Enter PayPal email: ");
                                    String email = sc.nextLine();
                                    System.out.print("Enter available balance: ");
                                    double balance = Double.parseDouble(sc.nextLine());
                                    customer.addPaymentMethod(new PayPal(email, balance));
                                    System.out.println("PayPal added.");
                                } else {
                                    throw new ShoppingException("Invalid payment method type.");
                                }
                                break;
                            case 3:
                                customer.displayPaymentMethods();
                                System.out.print("Enter index to remove: ");
                                int index = Integer.parseInt(sc.nextLine());
                                customer.removePaymentMethod(index - 1);
                                System.out.println("Payment method removed.");
                                break;
                            case 4:
                                customer.displayPaymentMethods();
                                System.out.print("Select payment method to add funds (index) or 0 to cancel: ");
                                int fundIndex = Integer.parseInt(sc.nextLine());
                                if (fundIndex == 0) break;
                                if (fundIndex < 1 || fundIndex > customer.getPaymentMethods().size())
                                    throw new ShoppingException("Invalid payment method index.");
                                PaymentMethod pm = customer.getPaymentMethods().get(fundIndex - 1);
                                System.out.print("Enter amount to add: ");
                                double addAmount = Double.parseDouble(sc.nextLine());
                                if (pm instanceof CreditCard) {
                                    ((CreditCard) pm).addFunds(addAmount);
                                } else if (pm instanceof PayPal) {
                                    ((PayPal) pm).addFunds(addAmount);
                                } else {
                                    throw new ShoppingException("Unsupported payment method for adding funds.");
                                }
                                break;
                            case 5:
                                break;
                            default:
                                System.out.println("Invalid choice.");
                        }
                        break;
                    case 11:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (ShoppingException | NumberFormatException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("======================================");
        System.out.println(" Thank you for shopping with us!");
        System.out.println("======================================");
        sc.close();
    }
}