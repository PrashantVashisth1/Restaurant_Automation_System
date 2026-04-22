package ras.model;

public class MenuItem {
    private int id;
    private String code;
    private String name;
    private String category;
    private double price;
    private boolean available;

    public MenuItem(int id, String code, String name, String category, double price, boolean available) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
        this.price = price;
        this.available = available;
    }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }

    public void setId(int id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() { return "[" + code + "] " + name + " - Rs." + String.format("%.2f", price); }
}
