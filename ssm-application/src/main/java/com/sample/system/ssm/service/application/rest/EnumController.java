package com.sample.system.ssm.service.application.rest;

import com.sample.system.ssm.service.domain.response.base.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping(value = "/api/v1/ssm/enums", produces = "application/vnd.api.v1+json")
public class EnumController {

    private final Map<String, Supplier<Object[]>> enumRegistry = new LinkedHashMap<>();

    public EnumController() {

    }

    private void register(String key, Supplier<Object[]> supplier) {
        enumRegistry.put(key.toUpperCase(Locale.ROOT), supplier);
    }

    @GetMapping("/names")
    public ResponseEntity<BaseResponse<Set<String>>> getAllEnumNames() {
        Set<String> names = enumRegistry.keySet();

        BaseResponse<Set<String>> response = new BaseResponse<>(true, names);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{enumName}")
    public ResponseEntity<?> getEnumValues(
            @Parameter(description = "نام enum مورد نظر", required = true, example = "CARDSTATUS")
            @PathVariable String enumName
    )
    {
        Supplier<Object[]> supplier = enumRegistry.get(enumName.toUpperCase(Locale.ROOT));
        BaseResponse<String> errorResponse;
        if (supplier == null) {
            errorResponse = new BaseResponse<>(false, "Enum not found: " + enumName);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
        }

        Object converted = convertEnumValues(supplier.get());

        BaseResponse<Object> response = new BaseResponse<>(true, converted);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    private List<Map<String, Object>> convertEnumValues(Object[] enumValues) {
        return Arrays.stream(enumValues)
                .map(this::convertSingleEnum)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertSingleEnum(Object enumConstant) {
        Map<String, Object> map = new LinkedHashMap<>();
        Field[] fields = enumConstant.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isSynthetic() || field.isEnumConstant()) {
                continue;
            }
            try {
                field.setAccessible(true);
                map.put(field.getName(), field.get(enumConstant));
            } catch (IllegalAccessException ignored) {
            }
        }
        map.put("name", enumConstant.toString());
        return map;
    }
}
