package proformA;

import proforma.xml21.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class ResponseXmlGenerator {

    public static class TestResult {
        private final String testId; // ID des übergeordneten Tests (z. B. "test1" oder "test2")
        private final String subtestId; // ID des Untertests (z. B. "testMinimumOneClass")
        private final boolean passed;
        private final String feedback;
        private final double weight;

        public TestResult(String testId, String subtestId, boolean passed, String feedback, double weight) {
            this.testId = testId;
            this.subtestId = subtestId;
            this.passed = passed;
            this.feedback = feedback;
            this.weight = weight;
        }

        public String getTestId() { return testId; }
        public String getSubtestId() { return subtestId; }
        public boolean isPassed() { return passed; }
        public String getFeedback() { return feedback; }
        public double getWeight() { return weight; }
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

        // Gruppiere die TestResults nach testId
        Map<String, List<TestResult>> groupedResults = testResults.stream()
                .collect(Collectors.groupingBy(TestResult::getTestId));

        // Definiere die gewünschte Reihenfolge der testIds
        List<String> preferredOrder = Arrays.asList("test1", "test2"); // test1 zuerst, dann test2

        // Erstelle test-response-Elemente in der gewünschten Reihenfolge
        for (String testId : preferredOrder) {
            if (groupedResults.containsKey(testId)) {
                List<TestResult> resultsForTest = groupedResults.get(testId);

                TestResponseType testResponse = new TestResponseType();
                testResponse.setId(testId);

                SubtestsResponseType subtestsResponse = new SubtestsResponseType();
                testResponse.setSubtestsResponse(subtestsResponse);

                // Erstelle subtest-response-Elemente für jeden Untertest
                for (TestResult testResult : resultsForTest) {
                    SubtestResponseType subtestResponse = new SubtestResponseType();
                    subtestResponse.setId(testResult.getSubtestId());

                    TestResultType testResultType = new TestResultType();
                    ResultType result = new ResultType();
                    result.setScore(BigDecimal.valueOf(testResult.isPassed() ? testResult.getWeight() : 0.0));
                    testResultType.setResult(result);

                    FeedbackListType feedbackList = new FeedbackListType();
                    FeedbackType feedback = new FeedbackType();
                    FeedbackType.Content content = new FeedbackType.Content();
                    content.setValue(testResult.getFeedback());
                    content.setFormat("plaintext");
                    feedback.setContent(content);
                    feedbackList.getStudentFeedback().add(feedback);
                    testResultType.setFeedbackList(feedbackList);

                    subtestResponse.setTestResult(testResultType);
                    subtestsResponse.getSubtestResponse().add(subtestResponse);
                }

                testsResponse.getTestResponse().add(testResponse);
            }
        }

        return response;
    }
}