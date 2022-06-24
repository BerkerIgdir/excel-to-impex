package business.classes;

import business.classes.exceptions.WrongPathTypeException;
import business.classes.interfaces.FileTransformer;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BatchTransformHandler implements FileTransformer<XSSFWorkbook> {
    private final String resourceDirectory;

    public BatchTransformHandler(String resourceDirectory) {
        if (!Files.isDirectory(Path.of(resourceDirectory))) {
            throw new WrongPathTypeException(WrongPathTypeException.NOT_DIRECTORY_FILE);
        }

        this.resourceDirectory = resourceDirectory;
    }

    @Override
    public void transform() {
        try (var pathStream = Files.list(Path.of(resourceDirectory))) {
            var targetPath = Files.createDirectory(createTargetDirectory());
            var excelFiles = pathStream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".xlsx"))
                    .map(XLSXTransformer::new)
                    .map(xlsxTransformer -> startTheThreads(xlsxTransformer, targetPath))
                    .collect(Collectors.toList());

            excelFiles.forEach(CompletableFuture::join);
        } catch (IOException e) {
            throw new WrongPathTypeException(e);
        }
    }

    private Path createTargetDirectory() {
        return Path.of(resourceDirectory.concat("impexes"));
    }

    private CompletableFuture<Void> startTheThreads(XLSXTransformer transformer, Path targetPath) {
        var future = CompletableFuture.runAsync(() -> transformer.transform(targetPath.toString()));
        future.exceptionally(throwable -> {
            System.out.println("A problem occured during the processing of the document: " + transformer.getWorkbookName());
            throwable.printStackTrace();
            return Void.TYPE.cast(0);
        });
        return future;
    }
}
