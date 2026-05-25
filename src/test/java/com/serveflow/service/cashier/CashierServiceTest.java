package com.serveflow.service.cashier;

import com.serveflow.dto.cashier.request.CashMovementInput;
import com.serveflow.dto.cashier.request.CloseSessionInput;
import com.serveflow.dto.cashier.request.OpenSessionInput;
import com.serveflow.dto.cashier.response.CashMovementOutput;
import com.serveflow.dto.cashier.response.CashSessionOutput;
import com.serveflow.exception.cashier.CashSessionAlreadyClosedException;
import com.serveflow.exception.cashier.CashSessionNotFoundException;
import com.serveflow.exception.cashier.OpenSessionAlreadyExistsException;
import com.serveflow.model.cashier.CashSessionStatus;
import com.serveflow.model.financial.TransactionType;
import com.serveflow.repository.cashier.CashMovementEntity;
import com.serveflow.repository.cashier.CashSessionEntity;
import com.serveflow.repository.cashier.SpringCashMovementRepository;
import com.serveflow.repository.cashier.SpringCashSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashierServiceTest {

    @Mock SpringCashSessionRepository sessionRepository;
    @Mock SpringCashMovementRepository movementRepository;

    @InjectMocks CashierService service;

    private UUID sessionId;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("openSession")
    class OpenSession {

        @Test
        @DisplayName("cria sessão quando não existe sessão aberta.")
        void openSession_criaSessao_whenNenhumaSessaoAberta() {
            when(sessionRepository.existsByStatus(CashSessionStatus.OPEN)).thenReturn(false);
            CashSessionEntity saved = sessionEntity(sessionId, CashSessionStatus.OPEN);
            when(sessionRepository.save(any(CashSessionEntity.class))).thenReturn(saved);

            CashSessionOutput result = service.openSession(
                    new OpenSessionInput(BigDecimal.TEN, "Abertura do dia"), "operador");

            assertThat(result.id()).isEqualTo(sessionId);
            assertThat(result.status()).isEqualTo("OPEN");
            verify(sessionRepository).save(any(CashSessionEntity.class));
        }

        @Test
        @DisplayName("lança OpenSessionAlreadyExistsException quando já existe sessão aberta.")
        void openSession_lancaExcecao_whenSessaoJaAberta() {
            when(sessionRepository.existsByStatus(CashSessionStatus.OPEN)).thenReturn(true);

            assertThatThrownBy(() -> service.openSession(
                    new OpenSessionInput(BigDecimal.TEN, null), "operador"))
                    .isInstanceOf(OpenSessionAlreadyExistsException.class);

            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("persiste saldo inicial e operador corretamente.")
        void openSession_persisteSaldoEOperador() {
            when(sessionRepository.existsByStatus(CashSessionStatus.OPEN)).thenReturn(false);
            CashSessionEntity saved = sessionEntity(sessionId, CashSessionStatus.OPEN);
            saved.setInitialBalance(new BigDecimal("500.00"));
            saved.setOpenedBy("caixa01");
            when(sessionRepository.save(any(CashSessionEntity.class))).thenReturn(saved);

            CashSessionOutput result = service.openSession(
                    new OpenSessionInput(new BigDecimal("500.00"), null), "caixa01");

            assertThat(result.openedBy()).isEqualTo("caixa01");
            assertThat(result.initialBalance()).isEqualByComparingTo("500.00");
        }
    }

    @Nested
    @DisplayName("closeSession")
    class CloseSession {

        @Test
        @DisplayName("fecha sessão aberta com sucesso.")
        void closeSession_fechaSessao_whenSessaoAberta() {
            CashSessionEntity entity = sessionEntity(sessionId, CashSessionStatus.OPEN);
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(entity));
            CashSessionEntity closed = sessionEntity(sessionId, CashSessionStatus.CLOSED);
            closed.setClosedBy("supervisor");
            when(sessionRepository.save(entity)).thenReturn(closed);

            CashSessionOutput result = service.closeSession(
                    sessionId, new CloseSessionInput("Fechamento"), "supervisor");

            assertThat(result.status()).isEqualTo("CLOSED");
            assertThat(result.closedBy()).isEqualTo("supervisor");
            verify(sessionRepository).save(entity);
        }

        @Test
        @DisplayName("lança CashSessionNotFoundException quando ID não existe.")
        void closeSession_lancaExcecao_whenSessaoNaoEncontrada() {
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.closeSession(
                    sessionId, new CloseSessionInput("obs"), "user"))
                    .isInstanceOf(CashSessionNotFoundException.class);
        }

        @Test
        @DisplayName("lança CashSessionAlreadyClosedException quando sessão já está fechada.")
        void closeSession_lancaExcecao_whenSessaoJaFechada() {
            CashSessionEntity entity = sessionEntity(sessionId, CashSessionStatus.CLOSED);
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> service.closeSession(
                    sessionId, new CloseSessionInput("obs"), "user"))
                    .isInstanceOf(CashSessionAlreadyClosedException.class);

            verify(sessionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getCurrentSession")
    class GetCurrentSession {

        @Test
        @DisplayName("retorna Optional.empty() quando não há sessão aberta.")
        void getCurrentSession_retornaVazio_whenNenhumaSessaoAberta() {
            when(sessionRepository.findFirstByStatusOrderByOpenedAtDesc(CashSessionStatus.OPEN))
                    .thenReturn(Optional.empty());

            assertThat(service.getCurrentSession()).isEmpty();
        }

        @Test
        @DisplayName("retorna output da sessão aberta quando existir.")
        void getCurrentSession_retornaSessao_whenSessaoAberta() {
            CashSessionEntity entity = sessionEntity(sessionId, CashSessionStatus.OPEN);
            when(sessionRepository.findFirstByStatusOrderByOpenedAtDesc(CashSessionStatus.OPEN))
                    .thenReturn(Optional.of(entity));

            Optional<CashSessionOutput> result = service.getCurrentSession();

            assertThat(result).isPresent();
            assertThat(result.get().status()).isEqualTo("OPEN");
            assertThat(result.get().id()).isEqualTo(sessionId);
        }
    }

    @Nested
    @DisplayName("addMovement")
    class AddMovement {

        @Test
        @DisplayName("registra movimentação em sessão aberta com origem MANUAL.")
        void addMovement_registraMovimento_whenSessaoAberta() {
            CashSessionEntity entity = sessionEntity(sessionId, CashSessionStatus.OPEN);
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(entity));
            CashMovementEntity saved = movementEntity(sessionId, TransactionType.INCOME);
            when(movementRepository.save(any(CashMovementEntity.class))).thenReturn(saved);

            CashMovementOutput result = service.addMovement(sessionId, validMovementInput(), "operador");

            assertThat(result).isNotNull();
            assertThat(result.sessionId()).isEqualTo(sessionId);
            assertThat(result.type()).isEqualTo("INCOME");
            verify(movementRepository).save(any(CashMovementEntity.class));
        }

        @Test
        @DisplayName("registra movimentação com origem customizada.")
        void addMovement_registraMovimento_comOrigemCustomizada() {
            CashSessionEntity entity = sessionEntity(sessionId, CashSessionStatus.OPEN);
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(entity));
            CashMovementEntity saved = movementEntity(sessionId, TransactionType.EXPENSE);
            saved.setOrigem("PEDIDO");
            when(movementRepository.save(any(CashMovementEntity.class))).thenReturn(saved);

            CashMovementOutput result = service.addMovement(sessionId, validMovementInput(), "operador", "PEDIDO");

            assertThat(result.origem()).isEqualTo("PEDIDO");
        }

        @Test
        @DisplayName("lança CashSessionNotFoundException quando sessão não existe.")
        void addMovement_lancaExcecao_whenSessaoNaoEncontrada() {
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addMovement(sessionId, validMovementInput(), "operador"))
                    .isInstanceOf(CashSessionNotFoundException.class);

            verify(movementRepository, never()).save(any());
        }

        @Test
        @DisplayName("lança CashSessionAlreadyClosedException quando sessão está fechada.")
        void addMovement_lancaExcecao_whenSessaoFechada() {
            CashSessionEntity entity = sessionEntity(sessionId, CashSessionStatus.CLOSED);
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> service.addMovement(sessionId, validMovementInput(), "operador"))
                    .isInstanceOf(CashSessionAlreadyClosedException.class);

            verify(movementRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("listMovements")
    class ListMovements {

        @Test
        @DisplayName("lança CashSessionNotFoundException quando sessão não existe.")
        void listMovements_lancaExcecao_whenSessaoNaoEncontrada() {
            when(sessionRepository.existsById(sessionId)).thenReturn(false);

            assertThatThrownBy(() -> service.listMovements(sessionId))
                    .isInstanceOf(CashSessionNotFoundException.class);
        }

        @Test
        @DisplayName("retorna lista vazia quando sessão existe mas não tem movimentos.")
        void listMovements_retornaVazio_whenSemMovimentos() {
            when(sessionRepository.existsById(sessionId)).thenReturn(true);
            when(movementRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)).thenReturn(List.of());

            assertThat(service.listMovements(sessionId)).isEmpty();
        }

        @Test
        @DisplayName("retorna todos os movimentos da sessão em ordem crescente.")
        void listMovements_retornaTodos_whenExistemMovimentos() {
            when(sessionRepository.existsById(sessionId)).thenReturn(true);
            List<CashMovementEntity> entities = List.of(
                    movementEntity(sessionId, TransactionType.INCOME),
                    movementEntity(sessionId, TransactionType.EXPENSE));
            when(movementRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)).thenReturn(entities);

            List<CashMovementOutput> result = service.listMovements(sessionId);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("listSessions")
    class ListSessions {

        @Test
        @DisplayName("retorna lista vazia quando não há sessões.")
        void listSessions_retornaVazio_whenNenhumaSessao() {
            when(sessionRepository.findAllByOrderByOpenedAtDesc()).thenReturn(List.of());

            assertThat(service.listSessions()).isEmpty();
        }

        @Test
        @DisplayName("retorna todas as sessões mapeadas para output.")
        void listSessions_retornaTodas_comOutputMapeado() {
            List<CashSessionEntity> entities = List.of(
                    sessionEntity(UUID.randomUUID(), CashSessionStatus.CLOSED),
                    sessionEntity(UUID.randomUUID(), CashSessionStatus.OPEN));
            when(sessionRepository.findAllByOrderByOpenedAtDesc()).thenReturn(entities);

            List<CashSessionOutput> result = service.listSessions();

            assertThat(result).hasSize(2);
        }
    }

    private CashSessionEntity sessionEntity(UUID id, CashSessionStatus status) {
        CashSessionEntity e = new CashSessionEntity();
        e.setId(id);
        e.setStatus(status);
        e.setInitialBalance(BigDecimal.TEN);
        e.setObservation("obs");
        e.setOpenedBy("operador");
        return e;
    }

    private CashMovementEntity movementEntity(UUID sid, TransactionType type) {
        CashMovementEntity e = new CashMovementEntity();
        e.setId(UUID.randomUUID());
        e.setSessionId(sid);
        e.setType(type);
        e.setAmount(BigDecimal.TEN);
        e.setDescription("Movimentação teste");
        e.setPerformedBy("operador");
        e.setOrigem("MANUAL");
        return e;
    }

    private CashMovementInput validMovementInput() {
        return new CashMovementInput(TransactionType.INCOME, BigDecimal.TEN, "Entrada de caixa", "OPERACIONAL");
    }
}
