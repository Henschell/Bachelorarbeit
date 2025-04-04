import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.*;
import org.eclipse.uml2.uml.Class;

public class UMLBewertungstoolTest {
    public static void main(String[] args) {
//        if (args.length < 1) {
//            System.out.println("Bitte geben Sie den Pfad zur XMI-Datei an.");
//            return;
//        }
//        String xmiFilePath = args[0];
    	String xmiFilePath = "models/U07.xmi";
        try {
            // ResourceSet für EMF erstellen
            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("xmi", UMLResource.Factory.INSTANCE);
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
            .put("uml", UMLResource.Factory.INSTANCE);

            // XMI-Datei laden
            Resource resource = resourceSet.getResource(URI.createFileURI(xmiFilePath), true);
            Model umlModel = (Model) resource.getContents().get(0);

            // Beispiel: Modellname ausgeben
            System.out.println("Geladenes UML-Modell: " + umlModel.getName());
            
         // Alle Klassen im Modell durchsuchen
            for (Element element : umlModel.getOwnedElements()) {
                if (element instanceof Class) {
                    Class umlClass = (Class) element;
                    System.out.println("Klasse gefunden: " + umlClass.getName());

                    // Attribute auslesen
                    System.out.println("  Attribute:");
                    for (Property attribute : umlClass.getOwnedAttributes()) {
                        System.out.println("    - " + attribute.getName() + " (" + attribute.getType().getName() + ")");
                    }

                    // Methoden (Operationen) auslesen
                    System.out.println("  Methoden:");
                    for (Operation operation : umlClass.getOwnedOperations()) {
                        System.out.println("    - " + operation.getName() + "()");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der XMI-Datei: " + e.getMessage());
            e.printStackTrace();
        }
    }
}