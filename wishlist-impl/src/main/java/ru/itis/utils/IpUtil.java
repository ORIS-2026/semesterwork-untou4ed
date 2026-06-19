package ru.itis.utils;


import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IpUtil {

    public static String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }

        return request.getRemoteAddr();
    }
}