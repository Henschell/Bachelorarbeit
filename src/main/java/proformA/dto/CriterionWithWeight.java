package proformA.dto;

public class CriterionWithWeight {
    private String criterion;
    private double weight;

    public CriterionWithWeight(String criterion, double weight) {
        this.criterion = criterion;
        this.weight = weight;
    }

    public String getCriterion() {
        return criterion;
    }

    public double getWeight() {
        return weight;
    }
}