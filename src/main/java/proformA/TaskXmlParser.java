package proformA;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import proforma.xml21.*;
import org.eclipse.uml2.uml.Class;
import umlParser.UmlModelParser;
import Evaluator.UmlGeneralCriteriaEvaluator;
import Evaluator.UmlGeneralCriteriaEvaluator.EvaluationResult;

// Hilfsklasse zum Speichern von Kriterium und Gewichtung
class CriterionWithWeight {
    String criterion;
    double weight;

    public CriterionWithWeight(String criterion, double weight) {
        this.criterion = criterion;
        this.weight = weight;
    }
}

public class TaskXmlParser {

    // Mapping von task.xml-Kriterien zu UmlGeneralCriteriaEvaluator-Methoden
    private static final Map<String, String> CRITERIA_MAPPING = new HashMap<>();

    static {
        // Namenskonventionen
        CRITERIA_MAPPING.put("FieldNamingConventions", "checkAttributeNameConvention");
        CRITERIA_MAPPING.put("MethodNamingConventions", "checkMethodNameConvention");
        CRITERIA_MAPPING.put("ClassNamingConventions", "checkClassNameConvention");

        // Sichtbarkeit und Getter/Setter
        CRITERIA_MAPPING.put("attributesShouldBePrivate", "checkAttributesArePrivate");
        CRITERIA_MAPPING.put("attributesShouldHaveGetters", "checkGettersForPrivateAttributes");
        CRITERIA_MAPPING.put("attributesShouldHaveSetters", "checkSettersForPrivateAttributes");

        // Konstruktor
        CRITERIA_MAPPING.put("constructorShouldExist", "checkConstructorPresenceAndConvention");

        // Weitere Kriterien
        CRITERIA_MAPPING.put("associationConsistency", "checkAssociationConsistency");
        CRITERIA_MAPPING.put("minimumAttributesOrMethods", "checkMinimumAttributesOrMethods");
        CRITERIA_MAPPING.put("minimumOneClass", "checkMinimumOneClass");
    }

    /**
     * Extrahiert die Bewertungskriterien und ihre Gewichtungen aus der task.xml-Datei.
     *
     * @param taskXmlPath Pfad zur task.xml-Datei
     * @return Liste von Kriterien mit ihren Gewichtungen
     * @throws JAXBException wenn ein Fehler beim Parsen der XML-Datei auftritt
     */
    public List<CriterionWithWeight> extractCriteriaWithWeights(String taskXmlPath) throws JAXBException {
        List<CriterionWithWeight> criteriaWithWeights = new ArrayList<>();
        System.out.println("Versuche, Datei zu parsen: " + taskXmlPath);

        // Parse die task.xml
        JAXBContext context = JAXBContext.newInstance(TaskType.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object taskObj = unmarshaller.unmarshal(new File(taskXmlPath));
        System.out.println("Unmarshalling abgeschlossen, taskObj: " + taskObj);

        TaskType task = null;
        if (taskObj instanceof TaskType) {
            task = (TaskType) taskObj;
            System.out.println("taskObj ist direkt eine Instanz von TaskType");
        } else if (taskObj instanceof JAXBElement) {
            @SuppressWarnings("unchecked")
            JAXBElement<TaskType> jaxbElement = (JAXBElement<TaskType>) taskObj;
            task = jaxbElement.getValue();
            System.out.println("taskObj ist ein JAXBElement, extrahierter TaskType: " + task);
        } else {
            System.out.println("taskObj ist weder TaskType noch JAXBElement: " + taskObj);
        }

        if (task != null) {
            System.out.println("TaskType erfolgreich erhalten, GradingHints: " + task.getGradingHints());
            processGradingHints(task.getGradingHints(), criteriaWithWeights);
        } else {
            System.out.println("TaskType konnte nicht extrahiert werden");
        }

        return criteriaWithWeights;
    }

    /**
     * Verarbeitet die GradingHints, um Test-Referenzen und ihre Gewichtungen zu extrahieren.
     *
     * @param gradingHints Das GradingHints-Objekt aus der task.xml
     * @param criteriaWithWeights Liste, in die die extrahierten Kriterien und Gewichtungen eingefügt werden
     */
    private void processGradingHints(GradingHintsType gradingHints, List<CriterionWithWeight> criteriaWithWeights) {
        if (gradingHints == null) {
            System.out.println("GradingHints ist null");
            return;
        }
        System.out.println("Verarbeite GradingHints: root=" + gradingHints.getRoot() + ", combine=" + gradingHints.getCombine());

        // Erstelle eine Map von ID zu GradesNodeType für die spätere Auflösung von Combine-Referenzen
        Map<String, GradesNodeType> nodeMap = new HashMap<>();

        // Füge das root-Element zur Map hinzu
        GradesNodeType root = gradingHints.getRoot();
        if (root != null && root.getId() != null) {
            nodeMap.put(root.getId(), root);
            System.out.println("Root hinzugefügt zur Map: " + root.getId());
        }

        // Füge alle combine-Elemente zur Map hinzu
        List<GradesNodeType> combines = gradingHints.getCombine();
        if (combines != null) {
            for (GradesNodeType combine : combines) {
                if (combine.getId() != null) {
                    nodeMap.put(combine.getId(), combine);
                    System.out.println("Combine hinzugefügt zur Map: " + combine.getId());
                }
            }
        }

        // Verarbeite das root-Element
        if (root != null) {
            System.out.println("Starte Verarbeitung von root: " + root.getId());
            processItems(root, criteriaWithWeights, nodeMap, new HashSet<>());
        }
    }

    /**
     * Verarbeitet ein GradesNodeType-Objekt, um Test-Referenzen und ihre Gewichtungen zu extrahieren.
     *
     * @param gradesNode Das GradesNodeType-Objekt
     * @param criteriaWithWeights Liste, in die die extrahierten Kriterien und Gewichtungen eingefügt werden
     * @param nodeMap Map von ID zu GradesNodeType für die Auflösung von Combine-Referenzen
     * @param visited Set von bereits besuchten Node-IDs, um Zyklen zu vermeiden
     */
    private void processItems(GradesNodeType gradesNode, List<CriterionWithWeight> criteriaWithWeights,
                             Map<String, GradesNodeType> nodeMap, Set<String> visited) {
        if (gradesNode == null) {
            System.out.println("GradesNode ist null");
            return;
        }

        // Verhindere Zyklen durch Überprüfen, ob der Knoten bereits besucht wurde
        String nodeId = gradesNode.getId();
        System.out.println("Verarbeite Node: " + nodeId);
        if (nodeId != null && !visited.add(nodeId)) {
            System.out.println("Zyklus erkannt für Node: " + nodeId);
            return; // Knoten wurde bereits besucht, Zyklus erkannt
        }

        // Verarbeite die Test-Referenzen und Combine-Referenzen
        List<GradesBaseRefChildType> refs = gradesNode.getTestRefOrCombineRef();
        System.out.println("Anzahl Refs: " + (refs != null ? refs.size() : 0));
        if (refs != null) {
            for (GradesBaseRefChildType ref : refs) {
                System.out.println("Verarbeite Ref: " + ref);
                if (ref instanceof GradesTestRefChildType) {
                    GradesTestRefChildType testRef = (GradesTestRefChildType) ref;
                    String subRef = testRef.getSubRef();
                    String refId = testRef.getRef();
                    double weight = testRef.getWeight() != null ? testRef.getWeight().doubleValue() : 0.0; // Default: 0.0
                    System.out.println("TestRef gefunden: subRef=" + subRef + ", refId=" + refId + ", weight=" + weight);
                    if (subRef != null && CRITERIA_MAPPING.containsKey(subRef)) {
                        String criterion = CRITERIA_MAPPING.get(subRef);
                        System.out.println("Kriterium hinzugefügt: " + criterion + " mit Gewichtung " + weight);
                        criteriaWithWeights.add(new CriterionWithWeight(criterion, weight));
                    }
                } else if (ref instanceof GradesCombineRefChildType) {
                    GradesCombineRefChildType combineRef = (GradesCombineRefChildType) ref;
                    String refId = combineRef.getRef();
                    System.out.println("CombineRef gefunden: refId=" + refId);
                    if (refId != null) {
                        GradesNodeType referencedNode = nodeMap.get(refId);
                        if (referencedNode != null) {
                            System.out.println("Verarbeite referenzierten Node: " + refId);
                            processItems(referencedNode, criteriaWithWeights, nodeMap, visited);
                        } else {
                            System.out.println("Referenzierter Node nicht gefunden: " + refId);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gibt die Bewertungsergebnisse in der Konsole aus, basierend auf der gleichen Logik wie ResponseXmlGenerator.
     *
     * @param evaluationResult Ergebnis der Bewertung durch den Evaluator
     * @param criteriaWithWeights Liste der Kriterien mit ihren Gewichtungen
     */
    private void printEvaluationResult(EvaluationResult evaluationResult, List<CriterionWithWeight> criteriaWithWeights) {
        System.out.println("Bewertungsergebnis:");
        List<String> errors = (evaluationResult != null) ? evaluationResult.getErrors() : new ArrayList<>();
        List<String> warnings = (evaluationResult != null) ? evaluationResult.getWarnings() : new ArrayList<>();

        if (warnings != null && !warnings.isEmpty()) {
            System.out.println("Warnungen:");
            for (String warning : warnings) {
                System.out.println("- " + warning);
            }
        }

        if (errors != null && !errors.isEmpty()) {
            System.out.println("Fehler:");
            for (String error : errors) {
                System.out.println("- " + error);
            }
        }

        // Prüfe jedes Kriterium auf Erfüllung
        for (CriterionWithWeight cw : criteriaWithWeights) {
            String criterion = cw.criterion;
            double weight = cw.weight;
            boolean criterionFulfilled = true;

            switch (criterion) {
                case "checkAttributeNameConvention":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Attribut") && w.contains("sollte mit einem Kleinbuchstaben beginnen"));
                    break;
                case "checkMethodNameConvention":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Methode") && w.contains("sollte mit einem Kleinbuchstaben beginnen"));
                    break;
                case "checkClassNameConvention":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Klasse") && w.contains("sollte mit einem Großbuchstaben beginnen"));
                    break;
                case "checkAttributesArePrivate":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Attribut") && w.contains("ist nicht privat"));
                    break;
                case "checkGettersForPrivateAttributes":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Privates Attribut") && w.contains("hat keinen Getter"));
                    break;
                case "checkSettersForPrivateAttributes":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Privates Attribut") && w.contains("hat keinen Setter"));
                    break;
                case "checkConstructorPresenceAndConvention":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Konstruktor") || w.contains("hat keinen Konstruktor"));
                    break;
                case "checkAssociationConsistency":
                    criterionFulfilled = !errors.stream().anyMatch(e -> e.contains("Assoziation") && e.contains("verweist auf nicht existierende Klasse"));
                    break;
                case "checkMinimumAttributesOrMethods":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("hat weder Attribute noch Methoden"));
                    break;
                case "checkMinimumOneClass":
                    criterionFulfilled = !errors.stream().anyMatch(e -> e.contains("Modell enthält keine Klassen"));
                    break;
                default:
                    continue; // Überspringe unbekannte Kriterien
            }

            if (criterionFulfilled) {
                System.out.println("Erfolge:");
                System.out.println("- " + criterion.replace("check", "") + " erfüllt (Gewichtung: " + weight + ")");
            } else {
                System.out.println("Fehlschläge:");
                System.out.println("- " + criterion.replace("check", "") + " nicht erfüllt (Gewichtung: 0.0)");
            }
        }
    }

    /**
     * Führt die Bewertung durch und generiert die response.xml.
     *
     * @param taskXmlPath Pfad zur task.xml-Datei
     * @param responseXmlPath Pfad, an dem die response.xml gespeichert werden soll
     * @throws JAXBException wenn ein Fehler beim Parsen oder Schreiben auftritt
     */
    public static void main(String[] args) {
        TaskXmlParser parser = new TaskXmlParser();
        UmlModelParser umlParser = new UmlModelParser();
        UmlGeneralCriteriaEvaluator evaluator = new UmlGeneralCriteriaEvaluator();
        ResponseXmlGenerator generator = new ResponseXmlGenerator();
        
        try {
            // Extrahiere Kriterien und Gewichtungen aus task.xml
            List<CriterionWithWeight> criteriaWithWeights = parser.extractCriteriaWithWeights("models/task1.xml");
            System.out.println("Extrahierte Kriterien mit Gewichtungen:");
            for (CriterionWithWeight cw : criteriaWithWeights) {
                System.out.println("- " + cw.criterion + " (weight: " + cw.weight + ")");
            }

            // Lade UML-Klassen aus der XMI-Datei
           // String xmiFilePath = "models/U09xmiTest.xmi";
            String xmiFilePath = "models/U09VisualParadigmUML2v2.xmi";
            //String xmiFilePath = "models/U09PapyrusCorrect.uml";
            List<Class> umlClasses = umlParser.parse(xmiFilePath);
            System.out.println("Geladene UML-Klassen:");
            for (Class umlClass : umlClasses) {
                System.out.println(umlParser.formatUmlClass(umlClass));
            }

            // Führe die Bewertung durch
            List<String> criteria = new ArrayList<>();
            for (CriterionWithWeight cw : criteriaWithWeights) {
                criteria.add(cw.criterion);
            }
            EvaluationResult evaluationResult = evaluator.evaluate(umlClasses, criteria);

            // Ausgabe der Bewertungsergebnisse
            parser.printEvaluationResult(evaluationResult, criteriaWithWeights);

            // Generiere die response.xml
            ResponseType response = generator.generateResponse(umlClasses, criteriaWithWeights, evaluationResult);
            
            // Schreibe die response.xml
            JAXBContext context = JAXBContext.newInstance(ResponseType.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(response, new File("models/response1.xml"));
            System.out.println("Response.xml erfolgreich erstellt: models/response1.xml");
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}