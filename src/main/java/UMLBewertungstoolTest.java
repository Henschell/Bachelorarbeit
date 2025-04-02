import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.UMLPackage;

public class UMLBewertungstoolTest {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Bitte geben Sie den Pfad zur XMI-Datei an.");
            return;
        }
        String xmiFilePath = args[0];
        try {
            // ResourceSet für EMF erstellen
            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("xmi", UMLResource.Factory.INSTANCE);

            // XMI-Datei laden
            Resource resource = resourceSet.getResource(URI.createFileURI(xmiFilePath), true);
            Model umlModel = (Model) resource.getContents().get(0);

            // Beispiel: Modellname ausgeben
            System.out.println("Geladenes UML-Modell: " + umlModel.getName());
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der XMI-Datei: " + e.getMessage());
            e.printStackTrace();
        }
    }
}