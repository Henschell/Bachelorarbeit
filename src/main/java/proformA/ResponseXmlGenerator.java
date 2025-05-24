package proformA;

import proforma.xml21.*;
import org.eclipse.uml2.uml.Class;

import Evaluator.UmlGeneralCriteriaEvaluator;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class ResponseXmlGenerator {
    private final UmlGeneralCriteriaEvaluator criteriaEvaluator;

    public ResponseXmlGenerator() {
        this.criteriaEvaluator = new UmlGeneralCriteriaEvaluator();
    }

    /**
     * Generiert eine ProFormA-Response basierend auf den Prüfkriterien, Gewichtungen und Bewertungsergebnissen.
     * Fügt Feedback für Fehler, Warnungen und erfolgreich erfüllte Kriterien hinzu.
     *
     * @param umlClasses Liste der UML-Klassen des Studentenmodells
     * @param criteriaWithWeights Liste der Kriterien mit ihren Gewichtungen
     * @param evaluationResult Ergebnis der Bewertung durch den Evaluator
     * @return ResponseType-Objekt, das in eine response.xml geschrieben werden kann
     */
    public ResponseType generateResponse(List<Class> umlClasses, List<CriterionWithWeight> criteriaWithWeights, UmlGeneralCriteriaEvaluator.EvaluationResult evaluationResult) {
        // Erstelle die ResponseType-Instanz
        ResponseType response = new ResponseType();
        response.setLang("de"); // Sprache auf Deutsch setzen, konsistent mit task.xml

        // Erstelle die SeparateTestFeedbackType-Instanz und setze sie
        SeparateTestFeedbackType separateTestFeedback = new SeparateTestFeedbackType();
        response.setSeparateTestFeedback(separateTestFeedback);

        // Setze das erforderliche submissionFeedbackList-Feld (vorläufig leer)
        separateTestFeedback.setSubmissionFeedbackList(new FeedbackListType());

        // Erstelle die TestsResponseType-Instanz und setze sie
        TestsResponseType testsResponse = new TestsResponseType();
        separateTestFeedback.setTestsResponse(testsResponse);

        // Erforderliche Felder files und responseMetaData setzen
        response.setFiles(new ResponseFilesType());
        ResponseMetaDataType metaData = new ResponseMetaDataType();
        DatatypeFactory datatypeFactory = null;
		try {
			datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        XMLGregorianCalendar xmlDateTime = datatypeFactory.newXMLGregorianCalendar(
            ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
        metaData.setResponseDatetime(xmlDateTime);
        GraderEngineType graderEngine = new GraderEngineType();
        graderEngine.setName("UMLGrader");
        graderEngine.setVersion("1.0");
        metaData.setGraderEngine(graderEngine);
        response.setResponseMetaData(metaData);

        // Mapping von Kriterien zu festen Test-IDs (test1 bis test10)
        List<String> criteriaMapping = new ArrayList<>();
        criteriaMapping.add("checkAttributeNameConvention");    // test1
        criteriaMapping.add("checkMethodNameConvention");       // test2
        criteriaMapping.add("checkClassNameConvention");        // test3
        criteriaMapping.add("checkAttributesArePrivate");       // test4
        criteriaMapping.add("checkGettersForPrivateAttributes"); // test5
        criteriaMapping.add("checkSettersForPrivateAttributes"); // test6
        criteriaMapping.add("checkConstructorPresenceAndConvention"); // test7
        criteriaMapping.add("checkAssociationConsistency");     // test8
        criteriaMapping.add("checkMinimumAttributesOrMethods"); // test9
        criteriaMapping.add("checkMinimumOneClass");            // test10

        // Prüfe jedes Kriterium auf Erfüllung und füge Feedback hinzu
        List<String> warnings = (evaluationResult != null) ? evaluationResult.getWarnings() : new ArrayList<>();
        List<String> errors = (evaluationResult != null) ? evaluationResult.getErrors() : new ArrayList<>();

        for (int i = 0; i < criteriaMapping.size(); i++) {
            String criterion = criteriaMapping.get(i);
            String testId = "test" + (i + 1);
            double weight = criteriaWithWeights.stream()
                    .filter(cw -> cw.criterion.equals(criterion))
                    .findFirst()
                    .map(cw -> cw.weight)
                    .orElse(0.5); // Standardgewicht, falls nicht gefunden

            String feedbackMessage = "";
            boolean criterionFulfilled = true;

            // Prüfe basierend auf dem Kriterium
            switch (criterion) {
                case "checkAttributeNameConvention":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Attribut") && w.contains("sollte mit einem Kleinbuchstaben beginnen"));
                    feedbackMessage = criterionFulfilled ? "Alle Attributnamen beginnen korrekt mit Kleinbuchstaben." :
                            "Einige Attributnamen beginnen nicht mit Kleinbuchstaben.";
                    break;
                case "checkMethodNameConvention":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Methode") && w.contains("sollte mit einem Kleinbuchstaben beginnen"));
                    feedbackMessage = criterionFulfilled ? "Alle Methodennamen entsprechen den Konventionen." :
                            "Einige Methodennamen entsprechen nicht den Konventionen.";
                    break;
                case "checkClassNameConvention":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Klasse") && w.contains("sollte mit einem Großbuchstaben beginnen"));
                    feedbackMessage = criterionFulfilled ? "Alle Klassennamen beginnen korrekt mit Großbuchstaben." :
                            "Einige Klassennamen beginnen nicht mit Großbuchstaben.";
                    break;
                case "checkAttributesArePrivate":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Attribut") && w.contains("ist nicht privat"));
                    feedbackMessage = criterionFulfilled ? "Alle Attribute sind privat." : "Einige Attribute sind nicht privat.";
                    break;
                case "checkGettersForPrivateAttributes":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Privates Attribut") && w.contains("hat keinen Getter"));
                    feedbackMessage = criterionFulfilled ? "Alle privaten Attribute haben Getter." : "Einige private Attribute haben keine Getter.";
                    break;
                case "checkSettersForPrivateAttributes":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Privates Attribut") && w.contains("hat keinen Setter"));
                    feedbackMessage = criterionFulfilled ? "Alle privaten Attribute haben Setter." : "Einige private Attribute haben keine Setter.";
                    break;
                case "checkConstructorPresenceAndConvention":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("Konstruktor") || w.contains("hat keinen Konstruktor"));
                    feedbackMessage = criterionFulfilled ? "Alle Klassen haben einen korrekten Konstruktor." :
                            "Einige Klassen haben keinen oder einen inkorrekten Konstruktor.";
                    break;
                case "checkAssociationConsistency":
                    criterionFulfilled = !errors.stream().anyMatch(e -> e.contains("Assoziation") && e.contains("verweist auf nicht existierende Klasse"));
                    feedbackMessage = criterionFulfilled ? "Alle Assoziationen sind konsistent." : "Einige Assoziationen sind nicht konsistent.";
                    break;
                case "checkMinimumAttributesOrMethods":
                    criterionFulfilled = !warnings.stream().anyMatch(w -> w.contains("hat weder Attribute noch Methoden"));
                    feedbackMessage = criterionFulfilled ? "Alle Klassen haben mindestens ein Attribut oder eine Methode." :
                            "Einige Klassen haben weder Attribute noch Methoden.";
                    break;
                case "checkMinimumOneClass":
                    criterionFulfilled = !errors.stream().anyMatch(e -> e.contains("Modell enthält keine Klassen"));
                    feedbackMessage = criterionFulfilled ? "Das Modell enthält mindestens eine Klasse." : "Das Modell enthält keine Klassen.";
                    break;
                default:
                    continue; // Überspringe unbekannte Kriterien
            }

            // Erstelle TestResponse basierend auf dem Ergebnis
            TestResponseType testResponse = new TestResponseType();
            testResponse.setId(testId); // Festes ID-Format: test1 bis test10

            TestResultType testResult = new TestResultType();
            ResultType result = new ResultType();
            result.setScore(BigDecimal.valueOf(criterionFulfilled ? weight : 0.0));
            testResult.setResult(result);

            FeedbackListType feedbackList = new FeedbackListType();
            FeedbackType feedback = new FeedbackType();
            FeedbackType.Content content = new FeedbackType.Content();
            content.setValue(feedbackMessage);
            content.setFormat("plaintext");
            feedback.setContent(content);
            feedbackList.getStudentFeedback().add(feedback);
            testResult.setFeedbackList(feedbackList);

            testResponse.setTestResult(testResult);
            testsResponse.getTestResponse().add(testResponse);
        }

        // Fehler als separate Test-Responses (optional, hier auskommentiert)
        /*
        int testIndex = 11;
        for (String error : errors) {
            TestResponseType testResponse = new TestResponseType();
            testResponse.setId("error-" + testIndex);
            TestResultType testResult = new TestResultType();
            ResultType result = new ResultType();
            result.setScore(BigDecimal.ZERO);
            testResult.setResult(result);
            FeedbackListType feedbackList = new FeedbackListType();
            FeedbackType feedback = new FeedbackType();
            FeedbackType.Content content = new FeedbackType.Content();
            content.setValue(error);
            feedback.setContent(content);
            feedbackList.getStudentFeedback().add(feedback);
            testResult.setFeedbackList(feedbackList);
            testResponse.setTestResult(testResult);
            testsResponse.getTestResponse().add(testResponse);
            testIndex++;
        }
        */

        return response;
    }

}