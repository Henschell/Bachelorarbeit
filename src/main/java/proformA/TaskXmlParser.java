package proformA;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import proforma.xml21.*;
import umlParser.UmlModelParser;
import proformA.dto.CriterionWithWeight;

/**
 * Die Klasse {@code TaskXmlParser} extrahiert Kriterien und Musterlösungen aus einer task.xml-Datei.
 */
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
        CRITERIA_MAPPING.put("testModelComparison", "testModelComparison");
    }
    
    /**
     * Extrahiert Kriterien mit Gewichtungen aus der task.xml-Datei.
     *
     * @param taskXmlPath Pfad zur task.xml-Datei
     * @param debug Flag zur Aktivierung von Debugging-Ausgaben
     * @return Liste von Kriterien mit Gewichtungen
     * @throws JAXBException bei Fehlern beim Unmarshalling
     * @throws IOException bei Dateifehlern
     */
    public List<CriterionWithWeight> extractCriteriaWithWeights(String taskXmlPath, boolean debug) throws JAXBException, IOException {
        List<CriterionWithWeight> criteriaWithWeights = new ArrayList<>();
        if (debug) System.out.println("Versuche, Datei zu parsen: " + taskXmlPath);

        JAXBContext context = JAXBContext.newInstance(TaskType.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object taskObj = unmarshaller.unmarshal(new File(taskXmlPath));
        if (debug) System.out.println("Unmarshalling abgeschlossen, taskObj: " + taskObj);

        TaskType task = null;
        if (taskObj instanceof TaskType) {
            task = (TaskType) taskObj;
            if (debug) System.out.println("taskObj ist direkt eine Instanz von TaskType");
        } else if (taskObj instanceof JAXBElement) {
            @SuppressWarnings("unchecked")
            JAXBElement<TaskType> jaxbElement = (JAXBElement<TaskType>) taskObj;
            task = jaxbElement.getValue();
            if (debug) System.out.println("taskObj ist ein JAXBElement, extrahierter TaskType: " + task);
        } else {
            if (debug) System.out.println("taskObj ist weder TaskType noch JAXBElement: " + taskObj);
        }

        if (task != null) {
            if (debug) System.out.println("TaskType erfolgreich erhalten, GradingHints: " + task.getGradingHints());
            processGradingHints(task.getGradingHints(), criteriaWithWeights, debug);
        } else {
            if (debug) System.out.println("TaskType konnte nicht extrahiert werden");
        }

        return criteriaWithWeights;
    }

    private void processGradingHints(GradingHintsType gradingHints, List<CriterionWithWeight> criteriaWithWeights, boolean debug) {
        if (gradingHints == null) {
            if (debug) System.out.println("GradingHints ist null");
            return;
        }
        if (debug) System.out.println("Verarbeite GradingHints: root=" + gradingHints.getRoot() + ", combine=" + gradingHints.getCombine());

        Map<String, GradesNodeType> nodeMap = new HashMap<>();
        GradesNodeType root = gradingHints.getRoot();
        if (root != null && root.getId() != null) {
            nodeMap.put(root.getId(), root);
            if (debug) System.out.println("Root hinzugefügt zur Map: " + root.getId());
        }

        List<GradesNodeType> combines = gradingHints.getCombine();
        if (combines != null) {
            for (GradesNodeType combine : combines) {
                if (combine.getId() != null) {
                    nodeMap.put(combine.getId(), combine);
                    if (debug) System.out.println("Combine hinzugefügt zur Map: " + combine.getId());
                }
            }
        }

        if (root != null) {
            if (debug) System.out.println("Starte Verarbeitung von root: " + root.getId());
            processItems(root, criteriaWithWeights, nodeMap, new HashSet<>(), debug);
        }
    }

    private void processItems(GradesNodeType gradesNode, List<CriterionWithWeight> criteriaWithWeights,
                             Map<String, GradesNodeType> nodeMap, Set<String> visited, boolean debug) {
        if (gradesNode == null) {
            if (debug) System.out.println("GradesNode ist null");
            return;
        }

        String nodeId = gradesNode.getId();
        if (debug) System.out.println("Verarbeite Node: " + nodeId);
        if (nodeId != null && !visited.add(nodeId)) {
            if (debug) System.out.println("Zyklus erkannt für Node: " + nodeId);
            return;
        }

        List<GradesBaseRefChildType> refs = gradesNode.getTestRefOrCombineRef();
        if (debug) System.out.println("Anzahl Refs: " + (refs != null ? refs.size() : 0));
        if (refs != null) {
            for (GradesBaseRefChildType ref : refs) {
                if (debug) System.out.println("Verarbeite Ref: " + ref);
                if (ref instanceof GradesTestRefChildType) {
                    GradesTestRefChildType testRef = (GradesTestRefChildType) ref;
                    String subRef = testRef.getSubRef();
                    String refId = testRef.getRef();
                    double weight = testRef.getWeight() != null ? testRef.getWeight().doubleValue() : 0.0;
                    if (debug) System.out.println("TestRef gefunden: subRef=" + subRef + ", refId=" + refId + ", weight=" + weight);
                    if (subRef != null && CRITERIA_MAPPING.containsKey(subRef)) {
                        String criterion = CRITERIA_MAPPING.get(subRef);
                        if (debug) System.out.println("Kriterium hinzugefügt: " + criterion + " mit Gewichtung " + weight);
                        criteriaWithWeights.add(new CriterionWithWeight(criterion, weight));
                    }
                } else if (ref instanceof GradesCombineRefChildType) {
                    GradesCombineRefChildType combineRef = (GradesCombineRefChildType) ref;
                    String refId = combineRef.getRef();
                    if (debug) System.out.println("CombineRef gefunden: refId=" + refId);
                    if (refId != null) {
                        GradesNodeType referencedNode = nodeMap.get(refId);
                        if (referencedNode != null) {
                            if (debug) System.out.println("Verarbeite referenzierten Node: " + refId);
                            processItems(referencedNode, criteriaWithWeights, nodeMap, visited, debug);
                        } else {
                            if (debug) System.out.println("Referenzierter Node nicht gefunden: " + refId);
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
     * @param debug Flag zur Aktivierung von Debugging-Ausgaben
     * @return Byte-Array der Musterlösung (z. B. XMI-Datei), oder null wenn keine Musterlösung gefunden wurde
     * @throws JAXBException wenn das Parsen fehlschlägt
     * @throws IOException bei Dateifehlern
     */
    public byte[] extractReferenceModel(String taskXmlPath, boolean debug) throws JAXBException, IOException {
        if (debug) System.out.println("Versuche, Musterlösung aus " + taskXmlPath + " zu extrahieren");

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
            if (debug) System.out.println("TaskType konnte nicht extrahiert werden");
            return null; // Keine Task gefunden
        }

        // Extrahiere <model-solutions>
        ModelSolutionsType modelSolutions = task.getModelSolutions();
        if (modelSolutions != null && modelSolutions.getModelSolution() != null) {
            if (debug) System.out.println("ModelSolutions gefunden: " + modelSolutions.getModelSolution().size() + " Lösungen");
            for (ModelSolutionType solution : modelSolutions.getModelSolution()) {
                if (debug) System.out.println("Verarbeite ModelSolution: " + solution);
                // Finde die referenzierte Datei
                if (solution.getFilerefs() != null && !solution.getFilerefs().getFileref().isEmpty()) {
                    if (debug) System.out.println("Filerefs gefunden: " + solution.getFilerefs().getFileref().size());
                    for (FilerefType fileref : solution.getFilerefs().getFileref()) {
                        String refId = fileref.getRefid(); // Korrigierter Methodenname
                        if (debug) System.out.println("Verarbeite Fileref mit refId: " + refId);
                        // Suche die Datei in <files>
                        if (task.getFiles() != null && !task.getFiles().getFile().isEmpty()) {
                            if (debug) System.out.println("Files gefunden: " + task.getFiles().getFile().size());
                            for (TaskFileType file : task.getFiles().getFile()) {
                                if (file.getId().equals(refId)) {
                                    if (debug) System.out.println("Passende Datei gefunden mit id: " + refId);
                                    // Prüfe auf <embedded-txt-file>
                                    if (file.getEmbeddedTxtFile() != null) {
                                        String content = file.getEmbeddedTxtFile().getValue(); // Korrigierter Methodenname
                                        if (debug) System.out.println("EmbeddedTxtFile gefunden, Inhalt: " + content);
                                        return content.getBytes(); // Text als bytes zurückgeben
                                    }
                                    // Prüfe auf <embedded-bin-file>
                                    if (file.getEmbeddedBinFile() != null) {
                                        byte[] content = file.getEmbeddedBinFile().getValue(); // Korrigierter Methodenname
                                        if (debug) System.out.println("EmbeddedBinFile gefunden, Länge: " + content.length);
                                        return content; // Base64 ist bereits dekodiert als byte[]
                                    }
                                    // Prüfe auf <attached-bin-file>
                                    if (file.getAttachedBinFile() != null) {
                                        String filePath = file.getAttachedBinFile(); // Direkt als String verwenden
                                        java.io.File fileOnDisk = new java.io.File(filePath);
                                        if (fileOnDisk.exists()) {
                                            if (debug) System.out.println("AttachedBinFile gefunden, Pfad: " + filePath);
                                            return java.nio.file.Files.readAllBytes(fileOnDisk.toPath()); // Externe Datei lesen
                                        } else {
                                            if (debug) System.out.println("AttachedBinFile nicht gefunden unter: " + filePath);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (debug) System.out.println("Keine ModelSolutions gefunden");
        }

        if (debug) System.out.println("Keine Musterlösung gefunden");
        return null; // Keine Musterlösung gefunden
    }

    /**
     * Hauptmethode zum Testen der TaskXmlParser-Klasse.
     *
     * @param args Kommandozeilenargumente (nicht verwendet)
     */
    public static void main(String[] args) {
        TaskXmlParser parser = new TaskXmlParser();
        String taskXmlPath = "models/taskWithSample.xml"; // Passe den Pfad an deine Datei an
        UmlModelParser umlParser = new UmlModelParser(); // Instanz für UML-Parsing

        try {
            // Teste extractCriteriaWithWeights
            System.out.println("Teste extractCriteriaWithWeights:");
            List<CriterionWithWeight> criteria = parser.extractCriteriaWithWeights(taskXmlPath, false);
            System.out.println("Extrahierte Kriterien mit Gewichtungen:");
            for (CriterionWithWeight cw : criteria) {
                System.out.println("- " + cw.getCriterion() + " (Gewicht: " + cw.getWeight() + ")");
            }
            System.out.println();

            // Teste extractReferenceModel und parse mit UmlModelParser
            System.out.println("\nTeste extractReferenceModel und UML-Parsing:");
            byte[] referenceModel = parser.extractReferenceModel(taskXmlPath, false);
            if (referenceModel != null) {
                System.out.println("Musterlösung extrahiert für " + taskXmlPath);

                // Speichere die Musterlösung temporär, um sie mit UmlModelParser zu parsen
                String tempFilePath = "models/extrahierteLösung.xmi";
                Files.write(java.nio.file.Paths.get(tempFilePath), referenceModel);
                System.out.println("Temporäre Datei gespeichert unter: " + tempFilePath);

                // Parse die temporäre Datei mit UmlModelParser
                List<org.eclipse.uml2.uml.Class> umlClasses = umlParser.parse(tempFilePath, false);
                System.out.println("Gefundene UML-Klassen in der Musterlösung:");
                for (org.eclipse.uml2.uml.Class umlClass : umlClasses) {
                    System.out.println("- Klasse: " + umlClass.getName());
                    // Optional: Weitere Details mit formatUmlClass ausgeben
                    System.out.println(umlParser.formatUmlClass(umlClass));
                }
            } else {
                System.out.println("Keine Musterlösung gefunden.");
            }

        } catch (JAXBException | IOException e) {
            System.err.println("Fehler beim Testen der TaskXmlParser-Klasse: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}