package com.example.messenger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSystemInfoDto {
    private String ipAddress;
    private String userAgent;
    private String device;
    private String os;
    private String location;
    private String browser;
}
