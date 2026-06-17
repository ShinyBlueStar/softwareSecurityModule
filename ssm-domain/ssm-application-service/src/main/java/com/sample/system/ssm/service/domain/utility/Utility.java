package com.sample.system.ssm.service.domain.utility;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.NumberFormat;
import java.util.*;

public class Utility {

    private static final Logger log = LogManager.getLogger(Utility.class);
    private static final Random random = new Random();

    public static String pad(String number, int len) {
        String normal = number;
        while (normal.length() < len) {
            normal = "0" + normal;
        }
        return normal;
    }

    public static boolean isAllowedList(String allowedList, String value) {
        if (!StringUtils.hasText(allowedList)) {
            log.info("value for ip list is null");
            return true;
        }
        var ipList = new LinkedList<>(Arrays.asList(allowedList.split(",")));

        if (!ipList.contains(value)) {
            log.error("value: {} isn't in valid list", value);
            return false;
        }
        return true;
    }

    public static boolean isNull(String data) {
        return !org.springframework.util.StringUtils.hasText(data);
    }

    public static String addComma(long amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    public static int generateRandomNumber(int min, int max) {
        return random.ints(min, (max + 1)).findFirst().getAsInt();
    }

    public static String cleanPhoneNumber(String mobile) {

        if (StringUtils.hasText(mobile)) {
            String onlyNumber = mobile.replaceAll("\\D", "");
            if (onlyNumber.startsWith("09")) {
                return onlyNumber.replaceFirst("^09", "9");
            }
            if (onlyNumber.startsWith("989")) {
                return onlyNumber.replaceFirst("^989", "9");
            }
            if (onlyNumber.startsWith("+989")) {
                return onlyNumber.replaceFirst("\\+989", "9");
            }
            if (onlyNumber.startsWith("9")) {
                return mobile;
            }
        }
        return mobile;
    }

    public static String cleanPhoneNumberAddZero(String mobile) {
        if (isNull(mobile)) {
            return mobile;
        }

        mobile = mobile.replaceAll("\\D", "");

        if (mobile.startsWith("09")) {
            return mobile;
        }
        if (mobile.startsWith("989")) {
            return mobile.replaceAll("^989", "09");
        }
        if (mobile.startsWith("9")) {
            return mobile.replaceAll("^9", "09");
        }
        return null;
    }

    public static String getCallerMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    public static String getCallerClassAndMethodName() {
        return Thread.currentThread().getStackTrace()[2].getClassName() + '.' +
                Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    public static String createResponse(String id, Long amount) {
        return id + "|" + amount;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    public static String mapToJsonOrNull(Object object) {
        try {
            var objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("mapToJson JsonProcessingException !", e);
            return "";
        }
    }

    public int generateRandomDigits(int n) {
        int m = (int) Math.pow(10, n - 1);
        return m + this.random.nextInt(9 * m);
    }


    public boolean isAllowedList(String allowedList, String value, String delimiter) {
        var ipList = new LinkedList<>(Arrays.asList(allowedList.split(delimiter)));

        if (!ipList.contains(value)) {
            log.error("value: {} isn't in valid list", value);
            return false;
        }
        return true;
    }

}
