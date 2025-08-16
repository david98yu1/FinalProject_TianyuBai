package com.example.eventstore.repo;

import java.util.List;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

public interface OrderEventsByOrderRepo extends CassandraRepository<com.example.eventstore.model.OrderEventsByOrder, Long> {
    @Query("SELECT * FROM shop.order_events_by_order WHERE order_id = :orderId")
    List<com.example.eventstore.model.OrderEventsByOrder> findByOrderId(Long orderId);
}
