package kz.demo.bankApplication.repository;

import kz.demo.bankApplication.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

}
