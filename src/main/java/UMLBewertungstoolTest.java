import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import Evaluator.EvaluationResult;
import Evaluator.UmlCriteriaEvaluatorTest;
import proformA.ResponseXmlGenerator;
import proformA.TaskXmlParser;
import proformA.dto.CriterionWithWeight;
import proforma.xml21.ResponseType;

public class UMLBewertungstoolTest {
    public static void main(String[] args) {
        TaskXmlParser parser = new TaskXmlParser();
        ResponseXmlGenerator generator = new ResponseXmlGenerator();
        String taskXmlMitMuster = "models/taskWithSample.xml";
        String studentLösung = "models/Astah/U09Falsch1.xmi";
        String responseXmlPath = "models/Astah/responseU09Falsch.xml";

        try {
            // Extrahiere Kriterien und Gewichtungen aus task.xml
            List<CriterionWithWeight> criteriaWithWeights = parser.extractCriteriaWithWeights(taskXmlMitMuster,false);
            System.out.println("Extrahierte Kriterien mit Gewichtungen:");
            for (CriterionWithWeight cw : criteriaWithWeights) {
                System.out.println("- " + cw.getCriterion() + " (weight: " + cw.getWeight() + ")");
            }

            // Extrahiere Musterlösung
            byte[] referenceModelBytes = parser.extractReferenceModel(taskXmlMitMuster,false);
            if (referenceModelBytes != null) {
                java.nio.file.Files.write(java.nio.file.Paths.get("models/extracted_reference_modelv2.xmi"), referenceModelBytes);
                System.out.println("Musterlösung extrahiert und in models/extracted_reference_modelv2.xmi gespeichert.\n");
            } else {
                System.out.println("Keine Musterlösung gefunden.");
            }

            // Erstelle ein Objekt von UmlCriteriaEvaluatorTest und initialisiere es
            UmlCriteriaEvaluatorTest testEvaluator = new UmlCriteriaEvaluatorTest();
            testEvaluator.initialize(studentLösung);

            // Führe die allgemeinen Tests aus (test1)
            Map<String, EvaluationResult> test1Results = testEvaluator.runAllgemeineTests(criteriaWithWeights);
            System.out.println("Testergebnisse (test1):");
            test1Results.forEach((key, value) -> System.out.println("- " + key + ": " + (value.isPassed() ? "Bestanden" : "Fehlgeschlagen") +
                    (value.isPassed() ? "" : " (" + value.toString() + ")")));

            // Führe den Musterlösungsvergleich aus (test2)
            Map<String, EvaluationResult> test2Results = testEvaluator.runModelComparisonTest(criteriaWithWeights, studentLösung, "models/extracted_reference_modelv2.xmi");
            if (!test2Results.isEmpty()) {
                System.out.println("Testergebnisse (test2):");
                test2Results.forEach((key, value) -> System.out.println("- " + key + ": " + (value.isPassed() ? "Bestanden" : "Fehlgeschlagen") +
                        (value.isPassed() ? "" : " (" + value.toString() + ")")));
            }

            // Bereite die Testergebnisse für die Response vor
            List<ResponseXmlGenerator.TestResult> results = new ArrayList<>();
            // Füge test1-Ergebnisse hinzu (nur allgemeine Kriterien)
            test1Results.forEach((criterion, result) -> {
                if (!criterion.equals("testModelComparison")) { // Verhindere, dass testModelComparison hier landet
                    results.add(new ResponseXmlGenerator.TestResult("test1", criterion, result.isPassed(), result.getFeedbackText(), result.getWeight()));
                }
            });
            // Füge test2-Ergebnisse hinzu (nur testModelComparison)
            test2Results.forEach((criterion, result) -> results.add(new ResponseXmlGenerator.TestResult("test2", criterion, result.isPassed(), result.getFeedbackText(), result.getWeight())));

            // Generiere die response.xml
            ResponseType response = generator.generateResponse(results);

            // Schreibe die response.xml
            JAXBContext context = JAXBContext.newInstance(ResponseType.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(response, new File(responseXmlPath));
            System.out.println("Response.xml erfolgreich erstellt: " + responseXmlPath);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}