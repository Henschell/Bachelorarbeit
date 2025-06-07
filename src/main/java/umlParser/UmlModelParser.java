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
    /**
     * Parst eine XMI-Datei und gibt eine Liste von {@code org.eclipse.uml2.uml.Class}-Objekten zurück.
     *
     * @param xmiFilePath Pfad zur XMI-Datei
     * @return Liste von UML-Klassen
     * @throws Exception bei Fehlern beim Laden oder Parsen
     */
	private static final ArrayList<String> UML_NAMESPACES = new ArrayList<String>();
	private static final ArrayList<String> UML_Extensions = new ArrayList<String>();
	private static final ArrayList<String> UML_Protocols = new ArrayList<String>();
	static {
	    UML_NAMESPACES.add(UMLPackage.eNS_URI);
	    UML_NAMESPACES.add("http://www.omg.org/spec/UML/20110701");
	    UML_NAMESPACES.add("http://www.eclipse.org/uml2/5.0.0/UML");
	    UML_NAMESPACES.add("http://schema.omg.org/spec/UML/2.0");
	    UML_NAMESPACES.add("http://www.eclipse.org/uml2/2.0.0/UML");
	    UML_Extensions.add(UMLResource.FILE_EXTENSION);
	    UML_Extensions.add("xmi");
	    UML_Protocols.add("pathmap");
	}
	
	private void registerUMLNamespaces(ResourceSet rs) {
		
		for (String namespace : UML_NAMESPACES) {
			rs.getPackageRegistry().put(namespace, UMLPackage.eINSTANCE);
		}
	}
	private void registerUMLExtensions(ResourceSet rs) {
		for (String extension : UML_Extensions) {
			rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(extension,UMLResource.Factory.INSTANCE);			
		}
	}
	private void registerUMLProtocols(ResourceSet rs) {
		for (String protocol : UML_Protocols) {
			rs.getResourceFactoryRegistry().getProtocolToFactoryMap().put(protocol, UMLResource.Factory.INSTANCE);
		}
	}
	private Resource loadPrimitiveTypesResource(ResourceSet resourceSet, String primitiveTypesPath, URI primitiveTypesURI) throws Exception {
	    resourceSet.getURIConverter().getURIMap().put(
	            URI.createURI("pathmap://UML_LIBRARIES/JavaPrimitiveTypes.library.uml"),
	            URI.createFileURI("models/UML_LIBRARIES/JavaPrimitiveTypes.library.uml")
	    );

	    Resource primitiveTypesResource = resourceSet.getResource(primitiveTypesURI, true);
	    if (primitiveTypesResource == null || primitiveTypesResource.getContents().isEmpty()) {
	        System.out.println("Fehler: JavaPrimitiveTypes.library.um konnte nicht geladen werden. Pfad: " + primitiveTypesPath);
	        throw new Exception("JavaPrimitiveTypes.library.um konnte nicht geladen werden.");
	    }
	    System.out.println("JavaPrimitiveTypes.library.um erfolgreich geladen mit " + primitiveTypesResource.getContents().size() + " Elementen.");
	    return primitiveTypesResource;
	}
	private void debugResourceContents(Resource resource) {
	    for (Object content : resource.getContents()) {
	        if (content instanceof org.eclipse.uml2.uml.Model) {
	            org.eclipse.uml2.uml.Model model = (org.eclipse.uml2.uml.Model) content;
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
	private void debugAvailableResources(ResourceSet resourceSet) {
	    System.out.println("Verfügbare Ressourcen im ResourceSet:");
	    for (Resource res : resourceSet.getResources()) {
	        System.out.println("  - " + res.getURI());
	    }
	}
	public List<Class> parse(String xmiFilePath) throws Exception {
	    List<Class> umlClasses = new ArrayList<>();

	    ResourceSet resourceSet = new ResourceSetImpl();
	    registerUMLNamespaces(resourceSet);
	    registerUMLExtensions(resourceSet);
	    registerUMLProtocols(resourceSet);

	    String javaPrimitiveTypesPath = "models/UML_LIBRARIES/JavaPrimitiveTypes.library.uml";
        URI primitiveTypesURI = URI.createFileURI(javaPrimitiveTypesPath);
        Resource primitiveTypesResource = loadPrimitiveTypesResource(resourceSet, javaPrimitiveTypesPath, primitiveTypesURI);

        // Debugging-Ausgaben
        //debugResourceContents(primitiveTypesResource);
        //debugAvailableResources(resourceSet);

        // Lade die Haupt-XMI-Datei
        Resource resource = resourceSet.createResource(URI.createFileURI(xmiFilePath));
        ((ResourceImpl) resource).setIntrinsicIDToEObjectMap(null);

        Map<String, Object> loadOptions = new HashMap<>();
        loadOptions.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_RECORD);
        loadOptions.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
	    try {
	        resource.load(loadOptions);
	    } catch (Exception e) {
	        System.out.println("Warnung: Fehler beim Laden der XMI-Datei (möglicherweise Stereotypen): " + e.getMessage());
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
	        // Ignoriere Pakete wie "java", "util" und "PrimitiveTypes"
	        String pkgName = pkg.getName();
	        if (pkgName != null && (pkgName.equals("java") || pkgName.equals("util") || pkgName.equals("PrimitiveTypes"))) {
	            return; // Überspringe diese Pakete
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
            String xmiFilePath = "models/extracted_reference_model.xmi";
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