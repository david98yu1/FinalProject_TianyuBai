package com.example.eventstore.model;

import java.time.Instant;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Table("order_events_by_account")
public class OrderEventsByAccount {
    @PrimaryKeyColumn(name = "account_id", type = PrimaryKeyType.PARTITIONED)
    private Long accountId;

    @PrimaryKeyColumn(name = "event_time", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Instant eventTime;

    @PrimaryKeyColumn(name = "order_id", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Long orderId;

    private String eventType;
    private String payload;
}
