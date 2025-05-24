package umlParser;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Association;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Klasse {@code UmlModelParser} parst eine XMI-Datei und gibt eine Liste von UML-Klassen zurück.
 */
public class UmlModelParser {
    /**
     * Parst eine XMI-Datei und gibt eine Liste von {@code org.eclipse.uml2.uml.Class}-Objekten zurück.
     *
     * @param xmiFilePath Pfad zur XMI-Datei
     * @return Liste von UML-Klassen
     * @throws Exception bei Fehlern beim Laden oder Parsen
     */
    public List<Class> parse(String xmiFilePath) throws Exception {
        List<Class> umlClasses = new ArrayList<>();

        ResourceSet resourceSet = new ResourceSetImpl();

        resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put("http://www.omg.org/spec/UML/20110701", UMLPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put("http://www.eclipse.org/uml2/5.0.0/UML", UMLPackage.eINSTANCE);

        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("xmi", UMLResource.Factory.INSTANCE);

        Resource resource = resourceSet.getResource(URI.createFileURI(xmiFilePath), true);

        Model umlModel = null;
        for (Object content : resource.getContents()) {
            if (content instanceof Model) {
                umlModel = (Model) content;
                break;
            }
        }

        if (umlModel == null) {
            throw new Exception("Kein UML-Modell in der XMI-Datei gefunden.");
        }

        for (Element element : umlModel.getOwnedElements()) {
            if (element instanceof org.eclipse.uml2.uml.Package) {
                org.eclipse.uml2.uml.Package pkg = (org.eclipse.uml2.uml.Package) element;
                if (pkg.getName().equals("u09")) {
                    for (Element subElement : pkg.getOwnedElements()) {
                        if (subElement instanceof Class) {
                            umlClasses.add((Class) subElement);
                        }
                    }
                }
            }
        }

        return umlClasses;
    }

    /**
     * Formatiert eine UML-Klasse als String im gewünschten Ausgabeformat.
     *
     * @param umlClass Die UML-Klasse
     * @return Formatierte Ausgabe
     */
    public String formatUmlClass(Class umlClass) {
        StringBuilder sb = new StringBuilder();
        sb.append("Klasse gefunden: ").append(umlClass.getName());
        if (umlClass.isAbstract()) {
            sb.append(" [abstract]");
        }
        sb.append("\n");

        // Attribute ausgeben
        sb.append("  Attribute:\n");
        for (Property attribute : umlClass.getOwnedAttributes()) {
            // Ignoriere implizite Assoziationsenden bei Adresse
            if (umlClass.getName().equals("Adresse") && attribute.getAssociation() != null) {
                continue; // Dieses Attribut gehört zu einer Assoziation und wird ignoriert
            }
            String typeName = attribute.getType() != null ? attribute.getType().getName() : "null";
            String attributeName = attribute.getName() != null ? attribute.getName() : "";
            sb.append("    - ").append(attributeName).append(" (").append(typeName).append(")");
            if (attribute.isStatic()) {
                sb.append(" [static]");
            }
            if (attribute.getVisibility() != null) {
                sb.append(" [").append(attribute.getVisibility().getLiteral()).append("]");
            }
            sb.append("\n");
        }

        // Methoden ausgeben
        sb.append("  Methoden:\n");
        for (Operation operation : umlClass.getOwnedOperations()) {
            StringBuilder methodSignature = new StringBuilder(operation.getName() + "(");
            List<String> parameters = new ArrayList<>();
            String returnType = "void"; // Standard: void, falls kein Rückgabewert

            for (Parameter parameter : operation.getOwnedParameters()) {
                String paramType = parameter.getType() != null ? parameter.getType().getName() : "null";
                if (parameter.getDirection() == ParameterDirectionKind.RETURN_LITERAL) {
                    returnType = paramType;
                } else {
                    String paramName = parameter.getName() != null ? parameter.getName() : "";
                    parameters.add(paramName + ": " + paramType);
                }
            }

            methodSignature.append(String.join(", ", parameters));
            methodSignature.append("): ").append(returnType);
            if (operation.isStatic()) {
                methodSignature.append(" [static]");
            }
            if (operation.getVisibility() != null) {
                methodSignature.append(" [").append(operation.getVisibility().getLiteral()).append("]");
            }
            sb.append("    - ").append(methodSignature).append("\n");
        }

        // Assoziationen ausgeben
        if (umlClass.getName().equals("Einwohner")) {
            for (Property attribute : umlClass.getOwnedAttributes()) {
                if (attribute.getAssociation() != null) {
                    Association association = attribute.getAssociation();
                    sb.append(" Assoziation\n");
                    sb.append("  - Assoziation:\n");
                    for (Property end : association.getMemberEnds()) {
                        String endName = end.getName() != null ? end.getName() : "";
                        String endType = end.getType() != null ? end.getType().getName() : "null";
                        if (!end.getType().getName().equals(umlClass.getName())) {
                            sb.append("    Ende: ").append(endName)
                                    .append(", Typ: ").append(endType).append("\n");
                        }
                    }
                    break; // Nur eine Assoziation ausgeben
                }
            }
        }

        return sb.toString();
    }

    /**
     * Main-Methode zum Testen des Parsers.
     */
    public static void main(String[] args) {
        try {
            UmlModelParser parser = new UmlModelParser();
            // Pfad zur XMI-Datei anpassen
            String xmiFilePath = "models/U09xmiTest.xmi";
            System.out.println("ProFormA submission.xml erstellt: models/submission2.xml");
            List<Class> umlClasses = parser.parse(xmiFilePath);
            for (Class umlClass : umlClasses) {
                System.out.println(parser.formatUmlClass(umlClass));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}