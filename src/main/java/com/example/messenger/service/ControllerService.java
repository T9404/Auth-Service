package com.example.messenger.service;

import com.example.messenger.dto.UserSystemInfoDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;

@Component
public class ControllerService {
    private static final String GEOIP_DB_PATH = "src/main/resources/GeoLite2-City.mmdb";
    private static final DatabaseReader GEOIP_DB_READER;

    static {
        try {
            GEOIP_DB_READER = new DatabaseReader.Builder(new File(GEOIP_DB_PATH)).build();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize GeoIP database", e);
        }
    }

    public HashMap<String, Object> getUserDevice(HttpServletRequest httpServletRequest) {
        HashMap<String, Object> userDevice = new HashMap<>();
        String ipAddress = httpServletRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpServletRequest.getRemoteAddr();
        }
        String userAgent = httpServletRequest.getHeader("User-Agent");
        userDevice.put("ipAddress", ipAddress);
        userDevice.put("userAgent", userAgent);
        return userDevice;
    }

    public UserSystemInfoDto getUserSystemInfo(HttpServletRequest httpServletRequest) {
        UserSystemInfoDto userSystemInfoDto = new UserSystemInfoDto();
        String ipAddress = httpServletRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpServletRequest.getRemoteAddr();
        }
        String userAgent = httpServletRequest.getHeader("User-Agent");
        String device = null;
        String os = null;
        String browser = null;

        UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
        ReadableUserAgent agent = parser.parse(userAgent);

        if (agent != null) {
            device = agent.getDeviceCategory().getName();
            os = agent.getOperatingSystem().getName();
            browser = agent.getName();
        }

        userSystemInfoDto.setIpAddress(checkUnknownVariable(ipAddress));
        userSystemInfoDto.setUserAgent(checkUnknownVariable(userAgent));
        userSystemInfoDto.setDevice(checkUnknownVariable(device));
        userSystemInfoDto.setOs(checkUnknownVariable(os));
        userSystemInfoDto.setBrowser(checkUnknownVariable(browser));
        userSystemInfoDto.setLocation(checkUnknownVariable(getCity(ipAddress)));

        return userSystemInfoDto;
    }

    private String checkUnknownVariable(String value) {
        return (value != null && !value.equals("unknown") && !value.isEmpty()) ? value : "N/A";
    }

    private String getCity(String ipAddress) {
        try {
            CityResponse response = GEOIP_DB_READER.city(InetAddress.getByName(ipAddress));
            return response.getCity().getName();
        } catch (Exception e) {
            return "N/A";
        }
    }
}
