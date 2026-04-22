package ras.model;

public class OrderItem {
    private int id;
    private int orderId;
    private int menuItemId;
    private String menuItemCode;
    private String menuItemName;
    private int quantity;
    private double unitPrice;

    public OrderItem(int id, int orderId, int menuItemId, String menuItemCode,
                     String menuItemName, int quantity, double unitPrice) {
        this.id = id;
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.menuItemCode = menuItemCode;
        this.menuItemName = menuItemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public double getLineTotal() { return quantity * unitPrice; }

    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public int getMenuItemId() { return menuItemId; }
    public String getMenuItemCode() { return menuItemCode; }
    public String getMenuItemName() { return menuItemName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }

    public void setId(int id) { this.id = id; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setMenuItemId(int menuItemId) { this.menuItemId = menuItemId; }
    public void setMenuItemCode(String menuItemCode) { this.menuItemCode = menuItemCode; }
    public void setMenuItemName(String menuItemName) { this.menuItemName = menuItemName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}
