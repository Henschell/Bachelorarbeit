package proformA;

import proforma.xml21.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class ResponseXmlGenerator {

    public static class TestResult {
        private final String testName;
        private final boolean passed;
        private final String failureMessage;
        private final double weight; // Gewichtung hinzugefügt

        public TestResult(String testName, boolean passed, String failureMessage, double weight) {
            this.testName = testName;
            this.passed = passed;
            this.failureMessage = failureMessage;
            this.weight = weight;
        }

        public String getTestName() {
            return testName;
        }

        public boolean isPassed() {
            return passed;
        }

        public String getFailureMessage() {
            return failureMessage;
        }

        public double getWeight() {
            return weight;
        }
    }

    private static class CriterionMapping {
        String testMethod;
        String successMessage;
        String failureMessage;

        CriterionMapping(String testMethod, String successMessage, String failureMessage) {
            this.testMethod = testMethod;
            this.successMessage = successMessage;
            this.failureMessage = failureMessage;
        }
    }

    public ResponseType generateResponse(List<TestResult> testResults) {
        ResponseType response = new ResponseType();
        response.setLang("de");

        SeparateTestFeedbackType separateTestFeedback = new SeparateTestFeedbackType();
        response.setSeparateTestFeedback(separateTestFeedback);

        separateTestFeedback.setSubmissionFeedbackList(new FeedbackListType());

        TestsResponseType testsResponse = new TestsResponseType();
        separateTestFeedback.setTestsResponse(testsResponse);

        response.setFiles(new ResponseFilesType());
        ResponseMetaDataType metaData = new ResponseMetaDataType();
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Fehler beim Erstellen des Datums: " + e.getMessage(), e);
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

        // Erstelle eine Liste von Kriterien-Mappings
        List<CriterionMapping> criteriaMapping = new ArrayList<>();
        criteriaMapping.add(new CriterionMapping("testMinimumOneClass",
                "Das Modell enthält mindestens eine Klasse.", "Das Modell enthält keine Klassen."));
        criteriaMapping.add(new CriterionMapping("testClassNameConvention",
                "Alle Klassennamen beginnen korrekt mit Großbuchstaben.", "Einige Klassennamen beginnen nicht mit Großbuchstaben."));
        criteriaMapping.add(new CriterionMapping("testAttributeNameConvention",
                "Alle Attributnamen beginnen korrekt mit Kleinbuchstaben.", "Einige Attributnamen beginnen nicht mit Kleinbuchstaben."));
        criteriaMapping.add(new CriterionMapping("testAssociationConsistency",
                "Alle Assoziationen sind konsistent.", "Einige Assoziationen sind nicht konsistent."));
        criteriaMapping.add(new CriterionMapping("testAttributesArePrivate",
                "Alle Attribute sind privat.", "Einige Attribute sind nicht privat."));
        criteriaMapping.add(new CriterionMapping("testGettersForPrivateAttributes",
                "Alle privaten Attribute haben Getter.", "Einige private Attribute haben keine Getter."));
        criteriaMapping.add(new CriterionMapping("testSettersForPrivateAttributes",
                "Alle privaten Attribute haben Setter.", "Einige private Attribute haben keine Setter."));
        criteriaMapping.add(new CriterionMapping("testMethodNameConvention",
                "Alle Methodennamen entsprechen den Konventionen.", "Einige Methodennamen entsprechen nicht den Konventionen."));
        criteriaMapping.add(new CriterionMapping("testConstructorPresenceAndConvention",
                "Alle Klassen haben einen korrekten Konstruktor.", "Einige Klassen haben keinen oder einen inkorrekten Konstruktor."));
        criteriaMapping.add(new CriterionMapping("testMinimumAttributesOrMethods",
                "Alle Klassen haben mindestens ein Attribut oder eine Methode.", "Einige Klassen haben weder Attribute noch Methoden."));

        // Erstelle ein einziges test-response-Element für test1
        TestResponseType testResponse = new TestResponseType();
        testResponse.setId("test1");

        // Erstelle ein subtests-response-Element
        SubtestsResponseType subtestsResponse = new SubtestsResponseType();
        testResponse.setSubtestsResponse(subtestsResponse);

        // Füge subtest-response-Elemente für jeden Untertest hinzu
        for (CriterionMapping mapping : criteriaMapping) {
            String testMethod = mapping.testMethod;
            String successMessage = mapping.successMessage;
            String failureMessage = mapping.failureMessage;

            TestResult testResult = testResults.stream()
                    .filter(result -> result.getTestName().equals(testMethod))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Testergebnis für " + testMethod + " nicht gefunden."));

            double weight = testResult.getWeight(); // Gewichtung direkt aus TestResult holen

            boolean criterionFulfilled = testResult.isPassed();
            String feedbackMessage = criterionFulfilled ? successMessage : failureMessage;

            if (!criterionFulfilled && !testResult.getFailureMessage().isEmpty()) {
                feedbackMessage += " Details: " + testResult.getFailureMessage();
            }

            // Erstelle ein subtest-response-Element für den Untertest
            SubtestResponseType subtestResponse = new SubtestResponseType();
            subtestResponse.setId(testMethod); // Verwende den sub-ref-Wert als ID

            TestResultType testResultType = new TestResultType();
            ResultType result = new ResultType();
            result.setScore(BigDecimal.valueOf(criterionFulfilled ? weight : 0.0));
            testResultType.setResult(result);

            FeedbackListType feedbackList = new FeedbackListType();
            FeedbackType feedback = new FeedbackType();
            FeedbackType.Content content = new FeedbackType.Content();
            content.setValue(feedbackMessage);
            content.setFormat("plaintext");
            feedback.setContent(content);
            feedbackList.getStudentFeedback().add(feedback);
            testResultType.setFeedbackList(feedbackList);

            subtestResponse.setTestResult(testResultType);
            subtestsResponse.getSubtestResponse().add(subtestResponse);
        }

        // Füge das test-response-Element zur tests-response hinzu
        testsResponse.getTestResponse().add(testResponse);

        return response;
    }
}