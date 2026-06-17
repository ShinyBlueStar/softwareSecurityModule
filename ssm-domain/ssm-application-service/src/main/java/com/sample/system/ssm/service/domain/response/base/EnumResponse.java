package com.sample.system.ssm.service.domain.response.base;

/**
 * Generic response object for enum values
 * Contains code and description for enum display
 */
public record EnumResponse(
        Integer code,
        String description
) {
    /**
     * Factory method to create EnumResponse from enum with code and description
     */
    public static EnumResponse fromEnum(Object enumValue) {
        if (enumValue == null) {
            return null;
        }

        try {
            // Use reflection to get code and description
            var codeMethod = enumValue.getClass().getMethod("getCode");
            var descriptionMethod = enumValue.getClass().getMethod("getDescription");

            Integer code = (Integer) codeMethod.invoke(enumValue);
            String description = (String) descriptionMethod.invoke(enumValue);

            return new EnumResponse(code, description);
        } catch (Exception e) {
            return null;
        }
    }
}