//
//import java.util.List;
//import java.io.File;
//
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.Marshaller;
//
//import org.eclipse.uml2.uml.Class;
//
//import proformA.ResponseXmlGenerator;
//import proformA.SubmissionXmlGenerator;
//import proforma.xml21.FeedbackType;
//import proforma.xml21.ResponseType;
//import proforma.xml21.TestResponseType;
//import proforma.xml21.TestResultType;
//import umlParser.UmlModelParser;
//
//
//public class UMLBewertungstoolTest {
//    public static void main(String[] args) {
//        String xmiFilePath = "models/U09xmiTest.xmi";
//        String submissionXmlPath = "models/submission2.xml";
//
//        try {
//            // Schritt 1: XMI-Datei in submission.xml einbetten (für Testzwecke)
//            SubmissionXmlGenerator submissionGenerator = new SubmissionXmlGenerator();
//            submissionGenerator.createSubmissionXml(xmiFilePath, submissionXmlPath);
//            
//            
//            UmlModelParser parser = new UmlModelParser();
//            List<org.eclipse.uml2.uml.Class> umlClasses = parser.parse(xmiFilePath);
//            for (Class umlClass : umlClasses) {
//                System.out.println(parser.formatUmlClass(umlClass));
//            }
//            
//            // Generiere die Response basierend auf den allgemeinen Prüfkriterien
//            ResponseXmlGenerator generator = new ResponseXmlGenerator();
//            ResponseType response = generator.generateResponse(umlClasses);
//            
//            // Schreibe die Response in eine XML-Datei
//            JAXBContext context = JAXBContext.newInstance(ResponseType.class);
//            Marshaller marshaller = context.createMarshaller();
//            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            marshaller.marshal(response, new File("models/response.xml"));
//            
//            // Ausgabe zur Überprüfung
//            for (TestResponseType testResponse : response.getSeparateTestFeedback().getTestsResponse().getTestResponse()) {
//            	System.out.println("Test: " + testResponse.getId());
//            	TestResultType testResult = testResponse.getTestResult();
//            	if (testResult != null && testResult.getFeedbackList() != null) {
//            		for (FeedbackType feedback : testResult.getFeedbackList().getStudentFeedback()) {
//            			System.out.println("Feedback: " + feedback.getContent().getValue());
//            		}
//            	}
//            }
//        } catch (Exception e) {
//            System.err.println("Fehler: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}