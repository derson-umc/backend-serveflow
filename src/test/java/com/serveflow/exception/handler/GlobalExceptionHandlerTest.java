package com.serveflow.exception.handler;

import com.serveflow.exception.auth.InvalidResetTokenException;
import com.serveflow.exception.cashier.CashSessionAlreadyClosedException;
import com.serveflow.exception.cashier.CashSessionNotFoundException;
import com.serveflow.exception.cashier.OpenSessionAlreadyExistsException;
import com.serveflow.exception.financial.AccountNotFoundException;
import com.serveflow.exception.financial.DuplicateSettlementException;
import com.serveflow.exception.financial.InconsistentAmountException;
import com.serveflow.exception.menu.MenuNotFoundException;
import com.serveflow.exception.order.OrderNotFoundException;
import com.serveflow.exception.product.ProductNotFoundException;
import com.serveflow.exception.stock.InsufficientStockException;
import com.serveflow.exception.stock.RecipeNotFoundException;
import com.serveflow.exception.stock.StockAlertNotFoundException;
import com.serveflow.exception.stock.StockItemNotFoundException;
import com.serveflow.exception.user.BusinessRuleException;
import com.serveflow.exception.user.ConflictException;
import com.serveflow.exception.user.UserNotFoundException;
import com.serveflow.service.audit.AuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    AuditService auditService;

    @InjectMocks
    GlobalExceptionHandler handler;

    @Nested
    @DisplayName("404 Not Found handlers")
    class NotFound {

        @Test
        @DisplayName("UserNotFoundException retorna 404")
        void userNotFound_returns404() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleUserNotFound(new UserNotFoundException("user1"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("ProductNotFoundException retorna 404")
        void productNotFound_returns404() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleProductNotFound(new ProductNotFoundException(UUID.randomUUID()));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("MenuNotFoundException retorna 404")
        void menuNotFound_returns404() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleMenuNotFound(new MenuNotFoundException(UUID.randomUUID()));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("OrderNotFoundException retorna 404")
        void orderNotFound_returns404() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleOrderNotFound(new OrderNotFoundException(UUID.randomUUID()));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("StockItemNotFoundException retorna 404")
        void stockItemNotFound_returns404() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleStockItemNotFound(new StockItemNotFoundException(UUID.randomUUID()));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("RecipeNotFoundException retorna 404")
        void recipeNotFound_returns404() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleRecipeNotFound(new RecipeNotFoundException(UUID.randomUUID()));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("StockAlertNotFoundException retorna 404")
        void stockAlertNotFound_returns404() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleStockAlertNotFound(new StockAlertNotFoundException(UUID.randomUUID()));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("CashSessionNotFoundException retorna 404")
        void cashSessionNotFound_returns404() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleCashSessionNotFound(new CashSessionNotFoundException(UUID.randomUUID()));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("AccountNotFoundException retorna 404")
        void accountNotFound_returns404() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleAccountNotFound(new AccountNotFoundException(UUID.randomUUID()));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("409 Conflict handlers")
    class Conflict {

        @Test
        @DisplayName("CashSessionAlreadyClosedException retorna 409")
        void cashSessionAlreadyClosed_returns409() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleCashSessionAlreadyClosed(
                            new CashSessionAlreadyClosedException(UUID.randomUUID()));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("OpenSessionAlreadyExistsException retorna 409")
        void openSessionAlreadyExists_returns409() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleOpenSessionAlreadyExists(new OpenSessionAlreadyExistsException());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("DuplicateSettlementException retorna 409")
        void duplicateSettlement_returns409() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleDuplicateSettlement(new DuplicateSettlementException(UUID.randomUUID()));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("ConflictException retorna 409")
        void conflict_returns409() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleConflict(new ConflictException("conflict msg"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("OptimisticLockingFailureException retorna 409")
        void optimisticLock_returns409() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleOptimisticLock(new OptimisticLockingFailureException("lock"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("IllegalStateException retorna 409")
        void illegalState_returns409() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleIllegalState(new IllegalStateException("state error"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }
    }

    @Nested
    @DisplayName("422 Unprocessable Entity handlers")
    class UnprocessableEntity {

        @Test
        @DisplayName("InsufficientStockException retorna 422")
        void insufficientStock_returns422() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleInsufficientStock(new InsufficientStockException("sem estoque"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        @Test
        @DisplayName("InconsistentAmountException retorna 422")
        void inconsistentAmount_returns422() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleInconsistentAmount(new InconsistentAmountException("valor inconsistente"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        @Test
        @DisplayName("BusinessRuleException retorna 422")
        void businessRule_returns422() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleBusinessRule(new BusinessRuleException("regra de negocio"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Nested
    @DisplayName("400 Bad Request handlers")
    class BadRequest {

        @Test
        @DisplayName("InvalidResetTokenException retorna 400")
        void invalidResetToken_returns400() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleInvalidResetToken(new InvalidResetTokenException());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("IllegalArgumentException retorna 400")
        void illegalArgument_returns400() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleBadRequest(new IllegalArgumentException("bad arg"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("NullPointerException retorna 400 com mensagem padrão quando sem mensagem")
        void nullPointer_returns400_withDefaultMessage() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleNullPointer(new NullPointerException());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).containsEntry("error", "Campo obrigatório ausente na requisição.");
        }

        @Test
        @DisplayName("NullPointerException com mensagem retorna a mensagem original")
        void nullPointer_returns400_withOriginalMessage() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleNullPointer(new NullPointerException("campo nulo"));
            assertThat(response.getBody()).containsEntry("error", "campo nulo");
        }
    }

    @Nested
    @DisplayName("500 Internal Server Error handlers")
    class InternalServerError {

        @Test
        @DisplayName("Exception genérica retorna 500")
        void generic_exception_returns500() {
            org.mockito.Mockito.doNothing().when(auditService).logError(
                    org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
            ResponseEntity<Map<String, Object>> response =
                    handler.handleGeneric(new RuntimeException("erro inesperado"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("response body structure")
    class ResponseBody {

        @Test
        @DisplayName("body contém timestamp, status e error")
        void body_containsTimestampStatusAndError() {
            ResponseEntity<Map<String, Object>> response =
                    handler.handleBadRequest(new IllegalArgumentException("teste"));
            Map<String, Object> body = response.getBody();
            assertThat(body).containsKeys("timestamp", "status", "error");
            assertThat(body.get("status")).isEqualTo(400);
        }
    }
}
