package com.serveflow.integration;

import com.serveflow.dto.order.request.AddressInput;
import com.serveflow.model.address.*;
import com.serveflow.model.address.Number;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressResolverTest {

    @Mock
    FindPostalCode findPostalCode;

    AddressResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new AddressResolver(findPostalCode, "São Paulo", "SP");
    }

    private Address buildAddress(String street, String city, String state) {
        return Address.create(
                new Cep("01310-100"),
                new Street(street),
                new City(city),
                new State(state),
                new Number("100"),
                null
        );
    }

    @Nested
    @DisplayName("resolve() — input nulo")
    class NullInput {

        @Test
        @DisplayName("retorna null quando AddressInput é nulo")
        void resolve_returnsNull_whenInputNull() {
            assertThat(resolver.resolve(null)).isNull();
        }
    }

    @Nested
    @DisplayName("resolve() — via CEP")
    class ViaCep {

        @Test
        @DisplayName("retorna Address resolvido pelo CEP quando encontrado")
        void resolve_returnsCepAddress_whenFound() {
            Address base = buildAddress("Av. Paulista", "São Paulo", "SP");
            when(findPostalCode.findByCep("01310-100")).thenReturn(Optional.of(base));

            AddressInput input = new AddressInput("01310-100", null, null, null, "500", null);
            Address result = resolver.resolve(input);

            assertThat(result).isNotNull();
            assertThat(result.getStreet()).isNotNull();
            verify(findPostalCode).findByCep("01310-100");
        }

        @Test
        @DisplayName("usa 'S/N' quando número não fornecido com CEP")
        void resolve_usesSN_whenNumberMissing() {
            Address base = buildAddress("Av. Paulista", "São Paulo", "SP");
            when(findPostalCode.findByCep("01310-100")).thenReturn(Optional.of(base));

            AddressInput input = new AddressInput("01310-100", null, null, null, null, null);
            Address result = resolver.resolve(input);

            assertThat(result).isNotNull();
            assertThat(result.getNumber().getValue()).isEqualTo("S/N");
        }

        @Test
        @DisplayName("inclui complemento quando fornecido com CEP")
        void resolve_includesComplement_whenProvided() {
            Address base = buildAddress("Av. Paulista", "São Paulo", "SP");
            when(findPostalCode.findByCep("01310-100")).thenReturn(Optional.of(base));

            AddressInput input = new AddressInput("01310-100", null, null, null, "100", "Apto 42");
            Address result = resolver.resolve(input);

            assertThat(result).isNotNull();
            assertThat(result.getComplement()).isNotNull();
            assertThat(result.getComplement().getValue()).isEqualTo("Apto 42");
        }

        @Test
        @DisplayName("cai para manual quando CEP não encontrado mas tem campos manuais")
        void resolve_fallsToManual_whenCepNotFound() {
            when(findPostalCode.findByCep(anyString())).thenReturn(Optional.empty());

            AddressInput input = new AddressInput("99999-999", "Rua das Flores", "Campinas", "SP", "100", null);
            Address result = resolver.resolve(input);

            assertThat(result).isNotNull();
            assertThat(result.getStreet().getValue()).isEqualTo("Rua das Flores");
        }
    }

    @Nested
    @DisplayName("resolve() — manual")
    class Manual {

        @Test
        @DisplayName("retorna Address manual quando não há CEP mas tem rua e número")
        void resolve_buildsManually_whenNoCep() {
            AddressInput input = new AddressInput(null, "Rua das Flores", "Campinas", "SP", "42", "Casa");
            Address result = resolver.resolve(input);

            assertThat(result).isNotNull();
            assertThat(result.getStreet().getValue()).isEqualTo("Rua das Flores");
            assertThat(result.getNumber().getValue()).isEqualTo("42");
            assertThat(result.getComplement().getValue()).isEqualTo("Casa");
            verifyNoInteractions(findPostalCode);
        }

        @Test
        @DisplayName("usa cidade padrão quando cidade não fornecida")
        void resolve_usesDefaultCity_whenCityMissing() {
            AddressInput input = new AddressInput(null, "Rua X", null, "SP", "1", null);
            Address result = resolver.resolve(input);

            assertThat(result.getCity().getValue()).isEqualTo("São Paulo");
        }

        @Test
        @DisplayName("usa estado padrão quando estado não fornecido")
        void resolve_usesDefaultState_whenStateMissing() {
            AddressInput input = new AddressInput(null, "Rua X", "Campinas", null, "1", null);
            Address result = resolver.resolve(input);

            assertThat(result.getState().getValue().name()).isEqualTo("SP");
        }

        @Test
        @DisplayName("retorna null quando rua ausente e CEP ausente")
        void resolve_returnsNull_whenNoStreetAndNoCep() {
            AddressInput input = new AddressInput(null, null, "São Paulo", "SP", "100", null);
            Address result = resolver.resolve(input);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("retorna null quando número ausente e CEP ausente")
        void resolve_returnsNull_whenNoNumberAndNoCep() {
            AddressInput input = new AddressInput(null, "Rua X", "São Paulo", "SP", null, null);
            Address result = resolver.resolve(input);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("inclui CEP quando fornecido manualmente")
        void resolve_includesCep_whenManual() {
            AddressInput input = new AddressInput("01310-100", "Av. Paulista", "São Paulo", "SP", "500", null);
            // CEP not found via API
            when(findPostalCode.findByCep(anyString())).thenReturn(Optional.empty());

            Address result = resolver.resolve(input);

            assertThat(result).isNotNull();
            assertThat(result.getCep()).isNotNull();
        }
    }
}
