package com.serveflow.repository.financial;

import com.serveflow.model.financial.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SpringAccountPayableRepository extends JpaRepository<AccountPayableEntity, UUID> {

    List<AccountPayableEntity> findByStatusOrderByDueDateAsc(AccountStatus status);

    List<AccountPayableEntity> findByDueDateBeforeAndStatus(LocalDate date, AccountStatus status);

    List<AccountPayableEntity> findAllByOrderByDueDateAsc();
}
