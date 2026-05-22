package com.notfound.bookstorenotificationservice.client;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTokenProviderTest {

    @Test
    void resolveBearerToken_logsInWithAdminCredentials() {
        UserServiceAuthClient authClient = mock(UserServiceAuthClient.class);
        String loginToken = jwtLikeToken("login");
        when(authClient.login(any())).thenReturn(successResponse(loginToken));
        UserServiceTokenProvider provider =
                new UserServiceTokenProvider(authClient, "admin", "admin");

        String token = provider.resolveBearerToken();

        assertThat(token).isEqualTo(loginToken);
        verify(authClient).login(any());
    }

    @Test
    void resolveBearerToken_withBlankEnvValues_defaultsToAdminCredentials() {
        UserServiceAuthClient authClient = mock(UserServiceAuthClient.class);
        String loginToken = jwtLikeToken("login");
        when(authClient.login(any())).thenReturn(successResponse(loginToken));
        UserServiceTokenProvider provider =
                new UserServiceTokenProvider(authClient, "", "");

        String token = provider.resolveBearerToken();

        ArgumentCaptor<UserServiceLoginRequest> requestCaptor =
                ArgumentCaptor.forClass(UserServiceLoginRequest.class);
        assertThat(token).isEqualTo(loginToken);
        verify(authClient).login(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getUsername()).isEqualTo("admin");
        assertThat(requestCaptor.getValue().getPassword()).isEqualTo("admin");
    }

    private static UserServiceApiResponse<UserServiceAuthResponse> successResponse(String token) {
        UserServiceAuthResponse authResponse = new UserServiceAuthResponse();
        authResponse.setToken(token);
        UserServiceApiResponse<UserServiceAuthResponse> response = new UserServiceApiResponse<>();
        response.setCode(UserServiceApiResponse.SUCCESS_CODE);
        response.setMessage("Success");
        response.setResult(authResponse);
        return response;
    }

    private static String jwtLikeToken(String suffix) {
        return "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-" + suffix;
    }
}
