package Evaluator;

import Evaluator.EvaluationResult;
import org.eclipse.uml2.uml.Class;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import proformA.dto.CriterionWithWeight;
import umlParser.UmlModelParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UmlGeneralCriteriaEvaluatorTest {

    private static UmlGeneralCriteriaEvaluator evaluator;
    private static List<Class> umlClasses;
    private static Set<String> classNames;

    // Initialisierungsmethode, die vor allen Tests ausgeführt wird
    @BeforeAll
    public static void setUp() throws Exception {
        UmlGeneralCriteriaEvaluatorTest test = new UmlGeneralCriteriaEvaluatorTest();
        test.initialize("models/U09PapyrusCorrect.xmi"); // Verwende die Musterlösung als Referenz
    }

    // Initialisierungsmethode, die auch manuell aufgerufen werden kann
    public void initialize(String xmiFilePath) throws Exception {
        evaluator = new UmlGeneralCriteriaEvaluator();

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

    // Methode, um alle Kriterien manuell auszuführen und die Ergebnisse zurückzugeben
    public Map<String, EvaluationResult> runTests(List<CriterionWithWeight> criteriaWithWeights) {
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

    @Test
    public EvaluationResult testMinimumOneClass(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight); // Gewichtung setzen
        result.setSuccessFeedback("Das Modell enthält mindestens eine Klasse."); // Feedback-Text aus XML
        evaluator.checkMinimumOneClass(umlClasses, result);
        return result;
    }

    @Test
    public EvaluationResult testClassNameConvention(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight); // Gewichtung setzen
        result.setSuccessFeedback("Alle Klassennamen beginnen korrekt mit Großbuchstaben."); // Feedback-Text aus XML
        for (Class umlClass : umlClasses) {
            evaluator.checkClassNameConvention(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testAttributeNameConvention(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight); // Gewichtung setzen
        result.setSuccessFeedback("Alle Attributnamen beginnen korrekt mit Kleinbuchstaben."); // Feedback-Text aus XML
        for (Class umlClass : umlClasses) {
            evaluator.checkAttributeNameConvention(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testAssociationConsistency(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight); // Gewichtung setzen
        result.setSuccessFeedback("Alle Assoziationen sind konsistent."); // Feedback-Text aus XML
        for (Class umlClass : umlClasses) {
            evaluator.checkAssociationConsistency(umlClass, classNames, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testAttributesArePrivate(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight); // Gewichtung setzen
        result.setSuccessFeedback("Alle Attribute sind privat."); // Feedback-Text aus XML
        for (Class umlClass : umlClasses) {
            evaluator.checkAttributesArePrivate(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testGettersForPrivateAttributes(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight); // Gewichtung setzen
        result.setSuccessFeedback("Alle privaten Attribute haben Getter."); // Feedback-Text aus XML
        for (Class umlClass : umlClasses) {
            evaluator.checkGettersForPrivateAttributes(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testSettersForPrivateAttributes(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight); // Gewichtung setzen
        result.setSuccessFeedback("Alle privaten Attribute haben Setter."); // Feedback-Text aus XML
        for (Class umlClass : umlClasses) {
            evaluator.checkSettersForPrivateAttributes(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testMethodNameConvention(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight); // Gewichtung setzen
        result.setSuccessFeedback("Alle Methodennamen entsprechen den Konventionen."); // Feedback-Text aus XML
        for (Class umlClass : umlClasses) {
            evaluator.checkMethodNameConvention(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testConstructorPresenceAndConvention(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight); // Gewichtung setzen
        result.setSuccessFeedback("Alle Klassen haben einen korrekten Konstruktor."); // Feedback-Text aus XML
        for (Class umlClass : umlClasses) {
            evaluator.checkConstructorPresenceAndConvention(umlClass, result);
        }
        return result;
    }

    @Test
    public EvaluationResult testMinimumAttributesOrMethods(double weight) {
        EvaluationResult result = new EvaluationResult();
        result.setWeight(weight); // Gewichtung setzen
        result.setSuccessFeedback("Alle Klassen haben mindestens ein Attribut oder eine Methode."); // Feedback-Text aus XML
        for (Class umlClass : umlClasses) {
            evaluator.checkMinimumAttributesOrMethods(umlClass, result);
        }
        return result;
    }
}