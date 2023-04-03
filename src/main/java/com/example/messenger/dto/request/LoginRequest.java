package com.example.messenger.dto.request;

import com.example.messenger.exception.JsonNotValidException;
import com.example.messenger.validation.generalValid.AvailableChar;
import com.example.messenger.validation.userValid.PhoneNumber;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @PhoneNumber
    private String phoneNumber;
    @Size(min = 8, max = 64)
    @AvailableChar
    private String password;
    private String ipAddress;
    private String userAgent;

    @JsonAnySetter
    public void handleUnknownProperty(String key, Object value) throws JsonNotValidException {
        throw new JsonNotValidException("Unknown property: " + key);
    }
}
