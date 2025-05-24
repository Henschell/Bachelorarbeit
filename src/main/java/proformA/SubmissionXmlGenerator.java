package proformA;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;

/**
 * Die Klasse {@code SubmissionXmlGenerator} ist dafür verantwortlich, eine XMI-Datei in eine ProFormA-konforme
 * {@code submission.xml}-Datei einzubetten. Sie wird primär für Testzwecke verwendet, um eine {@code submission.xml}
 * zu erstellen, die später in eine {@code submission.zip} gepackt werden kann, um die Eingabe von Moodle zu simulieren.
 *
 * <p>Die Klasse liest eine XMI-Datei ein, codiert ihren Inhalt in Base64 und erstellt eine {@code submission.xml},
 * die den Dateinamen und den Base64-codierten Inhalt der XMI-Datei enthält.</p>
 *
 * @see Submission
 * @see SubmissionFiles
 * @see EmbeddedBinFile
 * @see EmbeddedTxtFile
 */
public class SubmissionXmlGenerator {
    /**
     * Erstellt eine ProFormA-konforme {@code submission.xml}-Datei aus einer XMI-Datei.
     *
     * <p>Die Methode liest die XMI-Datei vom angegebenen Pfad, codiert ihren Inhalt in Base64 und erstellt eine
     * {@code submission.xml}, die den Dateinamen (im {@code <embedded-bin-file>}-Element) und den Base64-codierten
     * Inhalt (im {@code <embedded-txt-file>}-Element) enthält. Die resultierende XML-Datei wird am angegebenen
     * Ausgabepfad gespeichert.</p>
     *
     * @param xmiFilePath der Pfad zur XMI-Datei, die eingebettet werden soll
     * @param submissionXmlPath der Pfad, an dem die {@code submission.xml}-Datei gespeichert werden soll
     * @throws Exception wenn ein Fehler beim Lesen der XMI-Datei oder beim Schreiben der XML-Datei auftritt
     */
    public void createSubmissionXml(String xmiFilePath, String submissionXmlPath) throws Exception {
        File xmiFile = new File(xmiFilePath);
        byte[] xmiBytes = new byte[(int) xmiFile.length()];
        try (FileInputStream fis = new FileInputStream(xmiFile)) {
            fis.read(xmiBytes);
        }
        String xmiBase64 = Base64.getEncoder().encodeToString(xmiBytes);

        Submission submission = new Submission();
        submission.setId("submission_123");

        SubmissionFiles files = new SubmissionFiles();

        EmbeddedBinFile binFile = new EmbeddedBinFile();
        binFile.setFilestorage("embedded");
        binFile.setId("uml_file");
        binFile.setValue(xmiFile.getName());
        files.setEmbeddedBinFile(binFile);

        EmbeddedTxtFile txtFile = new EmbeddedTxtFile();
        txtFile.setId("uml_content");
        txtFile.setContent(xmiBase64);
        files.setEmbeddedTxtFile(txtFile);

        submission.setFiles(files);

        JAXBContext context = JAXBContext.newInstance(Submission.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(submission, new File(submissionXmlPath));

        System.out.println("ProFormA submission.xml erstellt: " + submissionXmlPath);
    }
    
    public static void main(String[] args) {
        try {
            String xmiPath = "models/U07.xmi"; 
            String outputPath = "models/submission.xml"; 

            SubmissionXmlGenerator generator = new SubmissionXmlGenerator();
            generator.createSubmissionXml(xmiPath, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}