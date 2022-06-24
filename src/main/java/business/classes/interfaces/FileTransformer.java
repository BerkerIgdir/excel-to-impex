package business.classes.interfaces;

import org.apache.poi.ss.usermodel.Workbook;

public interface FileTransformer<T extends Workbook> {
    void transform();
}
