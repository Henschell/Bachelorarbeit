package Evaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein DTO, das die Ergebnisse einer Evaluierung speichert, einschließlich Fehler, Warnungen, Status und Gewichtung.
 */
public class EvaluationResult {
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private boolean passed = true;
    private double weight = 0.0;
    private String successFeedback; // Neues Feld für den Erfolgs-Feedback-Text

    public void setSuccessFeedback(String successFeedback) {
        this.successFeedback = successFeedback;
    }

    public void addError(String error) {
        errors.add(error);
        passed = false; // Fehler setzen den Status auf "failed"
    }

    public void addWarning(String warning) {
        warnings.add(warning);
        passed = false; // Warnungen setzen den Status ebenfalls auf "failed"
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public double getScore() {
        return isPassed() ? getWeight() : 0.0; // Score ist weight, wenn passed, sonst 0
    }

    public String getFeedbackText() {
        if (isPassed() && errors.isEmpty() && warnings.isEmpty()) {
            return successFeedback != null ? successFeedback : "Alle Bedingungen erfüllt.";
        } else if (!errors.isEmpty()) {
            return "Fehler:\n" + String.join("\n", errors);
        } else if (!warnings.isEmpty()) {
            return "Warnungen:\n" + String.join("\n", warnings);
        }
        return "Unbekannter Status.";
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!errors.isEmpty()) {
            sb.append("Fehler:\n");
            for (String error : errors) {
                sb.append("- ").append(error).append("\n");
            }
        }
        if (!warnings.isEmpty()) {
            sb.append("Warnungen:\n");
            for (String warning : warnings) {
                sb.append("- ").append(warning).append("\n");
            }
        }
        return sb.toString().trim();
    }
}