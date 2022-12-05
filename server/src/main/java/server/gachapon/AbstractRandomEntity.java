package server.gachapon;

public class AbstractRandomEntity {

    private int idreward;

    private final int id;

    private final int quantity;

    /**
     * 100000 = 100%
     * 10000 = 10 %
     * 1000 = 1 %
     * 500  = 0.5 %
     * 50   = 0.05 %
     */
    private final double chance;

    private final String description;

    public AbstractRandomEntity(int idreward, int id, int quantity, double chance, String description) {
        this.id = id;
        this.chance = chance;
        this.quantity = quantity;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getChance() {
        return chance;
    }

    public String getDescription() {
        return description;
    }

    public int getIdreward() {
        return idreward;
    }

    @Override
    public String toString() {
        return "AbstractRandomEntity [id=" + id + ", quantity=" + quantity + ", chance=" + chance + ", description="
                + description + "]";
    }


}
