package com.example.messenger.dto.request;

import com.example.messenger.exception.JsonNotValidException;
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
public class VerifyCodeRequest {
    @Size(min = 6, max = 6)
    private String verifyCode;

    @JsonAnySetter
    public void handleUnknownProperty(String key, Object value) throws JsonNotValidException {
        throw new JsonNotValidException("Unknown property: " + key);
    }
}
