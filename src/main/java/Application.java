import business.classes.BatchTransformHandler;
import business.classes.XLSXTransformer;


public class Application {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("YOU MUST PROVIDE A DIRECTORY AS AN ARGUMENT!");
            return;
        } else if (args[0].equals("-b")) {
            var transformer = new BatchTransformHandler(args[1]);
            transformer.transform();
            return;
        }
        var transformer = new XLSXTransformer(args[0]);
        transformer.transform();
    }
}
