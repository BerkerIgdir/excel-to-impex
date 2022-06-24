package regextest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class RegexTest {
    private static final String KEY_REGEX = "([^A-Z].*(\\..*))+[^\\.]";
    public static final String KEY_REGEX_OLD = "[^A-Z].*(\\..*)+[^\\.]";

    private static final String KEY_RECTIFIER_REGEX = "^[a-z].*[a-z]";
    private static final List<String> stringsToTest = List.of("; newsletter.management2.unsubscribe.name =",
            "A normal sentence.",
            "a sentence that shouldnt pass the test.",
            "anormal.sentence",
            "button.genericConfigurableForm.updateProfileButton2.text ;  ");

    private static final Pattern pattern = Pattern.compile(KEY_REGEX);
    private static final Pattern patternOld = Pattern.compile(KEY_REGEX_OLD);
    private static final Pattern rectifierPattern = Pattern.compile(KEY_RECTIFIER_REGEX);

    @Test
    void regexTest() {
        var resultList = stringsToTest.stream()
                .filter(s -> patternOld.matcher(s).matches())
                .collect(Collectors.toList());

        Assertions.assertTrue(resultList.contains("anormal.sentence"));
        Assertions.assertTrue(resultList.contains("; newsletter.management2.unsubscribe.name ="));
        Assertions.assertTrue(resultList.contains("button.genericConfigurableForm.updateProfileButton2.text ;  "));

        Assertions.assertFalse(resultList.contains("A normal sentence."));
        Assertions.assertFalse(resultList.contains("a sentence that shouldnt pass the test."));
    }

    @Test
    void isContainUpperCase() {
        var string = "button.genericConfigurableForm.updateProfileButton2.text ;  ";
        var lowerCaseString = "aaaaaaa";
        Assertions.assertFalse(string.matches("\\w+"));
        Assertions.assertTrue(StandardCharsets.US_ASCII.newEncoder().canEncode(string));
        Assertions.assertTrue(string.chars().noneMatch(Character::isUpperCase));
        Assertions.assertTrue(lowerCaseString.chars().noneMatch(Character::isUpperCase));
    }

    @Test
    void nonLatinWordsRegexTest() {
        var asianString = "주소가 성공적으로 업데이트됐습니다. ";
        var greekString = "Η διεύθυνσή σας ενημερώθηκε με επιτυχία. ";
        Assertions.assertFalse(StandardCharsets.US_ASCII.newEncoder().canEncode(greekString));
        Assertions.assertFalse(greekString.matches("\\w+"));
        Assertions.assertFalse(asianString.matches("\\w+"));
        Assertions.assertTrue(pattern.matcher(greekString).matches());
    }

    @Test
    void rectifierRegexTest() {
        var stringToTest = "; customer.interest.save.changes =";
        var spacesTrimmed = stringToTest.replaceAll("\\s", "");
        Assertions.assertFalse(rectifierPattern.matcher(spacesTrimmed).matches());
    }

    @Test
    void rectifierLoopTest() {
        var stringToTest = "; customer.interest.save.changes =";
        var spacesTrimmed = stringToTest.replaceAll("\\s", "");
        char firstLetter = spacesTrimmed.charAt(0);
        int beginningIndex = 1;
        while (firstLetter < 'a' || firstLetter > 'z') {
            spacesTrimmed = spacesTrimmed.substring(beginningIndex);
            firstLetter = spacesTrimmed.charAt(0);
        }
        int endingIndex = spacesTrimmed.length() - 1;
        char lastLetter = spacesTrimmed.charAt(endingIndex);
        while (lastLetter < 'a' || lastLetter > 'z') {
            spacesTrimmed = spacesTrimmed.substring(0, endingIndex--);
            lastLetter = spacesTrimmed.charAt(endingIndex);
        }
        Assertions.assertTrue(rectifierPattern.matcher(spacesTrimmed).matches());
    }
}
