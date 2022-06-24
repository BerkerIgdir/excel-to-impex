package business.classes;

import business.classes.exceptions.WrongPathTypeException;
import business.classes.interfaces.FileTransformer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static business.constants.ImpexApplicationConstants.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public class XLSXTransformer implements FileTransformer<XSSFWorkbook> {

    public static final Pattern keyPattern = Pattern.compile(KEY_REGEX);
    private final Path resourceDirectory;
    private final String workbookName;

    public XLSXTransformer(String workbookDirectory) {
        this(Path.of(workbookDirectory));
    }

    public XLSXTransformer(Path workbookDirectory) {
        if (!Files.isRegularFile(workbookDirectory)) {
            throw new WrongPathTypeException(WrongPathTypeException.NOT_REGULAR_FILE);
        }
        this.workbookName = workbookDirectory.getFileName().toString();
        this.resourceDirectory = workbookDirectory;
    }

    @Override
    public void transform() {
        transform(resourceDirectory.getParent().toString());
    }


    public void transform(String path) {
        try (var outputStream = Files.newOutputStream(getImpexFilePath(path));
             var inputStream = Files.newInputStream(resourceDirectory);
             var workBook = WorkbookFactory.create(inputStream)) {

            var mergedTranslationKeyTranslationValuesMap = getMergedTranslationKeyTranslationValuesMap(workBook);
            if (mergedTranslationKeyTranslationValuesMap.isEmpty()) {
                return;
            }
            var impexHeader = getImpexHeader(mergedTranslationKeyTranslationValuesMap);
            var impexBody = getImpexBody(mergedTranslationKeyTranslationValuesMap);

            var impex = impexHeader.concat("\n").concat(impexBody);

            outputStream.write(impex.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path getImpexFilePath(String targetDirectory) {
        String fileNameWithoutExtension = getFileNameWithoutExtension();

        return Path.of(targetDirectory
                .concat("/")
                .concat(fileNameWithoutExtension)
                .concat(".impex"));
    }

    private String getFileNameWithoutExtension() {
        int index = resourceDirectory.getFileName().toString().indexOf(".");
        return resourceDirectory.getFileName()
                .toString()
                .substring(0, index);
    }

    private String getImpexBody(LinkedHashMap<Cell, Map<Cell, List<Cell>>> mergedTranslationKeyTranslationValuesMap) {
        return mergedTranslationKeyTranslationValuesMap.entrySet()
                .stream()
                .map(this::rowToString)
                .collect(Collectors.joining("\n"));
    }

    private String getImpexHeader(LinkedHashMap<Cell, Map<Cell, List<Cell>>> mergedTranslationKeyTranslationValuesMap) {
        var randomEntryToCreateImpexHeader = new ArrayList<>(mergedTranslationKeyTranslationValuesMap.values()
                .stream()
                .findFirst()
                .map(Map::keySet)
                .orElseThrow());

        return prepareImpexHeader(randomEntryToCreateImpexHeader);
    }

    private LinkedHashMap<Cell, Map<Cell, List<Cell>>> getMergedTranslationKeyTranslationValuesMap(Workbook workBook) {
        var sheet1 = workBook.getSheetAt(0);
        return StreamSupport.stream(sheet1.spliterator(), false)
                .flatMap(rw -> StreamSupport.stream(rw.spliterator(), false))
                .filter(this::isContainNonLatinOrUpperCase)
                .filter(cll -> keyPattern.matcher(cll.getStringCellValue()).matches())
                .map(this::createTranslationKeyTranslationValuesMap)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k2, LinkedHashMap::new));
    }

    private boolean isContainNonLatinOrUpperCase(Cell cell) {
        var stringValue = cell.getStringCellValue();
        return StandardCharsets.US_ASCII.newEncoder().canEncode(stringValue);
    }

    private String prepareImpexHeader(List<Cell> languageConstantCells) {
        var languages = languageConstantCells
                .stream()
                .map(Cell::getStringCellValue)
                .map(this::languageSymbolRectifier)
                .collect(Collectors.joining(";"));
        return IMPEX_HEADER_BEGINNING.concat(languages);
    }

    private String languageSymbolRectifier(String s) {
        if (s.compareToIgnoreCase("gr") == 0) {
            s = "el";
        }
        if (s.compareToIgnoreCase("cz") == 0) {
            s = "cs";
        }
        if (s.toLowerCase().contains("default")) {
            s = "en";
        }
        return IMPEX_LANG_STRING.replace("language", s);
    }

    private String rowToString(Map.Entry<Cell, Map<Cell, List<Cell>>> cellMapEntry) {
        var impexBuilder = new StringBuilder();
        var translationKey = translationKeyRectifier(cellMapEntry.getKey().getStringCellValue());

        impexBuilder.append(translationKey);
        impexBuilder.append(";");
        impexBuilder.append(getStringValuesAppended(cellMapEntry));
        return impexBuilder.toString();
    }

    private String getStringValuesAppended(Map.Entry<Cell, Map<Cell, List<Cell>>> cellMapEntry) {
        return cellMapEntry.getValue()
                .values()
                .stream()
                .flatMap(List::stream)
                .map(Cell::getStringCellValue)
                .map(this::translationStringRectifier)
                .collect(Collectors.joining(";"));
    }

    private String translationStringRectifier(String translation) {

        return translation.replace(';', ' ');
    }

    private Map<Cell, Map<Cell, List<Cell>>> createTranslationKeyTranslationValuesMap(Cell keyCell) {
        var map = StreamSupport.stream(keyCell.getRow().spliterator(), false)
                .filter(cell -> cell.getColumnIndex() > keyCell.getColumnIndex())
                .filter(cell -> !cell.getStringCellValue().isEmpty())
                .collect(groupingBy(cell -> cell.getRow()
                        .getSheet()
                        .getRow(0)
                        .getCell(cell.getColumnIndex()), LinkedHashMap::new, Collectors.toList()));

        return Map.of(keyCell, map);
    }

    private String translationKeyRectifier(String translationKey) {
        translationKey = translationKey.replaceAll("\\s", "");

        char firstLetter = translationKey.charAt(0);
        int beginningIndex = 1;
        while (firstLetter < 'a' || firstLetter > 'z') {
            translationKey = translationKey.substring(beginningIndex++);
            firstLetter = translationKey.charAt(0);
        }

        int endingIndex = translationKey.length() - 1;
        char lastLetter = translationKey.charAt(endingIndex);
        while (lastLetter < 'a' || lastLetter > 'z') {
            translationKey = translationKey.substring(0, endingIndex--);
            lastLetter = translationKey.charAt(endingIndex);
        }

        return ";" + translationKey;
    }

    public String getWorkbookName() {
        return this.workbookName;
    }
}
