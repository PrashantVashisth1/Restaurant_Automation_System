package ras.model;

public class IngredientUsage {
    private int id;
    private int ingredientId;
    private String ingredientName;
    private int menuItemId;
    private String menuItemName;
    private double quantityUsed;
    private String usageDate; // yyyy-MM-dd

    public IngredientUsage(int id, int ingredientId, String ingredientName,
                           int menuItemId, String menuItemName,
                           double quantityUsed, String usageDate) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.quantityUsed = quantityUsed;
        this.usageDate = usageDate;
    }

    public int getId() { return id; }
    public int getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public int getMenuItemId() { return menuItemId; }
    public String getMenuItemName() { return menuItemName; }
    public double getQuantityUsed() { return quantityUsed; }
    public String getUsageDate() { return usageDate; }

    public void setId(int id) { this.id = id; }
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
    public void setMenuItemId(int menuItemId) { this.menuItemId = menuItemId; }
    public void setMenuItemName(String menuItemName) { this.menuItemName = menuItemName; }
    public void setQuantityUsed(double quantityUsed) { this.quantityUsed = quantityUsed; }
    public void setUsageDate(String usageDate) { this.usageDate = usageDate; }
}
