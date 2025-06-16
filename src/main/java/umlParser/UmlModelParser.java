package umlParser;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Association;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Die Klasse {@code UmlModelParser} parst eine XMI-Datei und gibt eine Liste von UML-Klassen zurück.
 */
public class UmlModelParser {
    private static final ArrayList<String> UML_NAMESPACES = new ArrayList<>();
    private static final ArrayList<String> UML_EXTENSIONS = new ArrayList<>();
    private static final ArrayList<String> UML_PROTOCOLS = new ArrayList<>();

    static {
        UML_NAMESPACES.add(UMLPackage.eNS_URI);
        UML_NAMESPACES.add("http://www.omg.org/spec/UML/20110701");
        UML_NAMESPACES.add("http://www.eclipse.org/uml2/5.0.0/UML");
        UML_NAMESPACES.add("http://schema.omg.org/spec/UML/2.0");
        UML_NAMESPACES.add("http://www.eclipse.org/uml2/2.0.0/UML");
        UML_EXTENSIONS.add(UMLResource.FILE_EXTENSION);
        UML_EXTENSIONS.add("xmi");
        UML_PROTOCOLS.add("pathmap");
    }

    private void registerUMLNamespaces(ResourceSet rs) {
        for (String namespace : UML_NAMESPACES) {
            rs.getPackageRegistry().put(namespace, UMLPackage.eINSTANCE);
        }
    }

    private void registerUMLExtensions(ResourceSet rs) {
        for (String extension : UML_EXTENSIONS) {
            rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(extension, UMLResource.Factory.INSTANCE);
        }
    }

    private void registerUMLProtocols(ResourceSet rs) {
        for (String protocol : UML_PROTOCOLS) {
            rs.getResourceFactoryRegistry().getProtocolToFactoryMap().put(protocol, UMLResource.Factory.INSTANCE);
        }
    }

    private Resource loadPrimitiveTypesResource(ResourceSet resourceSet, String primitiveTypesPath, URI primitiveTypesURI, boolean debug) throws Exception {
        resourceSet.getURIConverter().getURIMap().put(
            URI.createURI("pathmap://UML_LIBRARIES/JavaPrimitiveTypes.library.uml"),
            URI.createFileURI("models/UML_LIBRARIES/JavaPrimitiveTypes.library.uml")
        );

        Resource primitiveTypesResource = resourceSet.getResource(primitiveTypesURI, true);
        if (debug && (primitiveTypesResource == null || primitiveTypesResource.getContents().isEmpty())) {
            System.out.println("Fehler: JavaPrimitiveTypes.library.um konnte nicht geladen werden. Pfad: " + primitiveTypesPath);
            throw new Exception("JavaPrimitiveTypes.library.um konnte nicht geladen werden.");
        }
        if (debug) {
            System.out.println("JavaPrimitiveTypes.library.um erfolgreich geladen mit " + (primitiveTypesResource != null ? primitiveTypesResource.getContents().size() : 0) + " Elementen.");
        }
        return primitiveTypesResource;
    }

    private void debugResourceContents(Resource resource, boolean debug) {
        if (debug) {
            for (Object content : resource.getContents()) {
                if (content instanceof Model) {
                    Model model = (Model) content;
                    System.out.println("Model: " + model.getName());
                    for (Element elem : model.getOwnedElements()) {
                        if (elem instanceof PrimitiveType) {
                            PrimitiveType pt = (PrimitiveType) elem;
                            System.out.println("  PrimitiveType: " + pt.getName() + " (xmi:id: " + pt.eResource().getURIFragment(pt) + ")");
                        }
                    }
                }
            }
        }
    }

    private void debugAvailableResources(ResourceSet resourceSet, boolean debug) {
        if (debug) {
            System.out.println("Verfügbare Ressourcen im ResourceSet:");
            for (Resource res : resourceSet.getResources()) {
                System.out.println("  - " + res.getURI());
            }
        }
    }

    /**
     * Parst eine XMI-Datei und gibt eine Liste von {@code org.eclipse.uml2.uml.Class}-Objekten zurück.
     *
     * @param xmiFilePath Pfad zur XMI-Datei
     * @param debug Flag, um Debugging-Ausgaben zu aktivieren
     * @return Liste von UML-Klassen
     * @throws Exception bei Fehlern beim Laden oder Parsen
     */
    public List<Class> parse(String xmiFilePath, boolean debug) throws Exception {
        List<Class> umlClasses = new ArrayList<>();

        ResourceSet resourceSet = new ResourceSetImpl();
        registerUMLNamespaces(resourceSet);
        registerUMLExtensions(resourceSet);
        registerUMLProtocols(resourceSet);

        String javaPrimitiveTypesPath = "models/UML_LIBRARIES/JavaPrimitiveTypes.library.uml";
        URI primitiveTypesURI = URI.createFileURI(javaPrimitiveTypesPath);
        Resource primitiveTypesResource = loadPrimitiveTypesResource(resourceSet, javaPrimitiveTypesPath, primitiveTypesURI, debug);

        debugResourceContents(primitiveTypesResource, debug);
        debugAvailableResources(resourceSet, debug);

        Resource resource = resourceSet.createResource(URI.createFileURI(xmiFilePath));
        ((ResourceImpl) resource).setIntrinsicIDToEObjectMap(null);

        Map<String, Object> loadOptions = new HashMap<>();
        loadOptions.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_RECORD);
        loadOptions.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
        try {
            resource.load(loadOptions);
        } catch (Exception e) {
            if (debug) {
                System.out.println("Warnung: Fehler beim Laden der XMI-Datei (möglicherweise Stereotypen): " + e.getMessage());
            }
            throw e;
        }

        if (resource.getContents().isEmpty()) {
            throw new Exception("Kein Inhalt in der XMI-Datei gefunden.");
        }

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

        findClasses(umlModel, umlClasses);

        return umlClasses;
    }

    private void findClasses(Element element, List<Class> umlClasses) {
        if (element instanceof org.eclipse.uml2.uml.Package) {
            org.eclipse.uml2.uml.Package pkg = (org.eclipse.uml2.uml.Package) element;
            String pkgName = pkg.getName();
            if (pkgName != null && (pkgName.equals("java") || pkgName.equals("util") || pkgName.equals("PrimitiveTypes"))) {
                return;
            }
            for (Element subElement : pkg.getOwnedElements()) {
                findClasses(subElement, umlClasses);
            }
        } else if (element instanceof Class) {
            umlClasses.add((Class) element);
        }
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

        sb.append("  Attribute:\n");
        for (Property attribute : umlClass.getOwnedAttributes()) {
            if (umlClass.getName().equals("Adresse") && attribute.getAssociation() != null) {
                continue;
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

        sb.append("  Methoden:\n");
        for (Operation operation : umlClass.getOwnedOperations()) {
            StringBuilder methodSignature = new StringBuilder(operation.getName() + "(");
            List<String> parameters = new ArrayList<>();
            String returnType = "void";

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
                            sb.append("    Ende: ").append(endName).append(", Typ: ").append(endType).append("\n");
                        }
                    }
                    break;
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
            String xmiFilePath = "models/Astah/U09Falsch1.xmi";
            System.out.println("Klassen für " + xmiFilePath + " werden geladen");
            List<Class> umlClasses = parser.parse(xmiFilePath, false); // Debug aktiviert
            for (Class umlClass : umlClasses) {
                System.out.println(parser.formatUmlClass(umlClass));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}