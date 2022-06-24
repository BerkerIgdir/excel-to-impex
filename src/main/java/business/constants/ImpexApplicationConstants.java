package business.constants;

public class ImpexApplicationConstants {
    private ImpexApplicationConstants(){}
    public static final String KEY_REGEX = "[^A-Z].*(\\..*)+[^\\.]";
    public static final String IMPEX_HEADER_BEGINNING = "INSERT_UPDATE LocalizationEntry; code[unique=true];";
    public static final String IMPEX_LANG_STRING = "translation[lang=language]";
}
