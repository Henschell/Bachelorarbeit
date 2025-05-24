package proformA;

import javax.xml.bind.annotation.*;

/**
 * Die Klasse {@code Submission} repräsentiert das Wurzelelement einer ProFormA-konformen {@code submission.xml}-Datei.
 * Sie wird verwendet, um die Abgabe eines Studenten (z. B. eine XMI-Datei) in einem ProFormA-Format zu strukturieren,
 * das von Lernplattformen wie Moodle verarbeitet werden kann. Diese Klasse ist mit JAXB-Annotationen versehen, um die
 * Serialisierung in XML und das Parsen aus XML zu ermöglichen.
 *
 * <p>Die Klasse enthält eine eindeutige ID für die Abgabe und eine Liste von Dateien ({@code SubmissionFiles}),
 * die die eigentliche Abgabe (z. B. eine Base64-codierte XMI-Datei) enthalten.</p>
 *
 * @see SubmissionFiles
 * @see EmbeddedBinFile
 * @see EmbeddedTxtFile
 */
@XmlRootElement(name = "submission", namespace = "urn:proforma:v2.1")
@XmlAccessorType(XmlAccessType.FIELD)
public class Submission {
    /** Die eindeutige ID der Abgabe, z. B. "submission_123". */
    @XmlElement(namespace = "urn:proforma:v2.1")
    private String id;

    /** Die Liste der Dateien, die die Abgabe enthalten (z. B. eine XMI-Datei). */
    @XmlElement(namespace = "urn:proforma:v2.1")
    private SubmissionFiles files;

    // Getter und Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public SubmissionFiles getFiles() { return files; }
    public void setFiles(SubmissionFiles files) { this.files = files; }
}

/**
 * Die Klasse {@code SubmissionFiles} repräsentiert das {@code <files>}-Element in einer ProFormA-konformen
 * {@code submission.xml}-Datei. Sie enthält die eingebetteten Dateien der Abgabe, aufgeteilt in ein
 * {@code EmbeddedBinFile} (für den Dateinamen) und ein {@code EmbeddedTxtFile} (für den Base64-codierten Inhalt).
 *
 * @see EmbeddedBinFile
 * @see EmbeddedTxtFile
 */
@XmlAccessorType(XmlAccessType.FIELD)
class SubmissionFiles {
    /** Das Element, das den Dateinamen der eingebetteten Datei enthält. */
    @XmlElement(name = "embedded-bin-file", namespace = "urn:proforma:v2.1")
    private EmbeddedBinFile embeddedBinFile;

    /** Das Element, das den Base64-codierten Inhalt der eingebetteten Datei enthält. */
    @XmlElement(name = "embedded-txt-file", namespace = "urn:proforma:v2.1")
    private EmbeddedTxtFile embeddedTxtFile;

    // Getter und Setter
    public EmbeddedBinFile getEmbeddedBinFile() { return embeddedBinFile; }
    public void setEmbeddedBinFile(EmbeddedBinFile embeddedBinFile) { this.embeddedBinFile = embeddedBinFile; }
    public EmbeddedTxtFile getEmbeddedTxtFile() { return embeddedTxtFile; }
    public void setEmbeddedTxtFile(EmbeddedTxtFile embeddedTxtFile) { this.embeddedTxtFile = embeddedTxtFile; }
}

/**
 * Die Klasse {@code EmbeddedBinFile} repräsentiert das {@code <embedded-bin-file>}-Element in einer ProFormA-konformen
 * {@code submission.xml}-Datei. Sie enthält Metadaten über die eingebettete Datei, wie den Dateinamen und die Speicherart.
 */
@XmlAccessorType(XmlAccessType.FIELD)
class EmbeddedBinFile {
    /** Die Speicherart der Datei, z. B. "embedded". */
    @XmlAttribute
    private String filestorage;

    /** Die ID der eingebetteten Datei, z. B. "uml_file". */
    @XmlAttribute
    private String id;

    /** Der Dateiname der eingebetteten Datei, z. B. "U07.xmi". */
    @XmlValue
    private String value;

    // Getter und Setter
    public String getFilestorage() { return filestorage; }
    public void setFilestorage(String filestorage) { this.filestorage = filestorage; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

/**
 * Die Klasse {@code EmbeddedTxtFile} repräsentiert das {@code <embedded-txt-file>}-Element in einer ProFormA-konformen
 * {@code submission.xml}-Datei. Sie enthält den Base64-codierten Inhalt der eingebetteten Datei.
 */
@XmlAccessorType(XmlAccessType.FIELD)
class EmbeddedTxtFile {
    /** Die ID des Elements, z. B. "uml_content". */
    @XmlAttribute
    private String id;

    /** Der Base64-codierte Inhalt der Datei. */
    @XmlValue
    private String content;

    // Getter und Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}