package proformA;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import proforma.xml21.*;
import Evaluator.EvaluationResult;
import Evaluator.UmlGeneralCriteriaEvaluatorTest;
import proformA.dto.CriterionWithWeight;


public class TaskXmlParser {

    private static final Map<String, String> CRITERIA_MAPPING = new HashMap<>();

    static {
        // Schlüssel entsprechen direkt den sub-ref-Werten aus taskv2.xml
        CRITERIA_MAPPING.put("testMinimumOneClass", "testMinimumOneClass");
        CRITERIA_MAPPING.put("testClassNameConvention", "testClassNameConvention");
        CRITERIA_MAPPING.put("testAttributeNameConvention", "testAttributeNameConvention");
        CRITERIA_MAPPING.put("testAssociationConsistency", "testAssociationConsistency");
        CRITERIA_MAPPING.put("testAttributesArePrivate", "testAttributesArePrivate");
        CRITERIA_MAPPING.put("testGettersForPrivateAttributes", "testGettersForPrivateAttributes");
        CRITERIA_MAPPING.put("testSettersForPrivateAttributes", "testSettersForPrivateAttributes");
        CRITERIA_MAPPING.put("testMethodNameConvention", "testMethodNameConvention");
        CRITERIA_MAPPING.put("testConstructorPresenceAndConvention", "testConstructorPresenceAndConvention");
        CRITERIA_MAPPING.put("testMinimumAttributesOrMethods", "testMinimumAttributesOrMethods");
    }
    
    
    public List<CriterionWithWeight> extractCriteriaWithWeights(String taskXmlPath) throws JAXBException {
        List<CriterionWithWeight> criteriaWithWeights = new ArrayList<>();
        System.out.println("Versuche, Datei zu parsen: " + taskXmlPath);

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

    private void processGradingHints(GradingHintsType gradingHints, List<CriterionWithWeight> criteriaWithWeights) {
        if (gradingHints == null) {
            System.out.println("GradingHints ist null");
            return;
        }
        System.out.println("Verarbeite GradingHints: root=" + gradingHints.getRoot() + ", combine=" + gradingHints.getCombine());

        Map<String, GradesNodeType> nodeMap = new HashMap<>();
        GradesNodeType root = gradingHints.getRoot();
        if (root != null && root.getId() != null) {
            nodeMap.put(root.getId(), root);
            System.out.println("Root hinzugefügt zur Map: " + root.getId());
        }

        List<GradesNodeType> combines = gradingHints.getCombine();
        if (combines != null) {
            for (GradesNodeType combine : combines) {
                if (combine.getId() != null) {
                    nodeMap.put(combine.getId(), combine);
                    System.out.println("Combine hinzugefügt zur Map: " + combine.getId());
                }
            }
        }

        if (root != null) {
            System.out.println("Starte Verarbeitung von root: " + root.getId());
            processItems(root, criteriaWithWeights, nodeMap, new HashSet<>());
        }
    }

    private void processItems(GradesNodeType gradesNode, List<CriterionWithWeight> criteriaWithWeights,
                             Map<String, GradesNodeType> nodeMap, Set<String> visited) {
        if (gradesNode == null) {
            System.out.println("GradesNode ist null");
            return;
        }

        String nodeId = gradesNode.getId();
        System.out.println("Verarbeite Node: " + nodeId);
        if (nodeId != null && !visited.add(nodeId)) {
            System.out.println("Zyklus erkannt für Node: " + nodeId);
            return;
        }

        List<GradesBaseRefChildType> refs = gradesNode.getTestRefOrCombineRef();
        System.out.println("Anzahl Refs: " + (refs != null ? refs.size() : 0));
        if (refs != null) {
            for (GradesBaseRefChildType ref : refs) {
                System.out.println("Verarbeite Ref: " + ref);
                if (ref instanceof GradesTestRefChildType) {
                    GradesTestRefChildType testRef = (GradesTestRefChildType) ref;
                    String subRef = testRef.getSubRef();
                    String refId = testRef.getRef();
                    double weight = testRef.getWeight() != null ? testRef.getWeight().doubleValue() : 0.0;
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
     * Extrahiert die Musterlösung aus der task.xml.
     *
     * @param taskXmlPath Pfad zur task.xml
     * @return Byte-Array der Musterlösung (z. B. XMI-Datei), oder null wenn keine Musterlösung gefunden wurde
     * @throws JAXBException wenn das Parsen fehlschlägt
     */
    public byte[] extractReferenceModel(String taskXmlPath) throws JAXBException {
        // Parse die task.xml mit JAXB
        JAXBContext context = JAXBContext.newInstance(TaskType.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object taskObj = unmarshaller.unmarshal(new File(taskXmlPath));

        TaskType task = null;
        if (taskObj instanceof TaskType) {
            task = (TaskType) taskObj;
        } else if (taskObj instanceof JAXBElement) {
            @SuppressWarnings("unchecked")
            JAXBElement<TaskType> jaxbElement = (JAXBElement<TaskType>) taskObj;
            task = jaxbElement.getValue();
        }

        if (task == null) {
            return null; // Keine Task gefunden
        }

        // Extrahiere <model-solutions>
        ModelSolutionsType modelSolutions = task.getModelSolutions();
        if (modelSolutions != null && modelSolutions.getModelSolution() != null) {
            for (ModelSolutionType solution : modelSolutions.getModelSolution()) {
                // Finde die referenzierte Datei
                if (solution.getFilerefs() != null && !solution.getFilerefs().getFileref().isEmpty()) {
                    for (FilerefType fileref : solution.getFilerefs().getFileref()) {
                        String refId = fileref.getRefid(); // Korrigierter Methodenname
                        // Suche die Datei in <files>
                        if (task.getFiles() != null && !task.getFiles().getFile().isEmpty()) {
                            for (TaskFileType file : task.getFiles().getFile()) {
                                if (file.getId().equals(refId)) {
                                    // Prüfe auf <embedded-txt-file>
                                    if (file.getEmbeddedTxtFile() != null) {
                                        String content = file.getEmbeddedTxtFile().getValue(); // Korrigierter Methodenname
                                        return content.getBytes(); // Text als bytes zurückgeben
                                    }
                                    // Prüfe auf <embedded-bin-file>
                                    if (file.getEmbeddedBinFile() != null) {
                                        byte[] content = file.getEmbeddedBinFile().getValue(); // Korrigierter Methodenname
                                        return content; // Base64 ist bereits dekodiert als byte[]
                                    }
                                    // Prüfe auf <attached-bin-file>
                                    if (file.getAttachedBinFile() != null) {
                                        String filePath = file.getAttachedBinFile(); // Direkt als String verwenden
                                        java.io.File fileOnDisk = new java.io.File(filePath);
                                        if (fileOnDisk.exists()) {
                                            try {
												return java.nio.file.Files.readAllBytes(fileOnDisk.toPath());
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											} // Externe Datei lesen
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return null; // Keine Musterlösung gefunden
    }
    

    public static void main(String[] args) {
        TaskXmlParser parser = new TaskXmlParser();
        ResponseXmlGenerator generator = new ResponseXmlGenerator();

        try {
            // Extrahiere Kriterien und Gewichtungen aus task.xml
            List<CriterionWithWeight> criteriaWithWeights = parser.extractCriteriaWithWeights("models/taskv2.xml");
            System.out.println("Extrahierte Kriterien mit Gewichtungen:");
            for (CriterionWithWeight cw : criteriaWithWeights) {
                System.out.println("- " + cw.getCriterion() + " (weight: " + cw.getWeight() + ")");
            }
            
            
            // Extrahiere Musterlösung
            byte[] referenceModelBytes = parser.extractReferenceModel("models/taskv2.xml");
            if (referenceModelBytes != null) {
                java.nio.file.Files.write(java.nio.file.Paths.get("models/extracted_reference_model.xmi"), referenceModelBytes);
                System.out.println("Musterlösung extrahiert und in models/extracted_reference_model.xmi gespeichert.");
            } else {
                System.out.println("Keine Musterlösung gefunden.");
            }

            // Erstelle ein Objekt von UmlGeneralCriteriaEvaluatorTest und initialisiere es
            UmlGeneralCriteriaEvaluatorTest testEvaluator = new UmlGeneralCriteriaEvaluatorTest();
            String xmiFilePath = "models/U09PapyrusCorrect.xmi";
            testEvaluator.initialize(xmiFilePath);

            // Führe die Tests manuell aus
            Map<String, EvaluationResult> testResults = testEvaluator.runTests(criteriaWithWeights);
            System.out.println("Testergebnisse:");
            for (Map.Entry<String,EvaluationResult> entry : testResults.entrySet()) {
                System.out.println("- " + entry.getKey() + ": " + (entry.getValue().isPassed() ? "Bestanden" : "Fehlgeschlagen") +
                        (entry.getValue().isPassed() ? "" : " (" + entry.getValue().toString() + ")"));
            }

            // Konvertiere die Ergebnisse in eine Liste von TestResult-Objekten für ResponseXmlGenerator
            List<ResponseXmlGenerator.TestResult> resultsForGenerator = new ArrayList<>();
            for (CriterionWithWeight cw : criteriaWithWeights) {
                EvaluationResult result = testResults.get(cw.getCriterion());
                if (result != null) {
                    resultsForGenerator.add(new ResponseXmlGenerator.TestResult(
                        cw.getCriterion(),
                        result.isPassed(),
                        result.toString(),
                        result.getWeight() // Gewichtung aus EvaluationResult übernehmen
                    ));
                } else {
                    throw new IllegalStateException("Testergebnis für " + cw.getCriterion() + " nicht gefunden.");
                }
            }

            // Generiere die response.xml basierend auf den Testergebnissen
            ResponseType response = generator.generateResponse(resultsForGenerator);

            // Schreibe die response.xml
            JAXBContext context = JAXBContext.newInstance(ResponseType.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(response, new File("models/response3.xml"));
            System.out.println("Response.xml erfolgreich erstellt: models/response3.xml");
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}