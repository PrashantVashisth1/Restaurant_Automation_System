package ras.model;

public class Invoice {
    private int id;
    private int purchaseOrderId;
    private int ingredientId;
    private String ingredientName;
    private String supplierName;
    private double quantityReceived;
    private double unitPrice;
    private double totalAmount;
    private String invoiceDate;
    private boolean chequePrinted;
    private String chequeNumber;
    private String status; // PAID, PENDING

    public Invoice(int id, int purchaseOrderId, int ingredientId, String ingredientName,
                   String supplierName, double quantityReceived, double unitPrice,
                   String invoiceDate, boolean chequePrinted, String chequeNumber, String status) {
        this.id = id;
        this.purchaseOrderId = purchaseOrderId;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.supplierName = supplierName;
        this.quantityReceived = quantityReceived;
        this.unitPrice = unitPrice;
        this.totalAmount = quantityReceived * unitPrice;
        this.invoiceDate = invoiceDate;
        this.chequePrinted = chequePrinted;
        this.chequeNumber = chequeNumber;
        this.status = status;
    }

    public int getId() { return id; }
    public int getPurchaseOrderId() { return purchaseOrderId; }
    public int getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public String getSupplierName() { return supplierName; }
    public double getQuantityReceived() { return quantityReceived; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalAmount() { return totalAmount; }
    public String getInvoiceDate() { return invoiceDate; }
    public boolean isChequePrinted() { return chequePrinted; }
    public String getChequeNumber() { return chequeNumber; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setPurchaseOrderId(int purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public void setQuantityReceived(double quantityReceived) { this.quantityReceived = quantityReceived; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }
    public void setChequePrinted(boolean chequePrinted) { this.chequePrinted = chequePrinted; }
    public void setChequeNumber(String chequeNumber) { this.chequeNumber = chequeNumber; }
    public void setStatus(String status) { this.status = status; }
}
