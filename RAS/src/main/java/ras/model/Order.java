package ras.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private String billNo;
    private String orderDate; // yyyy-MM-dd HH:mm:ss
    private double totalAmount;
    private String status; // PENDING, BILLED, CANCELLED
    private int clerkId;
    private List<OrderItem> items;

    public Order(int id, String billNo, String orderDate, double totalAmount, String status, int clerkId) {
        this.id = id;
        this.billNo = billNo;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.clerkId = clerkId;
        this.items = new ArrayList<>();
    }

    public void addItem(OrderItem item) { items.add(item); }
    public void recalculateTotal() {
        totalAmount = items.stream().mapToDouble(OrderItem::getLineTotal).sum();
    }

    public int getId() { return id; }
    public String getBillNo() { return billNo; }
    public String getOrderDate() { return orderDate; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public int getClerkId() { return clerkId; }
    public List<OrderItem> getItems() { return items; }

    public void setId(int id) { this.id = id; }
    public void setBillNo(String billNo) { this.billNo = billNo; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setClerkId(int clerkId) { this.clerkId = clerkId; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
