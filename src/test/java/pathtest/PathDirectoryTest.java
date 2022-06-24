package pathtest;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class PathDirectoryTest {
    private static final String RESOURCE_DIRECTORY = "/Users/berkerigdir/ExcelToMaven/src/main/resources/translations.impex";
    private static final Path path = Path.of(RESOURCE_DIRECTORY);

    @Test
    void simplePathOperations(){
        System.out.println(path.getParent());
        System.out.println(path.getFileName());
        int index = path.getFileName().toString().indexOf(".");
        var fileNameWithoutExtension = path.getFileName().toString().substring(0,index);
        System.out.println(fileNameWithoutExtension);
    }
}
