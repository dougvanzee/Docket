package docket.reports;

public class AppointmentType {
    private String name;
    private int quantity;

    public AppointmentType(String name) {
        this.name = name;
        quantity = 1;
    }

    public String getName() { return name; }

    public int getQuantity() { return quantity; }

    public void increment() { quantity++; }
}
