package com.serveflow.repository.financial;

import com.serveflow.model.financial.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SpringAccountReceivableRepository extends JpaRepository<AccountReceivableEntity, UUID> {

    List<AccountReceivableEntity> findByStatusOrderByDueDateAsc(AccountStatus status);

    List<AccountReceivableEntity> findByDueDateBeforeAndStatus(LocalDate date, AccountStatus status);

    List<AccountReceivableEntity> findAllByOrderByDueDateAsc();
}
