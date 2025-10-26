package zzzank.probejs.utils;

import lombok.val;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NameUtils {
    public static final Set<String> TS_KEYWORDS = Set.of(
        ("abstract,arguments,"
        + "boolean,break,byte,"
        + "case,catch,char,const,continue,constructor,"
        + "debugger,default,delete,do,double,"
        + "else,eval,export,"
        + "false,final,finally,float,for,function,"
        + "goto,"
        + "if,implements,in,instanceof,int,interface,"
        + "let,long,"
        + "native,new,null,"
        + "package,private,protected,public,"
        + "return,"
        + "short,static,switch,synchronized,"
        + "this,throw,throws,transient,true,try,typeof,"
        + "var,void,volatile,"
        + "while,with,"
        + "yield").split(","));
    public static final Pattern MATCH_JS_IDENTIFIER = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");
    public static final Pattern MATCH_IMPORT = Pattern.compile("^import \\{(.+)} from (.+)");
    public static final Pattern MATCH_CONST_REQUIRE = Pattern.compile("^const \\{(.+)} = require\\((.+)\\)");
    public static final Pattern MATCH_ANY_REQUIRE = Pattern.compile("^.+ \\{(.+)} = require\\((.+)\\)");
    public static final Pattern MATCH_LINE_BREAK = Pattern.compile("\n");
    public static final UnaryOperator<String> LOWER_CASE = s -> s.toLowerCase(Locale.ROOT);

    public static String[] extractAlphabets(String input) {
        return input.split("[^a-zA-Z]+");
    }

    public static String asCamelCase(String[] words) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                result.append(i == 0
                    ? Character.toLowerCase(word.charAt(0))
                    : Character.toUpperCase(word.charAt(0))
                );
                result.append(word.substring(1));
            }
        }
        return result.toString();
    }

    public static String firstLower(String word) {
        if (word.isEmpty() || !Character.isLetter(word.charAt(0))) {
            return word;
        }
        return Character.toLowerCase(word.charAt(0)) + word.substring(1);
    }

    public static String[] resourceLocationToPath(String resourceLocation) {
        return resourceLocation.split("/");
    }

    public static String finalComponentToTitle(String resourceLocation) {
        val path = resourceLocationToPath(resourceLocation);
        val last = path[path.length - 1];
        return Arrays.stream(last.split("_")).map(NameUtils::getCapitalized).collect(Collectors.joining());
    }

    public static String rlToTitle(String s) {
        return Arrays.stream(s.split("/")).map(NameUtils::snakeToTitle).collect(Collectors.joining());
    }

    public static boolean isTSIdentifier(String s) {
        return MATCH_JS_IDENTIFIER.matcher(s).matches();
    }

    public static boolean isNameSafe(String s) {
        return !TS_KEYWORDS.contains(s) && isTSIdentifier(s);
    }

    public static String getCapitalized(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String snakeToTitle(String s) {
        return Arrays.stream(s.split("_")).map(NameUtils::getCapitalized).collect(Collectors.joining());
    }

    public static String replaceRegion(String str, int start, int end, String oldText, String newText) {
        if (start < 0 || start >= end || end > str.length()) {
            throw new IllegalArgumentException("Invalid start or end index");
        }

        val prefix = str.substring(0, start);
        val region = str.substring(start, end);
        val suffix = str.substring(end);

        val replacedRegion = region.replace(oldText, newText);

        return prefix + replacedRegion + suffix;
    }

    public static String cutOffStartEnds(String str, List<int[]> startEnds) {
        val result = new StringBuilder(str);

        // Iterate over the pairs in reverse order
        for (int i = startEnds.size() - 1; i >= 0; i--) {
            val startEnd = startEnds.get(i);
            // Cut off the substring from start to end (exclusive)
            result.delete(startEnd[0], startEnd[1]);
        }

        return result.toString();
    }
}
