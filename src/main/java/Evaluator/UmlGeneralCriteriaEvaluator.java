package Evaluator;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.VisibilityKind;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Die Klasse {@code UmlGeneralCriteriaEvaluator} prüft ein UML-Modell anhand allgemeiner Kriterien.
 */
public class UmlGeneralCriteriaEvaluator {
    // Klasse für Bewertungshinweise (Fehler und Warnungen)
    public static class EvaluationResult {
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
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
            return sb.toString();
        }
    }

    /**
     * Prüft, ob das Modell mindestens eine Klasse enthält.
     */
    public boolean checkMinimumOneClass(List<Class> umlClasses, EvaluationResult result) {
        if (umlClasses.isEmpty()) {
            result.addError("Modell enthält keine Klassen.");
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob Klassennamen mit Großbuchstaben beginnen.
     */
    public boolean checkClassNameConvention(Class umlClass, EvaluationResult result) {
        String className = umlClass.getName();
        if (!Character.isUpperCase(className.charAt(0))) {
            result.addWarning("Klasse " + className + " sollte mit einem Großbuchstaben beginnen.");
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob Attributnamen mit Kleinbuchstaben beginnen.
     */
    public boolean checkAttributeNameConvention(Class umlClass, EvaluationResult result) {
        String className = umlClass.getName();
        for (Property attribute : umlClass.getOwnedAttributes()) {
            String attrName = attribute.getName();
            if (attrName != null && !attrName.isEmpty() && !Character.isLowerCase(attrName.charAt(0))) {
                result.addWarning("Attribut " + attrName + " in Klasse " + className + " sollte mit einem Kleinbuchstaben beginnen.");
                return false;
            }
        }
        return true;
    }

    /**
     * Prüft die Konsistenz von Assoziationen (ob der Typ existiert).
     */
    public boolean checkAssociationConsistency(Class umlClass, Set<String> classNames, EvaluationResult result) {
        String className = umlClass.getName();
        for (Property attribute : umlClass.getOwnedAttributes()) {
            if (attribute.getAssociation() != null) {
                String typeName = attribute.getType().getName();
                if (!classNames.contains(typeName)) {
                    result.addError("Assoziation in Klasse " + className + " verweist auf nicht existierende Klasse " + typeName + ".");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Prüft, ob alle Attribute privat sind.
     */
    public boolean checkAttributesArePrivate(Class umlClass, EvaluationResult result) {
        String className = umlClass.getName();
        for (Property attribute : umlClass.getOwnedAttributes()) {
            if (attribute.getVisibility() != VisibilityKind.PRIVATE_LITERAL) {
                result.addWarning("Attribut " + attribute.getName() + " in Klasse " + className + " ist nicht privat.");
                return false;
            }
        }
        return true;
    }

    /**
     * Prüft, ob private Attribute Getter haben.
     */
    public boolean checkGettersForPrivateAttributes(Class umlClass, EvaluationResult result) {
        String className = umlClass.getName();
        boolean allHaveGetters = true;
        for (Property attribute : umlClass.getOwnedAttributes()) {
            if (attribute.getVisibility() == VisibilityKind.PRIVATE_LITERAL) {
                String attrName = attribute.getName();
                // Ignoriere Attribute ohne Namen
                if (attrName == null || attrName.isEmpty()) {
                    continue;
                }
                String getterName = "get" + capitalize(attrName);
                boolean hasGetter = false;
                for (Operation operation : umlClass.getOwnedOperations()) {
                    if (operation.getName() != null && operation.getName().equals(getterName)) {
                        hasGetter = true;
                        break;
                    }
                }
                if (!hasGetter) {
                    result.addWarning("Privates Attribut " + attrName + " in Klasse " + className + " hat keinen Getter.");
                    allHaveGetters = false;
                }
            }
        }
        return allHaveGetters;
    }

    /**
     * Prüft, ob private Attribute Setter haben.
     */
    public boolean checkSettersForPrivateAttributes(Class umlClass, EvaluationResult result) {
        String className = umlClass.getName();
        boolean allHaveSetters = true;
        for (Property attribute : umlClass.getOwnedAttributes()) {
            if (attribute.getVisibility() == VisibilityKind.PRIVATE_LITERAL) {
                String attrName = attribute.getName();
                // Ignoriere Attribute ohne Namen
                if (attrName == null || attrName.isEmpty()) {
                    continue;
                }
                String setterName = "set" + capitalize(attrName);
                boolean hasSetter = false;
                for (Operation operation : umlClass.getOwnedOperations()) {
                    if (operation.getName() != null && operation.getName().equals(setterName)) {
                        hasSetter = true;
                        break;
                    }
                }
                if (!hasSetter) {
                    result.addWarning("Privates Attribut " + attrName + " in Klasse " + className + " hat keinen Setter.");
                    allHaveSetters = false;
                }
            }
        }
        return allHaveSetters;
    }

    /**
     * Prüft, ob Methodennamen (außer Konstruktoren) mit Kleinbuchstaben beginnen.
     */
    public boolean checkMethodNameConvention(Class umlClass, EvaluationResult result) {
        String className = umlClass.getName();
        for (Operation operation : umlClass.getOwnedOperations()) {
            String opName = operation.getName();
            // Überspringe Konstruktoren
            if (opName != null && opName.equals(className)) {
                continue;
            }
            if (!Character.isLowerCase(opName.charAt(0))) {
                result.addWarning("Methode " + opName + " in Klasse " + className + " sollte mit einem Kleinbuchstaben beginnen.");
                return false;
            }
        }
        return true;
    }

    /**
     * Prüft, ob ein Konstruktor vorhanden ist und ob sein Name mit Großbuchstaben beginnt.
     */
    public boolean checkConstructorPresenceAndConvention(Class umlClass, EvaluationResult result) {
        String className = umlClass.getName();
        boolean hasConstructor = false;
        for (Operation operation : umlClass.getOwnedOperations()) {
            if (operation.getName() != null && operation.getName().equals(className)) {
                hasConstructor = true;
                if (!Character.isUpperCase(operation.getName().charAt(0))) {
                    result.addWarning("Konstruktor " + operation.getName() + " in Klasse " + className + " sollte mit einem Großbuchstaben beginnen.");
                    return false;
                }
                break;
            }
        }
        if (!hasConstructor) {
            result.addWarning("Klasse " + className + " hat keinen Konstruktor.");
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob die Klasse mindestens ein Attribut oder eine Methode hat.
     */
    public boolean checkMinimumAttributesOrMethods(Class umlClass, EvaluationResult result) {
        String className = umlClass.getName();
        if (umlClass.getOwnedAttributes().isEmpty() && umlClass.getOwnedOperations().isEmpty()) {
            result.addWarning("Klasse " + className + " hat weder Attribute noch Methoden.");
            return false;
        }
        return true;
    }

    /**
     * Führt eine Bewertung des UML-Modells anhand der ausgewählten Kriterien durch.
     *
     * @param umlClasses Liste der UML-Klassen
     * @param selectedCriteria Liste der Kriterien, die geprüft werden sollen
     * @return Bewertungsergebnis mit Fehlern und Warnungen
     */
    public EvaluationResult evaluate(List<Class> umlClasses, List<String> selectedCriteria) {
        EvaluationResult result = new EvaluationResult();

        // Prüfe zuerst, ob es überhaupt Klassen gibt
        if (selectedCriteria.contains("checkMinimumOneClass")) {
            if (!checkMinimumOneClass(umlClasses, result)) {
                return result; // Wenn keine Klassen vorhanden sind, weitere Prüfungen überspringen
            }
        }

        // Liste der Klassennamen für Konsistenzprüfungen
        Set<String> classNames = new HashSet<>();
        for (Class umlClass : umlClasses) {
            classNames.add(umlClass.getName());
        }

        // Prüfe jedes Kriterium für jede Klasse
        for (Class umlClass : umlClasses) {
            if (selectedCriteria.contains("checkClassNameConvention")) {
                checkClassNameConvention(umlClass, result);
            }
            if (selectedCriteria.contains("checkAttributeNameConvention")) {
                checkAttributeNameConvention(umlClass, result);
            }
            if (selectedCriteria.contains("checkAssociationConsistency")) {
                checkAssociationConsistency(umlClass, classNames, result);
            }
            if (selectedCriteria.contains("checkAttributesArePrivate")) {
                checkAttributesArePrivate(umlClass, result);
            }
            if (selectedCriteria.contains("checkGettersForPrivateAttributes")) {
                checkGettersForPrivateAttributes(umlClass, result);
            }
            if (selectedCriteria.contains("checkSettersForPrivateAttributes")) {
                checkSettersForPrivateAttributes(umlClass, result);
            }
            if (selectedCriteria.contains("checkMethodNameConvention")) {
                checkMethodNameConvention(umlClass, result);
            }
            if (selectedCriteria.contains("checkConstructorPresenceAndConvention")) {
                checkConstructorPresenceAndConvention(umlClass, result);
            }
            if (selectedCriteria.contains("checkMinimumAttributesOrMethods")) {
                checkMinimumAttributesOrMethods(umlClass, result);
            }
        }

        return result;
    }

    /**
     * Führt eine Bewertung des UML-Modells anhand aller Kriterien durch (Fallback-Methode).
     *
     * @param umlClasses Liste der UML-Klassen
     * @return Bewertungsergebnis mit Fehlern und Warnungen
     */
    public EvaluationResult evaluate(List<Class> umlClasses) {
        List<String> allCriteria = List.of(
            "checkMinimumOneClass",
            "checkClassNameConvention",
            "checkAttributeNameConvention",
            "checkAssociationConsistency",
            "checkAttributesArePrivate",
            "checkGettersForPrivateAttributes",
            "checkSettersForPrivateAttributes",
            "checkMethodNameConvention",
            "checkConstructorPresenceAndConvention",
            "checkMinimumAttributesOrMethods"
        );
        return evaluate(umlClasses, allCriteria);
    }

    /**
     * Hilfsmethode zum Kapitalisieren eines Strings (z. B. "name" -> "Name").
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}