package com.serveflow.integration;

import com.serveflow.model.address.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests FindPostalCode CEP-validation branches that short-circuit before HTTP.
 */
@ExtendWith(MockitoExtension.class)
class FindPostalCodeTest {

    @Mock
    RestClient.Builder builder;
    @Mock
    RestClient restClient;

    private FindPostalCode createFpc() {
        when(builder.build()).thenReturn(restClient);
        return new FindPostalCode("https://viacep.com.br/ws/{cep}/json/", builder);
    }

    @Test
    @DisplayName("CEP com apenas 4 dígitos retorna Optional.empty sem chamada HTTP")
    void findByCep_returnsEmpty_whenCepTooShort() {
        FindPostalCode fpc = createFpc();
        Optional<Address> result = fpc.findByCep("1234");
        assertThat(result).isEmpty();
        verifyNoInteractions(restClient);
    }

    @Test
    @DisplayName("CEP null retorna Optional.empty sem chamada HTTP")
    void findByCep_returnsEmpty_whenNull() {
        FindPostalCode fpc = createFpc();
        Optional<Address> result = fpc.findByCep(null);
        assertThat(result).isEmpty();
        verifyNoInteractions(restClient);
    }

    @Test
    @DisplayName("CEP com 7 dígitos retorna Optional.empty")
    void findByCep_returnsEmpty_whenSevenDigits() {
        FindPostalCode fpc = createFpc();
        Optional<Address> result = fpc.findByCep("1234567");
        assertThat(result).isEmpty();
        verifyNoInteractions(restClient);
    }

    @Test
    @DisplayName("CEP de 9 dígitos retorna Optional.empty")
    void findByCep_returnsEmpty_whenNineDigits() {
        FindPostalCode fpc = createFpc();
        Optional<Address> result = fpc.findByCep("123456789");
        assertThat(result).isEmpty();
        verifyNoInteractions(restClient);
    }
}
