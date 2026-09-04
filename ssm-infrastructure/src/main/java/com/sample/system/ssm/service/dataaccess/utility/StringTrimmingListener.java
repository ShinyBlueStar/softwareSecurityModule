package com.sample.system.ssm.service.dataaccess.utility;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class StringTrimmingListener {
    public static Boolean parseBooleanValue(String value) {
        if (value == null) return null;

        return switch (value.trim().toLowerCase()) {
            case "true", "1", "yes", "y" -> true;
            case "false", "0", "no", "n" -> false;
            default -> null;
        };
    }

    static String normalizeFaInput(String s) {
        if (s == null) return null;
        return s
                .replace('ي','ی').replace('ى','ی')   // Arabic Yeh -> Persian Yeh
                .replace('ك','ک')                    // Arabic Kaf -> Persian Kaf
                .replace('ۀ','ه').replace('ة','ه')   // Ta marbuta/Heh Yeh above -> Heh
                .replace('أ','ا').replace('إ','ا').replace('آ','ا') // Hamza/Alef -> Alef
                .replace("\u200c","")                // ZWNJ
                .trim();
    }
    static String escapeLike(String s) {
        return s.replace("\\","\\\\").replace("%","\\%").replace("_","\\_");
    }

    public static Predicate buildFarsiLikePredicate(
            Root<?> root,
            CriteriaBuilder cb,
            String fieldName,
            String searchValue) {

        if (StringUtils.isBlank(searchValue)) return null;

        String in = normalizeFaInput(searchValue).toLowerCase(Locale.ROOT);
        String pattern = "%" + escapeLike(in) + "%";

        // Normalization on DB side
        Expression<String> normalizedColumn = cb.lower(
                cb.function(
                        "TRANSLATE", String.class,
                        cb.function("REPLACE", String.class,
                                cb.coalesce(root.get(fieldName), cb.literal("")),
                                cb.literal("\u200C"), cb.literal("")
                        ),
                        cb.literal("كيۀةىأإآ"),
                        cb.literal("کیههیااا")
                )
        );
        return cb.like(normalizedColumn, pattern, '\\');
    }
}
