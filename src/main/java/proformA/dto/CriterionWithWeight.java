package proformA.dto;

public class CriterionWithWeight {
    private final String criterion;
    private final double weight;
    private final String refId; // Neues Feld für refId

    public CriterionWithWeight(String criterion, double weight, String refId) {
        this.criterion = criterion;
        this.weight = weight;
        this.refId = refId;
    }

    public String getCriterion() { return criterion; }
    public double getWeight() { return weight; }
    public String getRefId() { return refId; } // Neue Methode
}