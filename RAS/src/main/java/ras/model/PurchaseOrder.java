package ras.model;

public class PurchaseOrder {
    private int id;
    private int ingredientId;
    private String ingredientCode;
    private String ingredientName;
    private double currentStock;
    private double thresholdValue;
    private double quantityOrdered;
    private String orderDate;
    private String status; // PENDING, FINALIZED, CANCELLED

    public PurchaseOrder(int id, int ingredientId, String ingredientCode, String ingredientName,
                         double currentStock, double thresholdValue, double quantityOrdered,
                         String orderDate, String status) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.ingredientCode = ingredientCode;
        this.ingredientName = ingredientName;
        this.currentStock = currentStock;
        this.thresholdValue = thresholdValue;
        this.quantityOrdered = quantityOrdered;
        this.orderDate = orderDate;
        this.status = status;
    }

    public int getId() { return id; }
    public int getIngredientId() { return ingredientId; }
    public String getIngredientCode() { return ingredientCode; }
    public String getIngredientName() { return ingredientName; }
    public double getCurrentStock() { return currentStock; }
    public double getThresholdValue() { return thresholdValue; }
    public double getQuantityOrdered() { return quantityOrdered; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }
    public void setIngredientCode(String ingredientCode) { this.ingredientCode = ingredientCode; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
    public void setCurrentStock(double currentStock) { this.currentStock = currentStock; }
    public void setThresholdValue(double thresholdValue) { this.thresholdValue = thresholdValue; }
    public void setQuantityOrdered(double quantityOrdered) { this.quantityOrdered = quantityOrdered; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setStatus(String status) { this.status = status; }
}
