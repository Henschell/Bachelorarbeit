package Evaluator;

import Evaluator.EvaluationResult;
import info.debatty.java.stringsimilarity.Levenshtein;

import org.eclipse.uml2.uml.Class;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import umlParser.UmlModelParser;

import proformA.dto.CriterionWithWeight;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UmlCriteriaEvaluatorTest {

    private static UmlCriteriaEvaluator evaluator;
    private static List<Class> umlClasses;
    private static Set<String> classNames;

    // Initialisierungsmethode, die vor allen Tests ausgeführt wird
    @BeforeAll
    public static void setUp() throws Exception {
        UmlCriteriaEvaluatorTest test = new UmlCriteriaEvaluatorTest();
        test.initialize("models/U09PapyrusCorrect.xmi"); // Verwende die Musterlösung als Referenz
    }

    // Initialisierungsmethode, die auch manuell aufgerufen werden kann
    public void initialize(String xmiFilePath) throws Exception {
        evaluator = new UmlCriteriaEvaluator();

        if (xmiFilePath == null || xmiFilePath.isEmpty()) {
            xmiFilePath = "models/U09PapyrusCorrect.xmi"; // Fallback
        }

        UmlModelParser parser = new UmlModelParser();
        umlClasses = parser.parse(xmiFilePath);

        classNames = new HashSet<>();
        for (Class umlClass : umlClasses) {
            classNames.add(umlClass.getName());
        }
    }

    // Methode, um alle allgemeinen Tests manuell auszuführen und die Ergebnisse zurückzugeben
    public Map<String, EvaluationResult> runAllgemeineTests(List<CriterionWithWeight> criteriaWithWeights) {
        Map<String, EvaluationResult> results = new HashMap<>();

        // Prüfe nur die Kriterien, die in der Liste übergeben wurden
        for (CriterionWithWeight cw : criteriaWithWeights) {
            String criterion = cw.getCriterion();
            double weight = cw.getWeight(); // Gewichtung aus CriterionWithWeight holen
            EvaluationResult result = new EvaluationResult();
            result.setWeight(weight); // Gewichtung setzen

            try {
                switch (criterion) {
                    case "testMinimumOneClass":
                        result = testMinimumOneClass(weight);
                        break;
                    case "testClassNameConvention":
                        result = testClassNameConvention(weight);
                        break;
                    case "testAttributeNameConvention":
                        result = testAttributeNameConvention(weight);
                        break;
                    case "testAssociationConsistency":
                        result = testAssociationConsistency(weight);
                        break;
                    case "testAttributesArePrivate":
                        result = testAttributesArePrivate(weight);
                        break;
                    case "testGettersForPrivateAttributes":
                        result = testGettersForPrivateAttributes(weight);
                        break;
                    case "testSettersForPrivateAttributes":
                        result = testSettersForPrivateAttributes(weight);
                        break;
                    case "testMethodNameConvention":
                        result = testMethodNameConvention(weight);
                        break;
                    case "testConstructorPresenceAndConvention":
                        result = testConstructorPresenceAndConvention(weight);
                        break;
                    case "testMinimumAttributesOrMethods":
                        result = testMinimumAttributesOrMethods(weight);
                        break;
                    default:
                        result.addError("Unbekanntes Kriterium: " + criterion);
                        break;
                }
            } catch (Exception e) {
                // Wenn ein Test fehlschlägt, speichere das Ergebnis und fahre mit dem nächsten Test fort
                result.addError("Fehler bei der Ausführung: " + e.getMessage());
            }
            results.put(criterion, result);
        }

        return results;
    }

// // Methode für den Musterlösungsvergleich, nur ausgeführt, wenn das Kriterium enthalten ist
//    public Map<String, EvaluationResult> runModelComparisonTest(List<CriterionWithWeight> criteriaWithWeights, String submissionPath, String referenceModelPath) {
//        Map<String, EvaluationResult> results = new HashMap<>();
//
//        // Prüfe, ob das Kriterium "testModelComparison" in der Liste enthalten ist
//        for (CriterionWithWeight cw : criteriaWithWeights) {
//            if (cw.getCriterion().equals("testModelComparison")) {
//                EvaluationResult result = new EvaluationResult();
//                result.setWeight(cw.getWeight());
//                result.setSuccessFeedback("Das Modell entspricht der Musterlösung.");
//
//                try {
//                    System.out.println("Vergleich gestartet: Abgabe (" + submissionPath + ") vs. Musterlösung (" + referenceModelPath + ")");
//
//                    // Parse die Abgabe (submission)
//                    UmlModelParser submissionParser = new UmlModelParser();
//                    List<Class> submissionClasses = submissionParser.parse(submissionPath);
//                    if (submissionClasses == null || submissionClasses.isEmpty()) {
//                        result.addError("Abgabe konnte nicht geladen werden.");
//                        System.out.println("Fehler: Abgabe konnte nicht geladen werden.");
//                        results.put("testModelComparison", result);
//                        return results;
//                    }
//                    System.out.println("Abgabe geladen: " + submissionClasses.size() + " Klassen gefunden.");
//
//                    // Parse die Musterlösung (reference)
//                    UmlModelParser referenceParser = new UmlModelParser();
//                    List<Class> referenceClasses = referenceParser.parse(referenceModelPath);
//                    if (referenceClasses == null || referenceClasses.isEmpty()) {
//                        result.addError("Musterlösung konnte nicht geladen werden.");
//                        System.out.println("Fehler: Musterlösung konnte nicht geladen werden.");
//                        results.put("testModelComparison", result);
//                        return results;
//                    }
//                    System.out.println("Musterlösung geladen: " + referenceClasses.size() + " Klassen gefunden.");
//
//                    // Einfacher Vergleich: Anzahl der Klassen und Namen
//                    System.out.println("Vergleich der Klassenanzahl: Abgabe (" + submissionClasses.size() + ") vs. Musterlösung (" + referenceClasses.size() + ")");
//                    if (submissionClasses.size() != referenceClasses.size()) {
//                        result.addError("Anzahl der Klassen stimmt nicht überein: " + submissionClasses.size() + " vs. " + referenceClasses.size());
//                        System.out.println("Fehler: Anzahl der Klassen stimmt nicht überein.");
//                    } else {
//                        Set<String> referenceClassNames = new HashSet<>();
//                        for (Class refClass : referenceClasses) {
//                            referenceClassNames.add(refClass.getName());
//                        }
//                        System.out.println("Musterlösung Klassenamen: " + referenceClassNames);
//
//                        Set<String> submissionClassNames = new HashSet<>();
//                        for (Class subClass : submissionClasses) {
//                            submissionClassNames.add(subClass.getName());
//                        }
//                        System.out.println("Abgabe Klassenamen: " + submissionClassNames);
//
//                        if (!referenceClassNames.equals(submissionClassNames)) {
//                            result.addError("Klassenamen stimmen nicht überein: Erwartet " + referenceClassNames + ", gefunden " + submissionClassNames);
//                            System.out.println("Fehler: Klassenamen stimmen nicht überein.");
//                        } else {
//                            System.out.println("Erfolg: Klassenanzahl und -namen stimmen überein.");
//                            // Kein addError, daher bleibt passed = true
//                        }
//                    }
//                } catch (Exception e) {
//                    result.addError("Fehler bei der Ausführung des Musterlösungsvergleichs: " + e.getMessage());
//                    System.out.println("Fehler: " + e.getMessage());
//                }
//                results.put("testModelComparison", result);
//                break; // Nur einmal ausführen, wenn das Kriterium vorhanden ist
//            }
//        }
//
//        return results;
//    }

    @Test
    public EvaluationResult testMinimumOneClass(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight);
        result.setSuccessFeedback("Das Modell enthält mindestens eine Klasse.");
        evaluator.checkMinimumOneClass(umlClasses, result);
        return result;
    }

    @Test
    public EvaluationResult testClassNameConvention(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight);
        result.setSuccessFeedback("Alle Klassennamen beginnen korrekt mit Großbuchstaben.");
        for (Class umlClass : umlClasses) {
            evaluator.checkClassNameConvention(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testAttributeNameConvention(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight);
        result.setSuccessFeedback("Alle Attributnamen beginnen korrekt mit Kleinbuchstaben.");
        for (Class umlClass : umlClasses) {
            evaluator.checkAttributeNameConvention(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testAssociationConsistency(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight);
        result.setSuccessFeedback("Alle Assoziationen sind konsistent.");
        for (Class umlClass : umlClasses) {
            evaluator.checkAssociationConsistency(umlClass, classNames, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testAttributesArePrivate(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight);
        result.setSuccessFeedback("Alle Attribute sind privat.");
        for (Class umlClass : umlClasses) {
            evaluator.checkAttributesArePrivate(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testGettersForPrivateAttributes(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight);
        result.setSuccessFeedback("Alle privaten Attribute haben Getter.");
        for (Class umlClass : umlClasses) {
            evaluator.checkGettersForPrivateAttributes(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testSettersForPrivateAttributes(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight);
        result.setSuccessFeedback("Alle privaten Attribute haben Setter.");
        for (Class umlClass : umlClasses) {
            evaluator.checkSettersForPrivateAttributes(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testMethodNameConvention(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight);
        result.setSuccessFeedback("Alle Methodennamen entsprechen den Konventionen.");
        for (Class umlClass : umlClasses) {
            evaluator.checkMethodNameConvention(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testConstructorPresenceAndConvention(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight);
        result.setSuccessFeedback("Alle Klassen haben einen korrekten Konstruktor.");
        for (Class umlClass : umlClasses) {
            evaluator.checkConstructorPresenceAndConvention(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testMinimumAttributesOrMethods(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight);
        result.setSuccessFeedback("Alle Klassen haben mindestens ein Attribut oder eine Methode.");
        for (Class umlClass : umlClasses) {
            evaluator.checkMinimumAttributesOrMethods(umlClass, result);
        }
        return result;
    }
    public Map<String, EvaluationResult> runModelComparisonTest(List<CriterionWithWeight> criteriaWithWeights, String submissionPath, String referenceModelPath) {
        Map<String, EvaluationResult> results = new HashMap<>();
        for (CriterionWithWeight cw : criteriaWithWeights) {
            if (cw.getCriterion().equals("testModelComparison")) {
                EvaluationResult result = new EvaluationResult();
                result.setWeight(cw.getWeight());
                result.setSuccessFeedback("Das Modell entspricht der Musterlösung.");

                try {
                    System.out.println("Vergleich gestartet: Abgabe (" + submissionPath + ") vs. Musterlösung (" + referenceModelPath + ")");
                    UmlModelParser submissionParser = new UmlModelParser();
                    List<Class> submissionClasses = submissionParser.parse(submissionPath);
                    if (submissionClasses == null || submissionClasses.isEmpty()) {
                        result.addError("Abgabe konnte nicht geladen werden.");
                        System.out.println("Fehler: Abgabe konnte nicht geladen werden.");
                        results.put("testModelComparison", result);
                        return results;
                    }
                    System.out.println("Abgabe geladen: " + submissionClasses.size() + " Klassen gefunden.");

                    UmlModelParser referenceParser = new UmlModelParser();
                    List<Class> referenceClasses = referenceParser.parse(referenceModelPath);
                    if (referenceClasses == null || referenceClasses.isEmpty()) {
                        result.addError("Musterlösung konnte nicht geladen werden.");
                        System.out.println("Fehler: Musterlösung konnte nicht geladen werden.");
                        results.put("testModelComparison", result);
                        return results;
                    }
                    System.out.println("Musterlösung geladen: " + referenceClasses.size() + " Klassen gefunden.");

                    // Prüfe die Anzahl der Klassen
                    if (submissionClasses.size() != referenceClasses.size()) {
                        result.addError("Anzahl der Klassen stimmt nicht überein: " + submissionClasses.size() + " vs. " + referenceClasses.size());
                        System.out.println("Fehler: Anzahl der Klassen stimmt nicht überein.");
                    } else {
                        Levenshtein levenshtein = new Levenshtein();
                        Set<String> referenceClassNames = new HashSet<>();
                        Map<String, Class> referenceClassMap = new HashMap<>();
                        for (Class refClass : referenceClasses) {
                            referenceClassNames.add(refClass.getName());
                            referenceClassMap.put(refClass.getName(), refClass);
                        }
                        System.out.println("Musterlösung Klassenamen: " + referenceClassNames);

                        Set<String> submissionClassNames = new HashSet<>();
                        Map<String, Class> submissionClassMap = new HashMap<>();
                        for (Class subClass : submissionClasses) {
                            submissionClassNames.add(subClass.getName());
                            submissionClassMap.put(subClass.getName(), subClass);
                        }
                        System.out.println("Abgabe Klassenamen: " + submissionClassNames);

                        boolean allMatch = true;
                        StringBuilder mismatches = new StringBuilder();
                        for (Class subClass : submissionClasses) {
                            String bestMatch = null;
                            double minDistance = Double.MAX_VALUE;
                            for (String refName : referenceClassNames) {
                                double distance = levenshtein.distance(subClass.getName(), refName);
                                if (distance < minDistance) {
                                    minDistance = distance;
                                    bestMatch = refName;
                                }
                            }
                            if (bestMatch != null && minDistance < 3) { // Schwellenwert für Ähnlichkeit
                                Class refClass = referenceClassMap.get(bestMatch);
                                // Hier könnte ein detaillierter 1:1-Vergleich der Klassen (Attribute, Methoden, etc.) hinzugefügt werden
                                if (!subClass.getName().equals(refClass.getName())) {
                                    allMatch = false;
                                    mismatches.append("Klasse ").append(subClass.getName()).append(" ähnelt ").append(bestMatch)
                                            .append(" (Distanz: ").append(minDistance).append("), aber Namen unterschiedlich.\n");
                                }
                            } else {
                                allMatch = false;
                                mismatches.append("Keine ähnliche Klasse für ").append(subClass.getName()).append(" gefunden.\n");
                            }
                        }

                        if (!allMatch) {
                            result.addError("Klassen stimmen nicht überein: " + mismatches.toString());
                            System.out.println("Fehler: Klassen stimmen nicht überein.");
                        } else {
                            System.out.println("Erfolg: Klassenanzahl und -namen (mit Ähnlichkeitsprüfung) stimmen überein.");
                        }
                    }
                } catch (Exception e) {
                    result.addError("Fehler bei der Ausführung des Musterlösungsvergleichs: " + e.getMessage());
                    System.out.println("Fehler: " + e.getMessage());
                }
                results.put("testModelComparison", result);
                break;
            }
        }

        return results;
    }
}