package ras.model;

public class Ingredient {
    private int id;
    private String code;
    private String name;
    private String unit;
    private double currentStock;
    private double thresholdValue;

    public Ingredient(int id, String code, String name, String unit, double currentStock, double thresholdValue) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.currentStock = currentStock;
        this.thresholdValue = thresholdValue;
    }

    public boolean isBelowThreshold() { return currentStock < thresholdValue; }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public double getCurrentStock() { return currentStock; }
    public double getThresholdValue() { return thresholdValue; }

    public void setId(int id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setCurrentStock(double currentStock) { this.currentStock = currentStock; }
    public void setThresholdValue(double thresholdValue) { this.thresholdValue = thresholdValue; }

    @Override
    public String toString() { return "[" + code + "] " + name + " (" + unit + ")"; }
}
