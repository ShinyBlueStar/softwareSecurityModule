package com.sample.system.ssm.service.domain.utility;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###");
    public static final String FARSI_LANGUAGE = "fa";
    public static final Locale FARSI_LOCALE = Locale.of(FARSI_LANGUAGE);
    public static final Locale ENGLISH_LOCALE = Locale.ENGLISH;
    private final static Pattern patternPersian = Pattern.compile("^[\\u0600-\\u06FF]+(?:[\\s0-9()،,-]+[\\u0600-\\u06FF]+)*$");


    public static String getLocaleText(Locale locale, String text) {

        if (text != null && locale != null && FARSI_LANGUAGE.equalsIgnoreCase(locale.getLanguage())) {
            StringBuffer sb;
            char c;

            sb = new StringBuffer();

            for (int i = 0; i < text.length(); i++) {
                c = text.charAt(i);
                if (c >= '0' && c <= '9') {
                    c = (char) (c + 0x6C0);
                }
                sb.append(c);
            }

            text = sb.toString();
        }

        return text;
    }

    public static String fixSomeWord(String statement) {
        if (Arrays.asList(statement.split(" ")).contains("نسده")) {
            return statement.replace("نسده", "نشده");
        }
        return statement;
    }

    public static boolean isMatched(String patternStr, String path) {
        Pattern pattern;

        patternStr = org.springframework.util.StringUtils.replace(patternStr, ".", "\\.");
        patternStr = org.springframework.util.StringUtils.replace(patternStr, "*", "+.*");
        pattern = Pattern.compile(patternStr);

        return pattern.matcher(path).matches();
    }

    public static String trimLeadingChars(String str, char ch) {
        String prunedString;
        int i;

        if (str == null) {
            return null;
        }

        prunedString = str;

        for (i = 0; i < str.length(); ++i) {
            if (str.charAt(i) != ch) {
                prunedString = str.substring(i, str.length());
                break;
            }
        }

        if (i == str.length()) {
            prunedString = String.valueOf(ch);
        }

        return prunedString;
    }

    public static boolean isNumber(String str) {

        if (str == null || "".equalsIgnoreCase(str)) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {

            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean hasText(String src) {
        if (src == null) {
            return false;
        }

        src = src.trim();

        return src.length() > 0;
    }

    public static boolean isPersianString(String str) {
        if (str == null || "".equalsIgnoreCase(str)) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) > 127) {
                return true;
            }
        }

        return false;
    }

    public static String fromPersianNumeric(String s) {
        s = org.springframework.util.StringUtils.replace(s, "\u06f0\u06f1\u06f2\u06f3\u06f4\u06f5\u06f6\u06f7\u06f8\u06f9\u066a", "0123456789%");
        s = org.springframework.util.StringUtils.replace(s, "\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669", "0123456789");
        return s;
    }

    public static String toPersianNumeric(String s) {
        /*
         * return StringUtils.replaceChars(s, "0123456789%",
         * "\u06f0\u06f1\u06f2\u06f3\u06f4\u06f5\u06f6\u06f7\u06f8\u06f9\u066a");
         */
        return org.springframework.util.StringUtils.replace(s, "0123456789", "\u06f0\u06f1\u06f2\u06f3\u06f4\u06f5\u06f6\u06f7\u06f8\u06f9");
    }

    public static String formatCardNumber(String str) {
        if (str == null || str.trim().length() == 0) {
            return "";
        }
        StringBuffer temp = new StringBuffer(str);
        int index = str.length();
        int offset = 0;
        for (int i = 0; i < index; i++) {
            if (i % 4 == 0 && i != 0) {
                temp.insert(i + offset, "-");
                offset++;
            }
        }
        return temp.toString();
    }

    public static String format(String value, int length) {
        StringBuffer result;

        result = new StringBuffer(value);

        while (result.length() < length) {
            result.insert(0, "0");
        }

        return result.toString();
    }

    public static String trim(String str, char ch) {
        String prunedString;
        int i;

        if (str == null) {
            return null;
        }

        prunedString = str;

        for (i = 0; i < str.length(); ++i) {
            if (str.charAt(i) != ch) {
                prunedString = str.substring(i, str.length());
                break;
            }
        }

        if (i == str.length()) {
            prunedString = String.valueOf(ch);
        }

        return prunedString;
    }

    protected static String getReplacement(Map model, Matcher aMatcher) throws Exception {
        String s;
        String beanName;
        Object value;

        s = aMatcher.group(0);
        s = s.substring(2, s.length() - 1);

        if (s.indexOf('.') == 0) {
            s = " " + s;
        }

        beanName = s;

        return ((value = model.get(beanName)) != null) ? value.toString() : "";
    }

    public static boolean equals(String[] items, String value) {
        for (String item : items) {
            if (!item.equals(value)) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(String s, String t) {

        if (s == null) {
            return t == null;
        }

        if (t == null) {
            return false;
        }

        s = s.trim();

        t = t.trim();

        return s.equals(t);
    }

    public static String nvl(String str) {

        if (str == null || str.equals("null")) {

            str = "";
        }

        return str;
    }

    public static String encode(String text) {

        if (!StringUtils.hasText(text)) {
            text = " ";
        }
        StringBuffer encoded = new StringBuffer();

        for (char c : text.toCharArray()) {
            encoded.append(encode(c));
        }
        return encoded.toString().length() > 25 ? encoded.toString().substring(0, 25) : encoded.toString();
    }

    private static String encode(char c) {
        return switch (c) {
            case '\u0627' -> "a";
            case '\u0622' -> "A";
            case '\u0628' -> "b";
            case '\u067e' -> "p";
            case '\u062a' -> "t";
            case '\u062b' -> "C";
            case '\u062c' -> "j";
            case '\u0686' -> "c";
            case '\u062d' -> "h";
            case '\u062e' -> "x";
            case '\u062f' -> "d";
            case '\u0630' -> "z";
            case '\u0631' -> "r";
            case '\u0632' -> "e";
            case '\u0698' -> "w";
            case '\u0633' -> "s";
            case '\u0634' -> "u";
            case '\u0635' -> "S";
            case '\u0636' -> "X";
            case '\u0637' -> "T";
            case '\u0638' -> "Z";
            case '\u0639' -> "i";
            case '\u063a' -> "Q";
            case '\u0641' -> "f";
            case '\u0642' -> "q";
            case '\u06a9', '\u0643' -> "k";
            case '\u06af' -> "g";
            case '\u0644' -> "l";
            case '\u0645' -> "m";
            case '\u0646' -> "n";
            case '\u0648' -> "v";
            case '\u0647' -> "H";
            case '\u06cc', '\u064a' -> "y";
            case '\u0626' -> "I";
            case '\u0020' -> " ";
            case '\u0640' -> "";
            default -> "+" + String.valueOf(c);
        };
    }

    public static boolean hasTextAndIsPersian(String src) {
        boolean isPersian = false;
        if (src == null) {
            return false;
        }
        src = src.trim();
        if (src.length() > 0) {

            for (int i = 0; i < src.length(); i++) {
                Matcher matcher = patternPersian.matcher(String.valueOf(src.charAt(i)));
                if (matcher.matches()) {
                    isPersian = true;
                    break;
                }

            }
        }
        return isPersian;

    }

    public static String separateNumberByComma(String src) {
        try {
            double amount = Double.parseDouble(src);
            DecimalFormat decimalFormat = new DecimalFormat("#,###");
            return decimalFormat.format(amount);
        } catch (NumberFormatException ex) {
            return "";
        }
    }

}
