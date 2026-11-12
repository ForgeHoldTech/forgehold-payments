
package com.forgehold.payments.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String username
) {
}
