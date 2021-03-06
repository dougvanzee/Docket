package docket.db;

public class FirstLevelDivision {
    private int divisionId;
    private String name;
    private int countryId;

    public FirstLevelDivision(int divisionId, String name, int countryId) {
        this.divisionId = divisionId;
        this.name = name;
        this.countryId = countryId;
    }

    public int getDivisionId() { return divisionId; }

    public String getName() { return name; }

    public int getCountryId() { return countryId; }

    @Override
    public String toString() { return name; }
}
