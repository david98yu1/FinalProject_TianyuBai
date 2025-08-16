package com.example.eventstore.repo;

import java.util.List;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import com.example.eventstore.model.OrderEventsByAccount;

public interface OrderEventsByAccountRepo extends CassandraRepository<OrderEventsByAccount, Long> {
    @Query("SELECT * FROM shop.order_events_by_account WHERE account_id = :accountId")
    List<OrderEventsByAccount> findByAccountId(Long accountId);
}
