package com.serveflow.service.product;

import com.serveflow.dto.product.request.ProductInput;
import com.serveflow.dto.product.response.ProductOutput;
import com.serveflow.exception.product.ProductNotFoundException;
import com.serveflow.model.product.Product;
import com.serveflow.repository.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository repository;

    @InjectMocks
    ProductService service;

    private ProductInput input() {
        return new ProductInput(
                "Hamburguer Artesanal",
                "Pão brioche, blend 180g, queijo",
                "Lanches",
                "Marca X",
                new BigDecimal("29.90"),
                "350g",
                null,
                null,
                null,
                null,
                null
        );
    }

    private Product savedProduct(UUID id) {
        return Product.builder()
                .id(id)
                .name("Hamburguer Artesanal")
                .description("Pão brioche, blend 180g, queijo")
                .category("Lanches")
                .brand("Marca X")
                .price(new BigDecimal("29.90"))
                .portion("350g")
                .active(true)
                .createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .version(1L)
                .build();
    }

    private void assertOutputMatchesProduct(ProductOutput output, Product product) {
        assertThat(output.id()).isEqualTo(product.getId());
        assertThat(output.name()).isEqualTo(product.getName());
        assertThat(output.description()).isEqualTo(product.getDescription());
        assertThat(output.category()).isEqualTo(product.getCategory());
        assertThat(output.brand()).isEqualTo(product.getBrand());
        assertThat(output.price()).isEqualByComparingTo(product.getPrice());
        assertThat(output.portion()).isEqualTo(product.getPortion());
        assertThat(output.active()).isEqualTo(product.isActive());
        assertThat(output.createdAt()).isEqualTo(product.getCreatedAt());
    }

    @Test
    @DisplayName("create: persiste produto e retorna output com todos os campos")
    void create_persistsAndReturnsFullOutput() {
        UUID id = UUID.randomUUID();
        Product saved = savedProduct(id);
        when(repository.save(any(Product.class))).thenReturn(saved);

        ProductOutput result = service.create(input());

        assertOutputMatchesProduct(result, saved);
        verify(repository).save(any(Product.class));
    }

    @Test
    @DisplayName("createBatch: persiste lista e retorna um output por item")
    void createBatch_persistsAllAndReturnsMappedOutputs() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<ProductInput> inputs = List.of(input(), input());
        List<Product> saved = List.of(savedProduct(id1), savedProduct(id2));
        when(repository.saveAll(anyList())).thenReturn(saved);

        List<ProductOutput> results = service.createBatch(inputs);

        assertThat(results).hasSize(2);
        assertOutputMatchesProduct(results.get(0), saved.get(0));
        assertOutputMatchesProduct(results.get(1), saved.get(1));
        verify(repository).saveAll(anyList());
    }

    @Test
    @DisplayName("createBatch: lista vazia retorna lista vazia sem chamar saveAll com dados")
    void createBatch_emptyInput_returnsEmptyList() {
        when(repository.saveAll(anyList())).thenReturn(List.of());

        List<ProductOutput> results = service.createBatch(List.of());

        assertThat(results).isEmpty();
        verify(repository).saveAll(anyList());
    }

    @Test
    @DisplayName("findById: retorna output quando produto é encontrado")
    void findById_returnsOutput_whenFound() {
        UUID id = UUID.randomUUID();
        Product product = savedProduct(id);
        when(repository.findById(id)).thenReturn(product);

        ProductOutput result = service.findById(id);

        assertOutputMatchesProduct(result, product);
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("findById: propaga ProductNotFound quando repositório lança a exceção")
    void findById_propagatesProductNotFound_whenRepositoryThrows() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new ProductNotFoundException(id));

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("findAllActive: retorna outputs de todos os produtos ativos")
    void findAllActive_returnsMappedActiveProducts() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<Product> active = List.of(savedProduct(id1), savedProduct(id2));
        when(repository.findAllActive()).thenReturn(active);

        List<ProductOutput> results = service.findAllActive();

        assertThat(results).hasSize(2);
        assertOutputMatchesProduct(results.get(0), active.get(0));
        assertOutputMatchesProduct(results.get(1), active.get(1));
        verify(repository).findAllActive();
    }

    @Test
    @DisplayName("findAllActive: retorna lista vazia quando não há produtos ativos")
    void findAllActive_returnsEmptyList_whenNoActiveProducts() {
        when(repository.findAllActive()).thenReturn(List.of());

        List<ProductOutput> results = service.findAllActive();

        assertThat(results).isEmpty();
        verify(repository).findAllActive();
    }

    @Test
    @DisplayName("update: aplica mudanças no produto existente e retorna output atualizado")
    void update_appliesChangesAndReturnsUpdatedOutput() {
        UUID id = UUID.randomUUID();
        Product existing = savedProduct(id);
        Product afterSave = savedProduct(id);
        when(repository.findById(id)).thenReturn(existing);
        when(repository.save(existing)).thenReturn(afterSave);

        ProductOutput result = service.update(id, input());

        assertOutputMatchesProduct(result, afterSave);
        verify(repository).findById(id);
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("update: propaga ProductNotFound quando produto não existe")
    void update_propagatesProductNotFound_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new ProductNotFoundException(id));

        assertThatThrownBy(() -> service.update(id, input()))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(repository).findById(id);
        verify(repository, org.mockito.Mockito.never()).save(any());
    }


    @Test
    @DisplayName("deactivate: delega ao repositório quando produto está ativo")
    void deactivate_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        Product active = savedProduct(id);
        when(repository.findById(id)).thenReturn(active);
        doNothing().when(repository).deactivate(id);

        service.deactivate(id);

        verify(repository).findById(id);
        verify(repository).deactivate(id);
    }

    @Test
    @DisplayName("deactivate: chama hardDelete quando produto já está inativo")
    void deactivate_callsHardDelete_whenProductInactive() {
        UUID id = UUID.randomUUID();
        Product inactive = Product.builder()
                .id(id)
                .name("Produto Inativo")
                .description("desc")
                .category("cat")
                .brand("brand")
                .price(new BigDecimal("10.00"))
                .portion("100g")
                .active(false)
                .createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .version(1L)
                .build();
        when(repository.findById(id)).thenReturn(inactive);
        doNothing().when(repository).hardDelete(id);

        service.deactivate(id);

        verify(repository).findById(id);
        verify(repository).hardDelete(id);
        verify(repository, org.mockito.Mockito.never()).deactivate(id);
    }

    @Test
    @DisplayName("deactivate: propaga ProductNotFound quando repositório lança a exceção no findById")
    void deactivate_propagatesProductNotFound_whenRepositoryThrows() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new ProductNotFoundException(id));

        assertThatThrownBy(() -> service.deactivate(id))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(repository).findById(id);
        verify(repository, org.mockito.Mockito.never()).deactivate(id);
    }
}
