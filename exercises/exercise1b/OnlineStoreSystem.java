import java.util.*;

public class OnlineStoreSystem {

    // Given data
    private static final String[] orderData = {
            "John,Laptop,1,899.99", "Sarah,Mouse,2,25.50", "Mike,Keyboard,1,75.00",
            "John,Monitor,1,299.99", "Sarah,Laptop,1,899.99", "Lisa,Mouse,3,25.50",
            "Mike,Headphones,1,150.00", "John,Mouse,1,25.50", "Lisa,Keyboard,2,75.00",
            "Sarah,Monitor,2,299.99", "Mike,Monitor,1,299.99", "Lisa,Headphones,1,150.00"
    };

    // Simple order model (keeps parsing clean)
    static class Order {
        String customer;
        String product;
        int quantity;
        double price;

        Order(String raw) {
            String[] parts = raw.split(",");
            this.customer = parts[0];
            this.product = parts[1];
            this.quantity = Integer.parseInt(parts[2]);
            this.price = Double.parseDouble(parts[3]);
        }

        double totalValue() {
            return quantity * price;
        }

        @Override
        public String toString() {
            return customer + "," + product + "," + quantity + "," + String.format("%.2f", price);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== ONLINE STORE ORDER PROCESSING SYSTEM ===\n");

        ArrayList<Order> orders = step1_arrayListStoreAllOrders();
        step2_hashSetUniqueCustomers(orders);
        step3_treeSetSortedProducts(orders);
        step4_hashMapTotals(orders);
        step5_queueProcessBigOrders(orders);
        step6_stackHandleReturns();
    }

    // STEP 1: ArrayList - Store All Orders
    public static ArrayList<Order> step1_arrayListStoreAllOrders() {
        System.out.println("STEP 1: Managing orders with ArrayList");

        ArrayList<Order> orders = new ArrayList<>();
        for (String raw : orderData) {
            orders.add(new Order(raw));
        }

        System.out.println("Total orders: " + orders.size());
        System.out.println("First 3 orders:");
        for (int i = 0; i < Math.min(3, orders.size()); i++) {
            System.out.println("  " + orders.get(i)); // prints raw-style order line
        }
        System.out.println();
        return orders;
    }

    // STEP 2: HashSet - Find Unique Customers
    public static void step2_hashSetUniqueCustomers(List<Order> orders) {
        System.out.println("STEP 2: Finding customers with HashSet");

        HashSet<String> customers = new HashSet<>();
        for (Order o : orders) customers.add(o.customer);

        System.out.println("Unique customers: " + customers);
        System.out.println("Total customers: " + customers.size());
        System.out.println();
    }

    // STEP 3: TreeSet - Sort Products
    public static void step3_treeSetSortedProducts(List<Order> orders) {
        System.out.println("STEP 3: Sorting products with TreeSet");

        TreeSet<String> products = new TreeSet<>();
        for (Order o : orders) products.add(o.product);

        System.out.println("Sorted products: " + products);
        System.out.println("Total products: " + products.size());
        System.out.println();
    }

    // STEP 4: HashMap - Calculate Totals
    public static void step4_hashMapTotals(List<Order> orders) {
        System.out.println("STEP 4: Calculating totals with HashMap");

        HashMap<String, Double> totalSpentByCustomer = new HashMap<>();
        HashMap<String, Integer> totalQtyByProduct = new HashMap<>();

        for (Order o : orders) {
            // total spent per customer
            double orderTotal = o.totalValue();
            totalSpentByCustomer.put(o.customer,
                    totalSpentByCustomer.getOrDefault(o.customer, 0.0) + orderTotal);

            // total quantity sold per product
            totalQtyByProduct.put(o.product,
                    totalQtyByProduct.getOrDefault(o.product, 0) + o.quantity);
        }

        System.out.println("Total spent by each customer:");
        for (String customer : totalSpentByCustomer.keySet()) {
            System.out.printf("  %s: $%.2f%n", customer, totalSpentByCustomer.get(customer));
        }

        System.out.println("Total quantity sold per product:");
        for (String product : totalQtyByProduct.keySet()) {
            System.out.printf("  %s: %d%n", product, totalQtyByProduct.get(product));
        }

        System.out.println();
    }

    // STEP 5: Queue - Process Big Orders
    public static void step5_queueProcessBigOrders(List<Order> orders) {
        System.out.println("STEP 5: Processing big orders with Queue (>= $200)");

        Queue<Order> bigOrders = new LinkedList<>();
        for (Order o : orders) {
            if (o.totalValue() >= 200.0) {
                bigOrders.add(o);
            }
        }

        System.out.println("Big orders queued: " + bigOrders.size());
        while (!bigOrders.isEmpty()) {
            Order next = bigOrders.poll(); // FIFO
            System.out.printf("  Processing: %s (Total: $%.2f)%n", next, next.totalValue());
        }
        System.out.println();
    }

    // STEP 6: Stack - Handle Returns
    public static void step6_stackHandleReturns() {
        System.out.println("STEP 6: Handling returns with Stack (LIFO)");

        Stack<String> returns = new Stack<>();
        // sample returns (can be any format you want)
        returns.push("RETURN: Sarah,Mouse,1");
        returns.push("RETURN: John,Monitor,1");
        returns.push("RETURN: Lisa,Keyboard,1");

        System.out.println("Returns stacked: " + returns.size());
        while (!returns.isEmpty()) {
            String r = returns.pop(); // LIFO
            System.out.println("  Processing return: " + r);
        }
        System.out.println();
    }
}
