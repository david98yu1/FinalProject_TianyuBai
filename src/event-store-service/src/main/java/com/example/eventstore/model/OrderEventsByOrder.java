package com.example.eventstore.model;

import java.time.Instant;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Table("order_events_by_order")
public class OrderEventsByOrder {
    @PrimaryKeyColumn(name = "order_id", type = PrimaryKeyType.PARTITIONED)
    private Long orderId;

    @PrimaryKeyColumn(name = "event_time", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private Instant eventTime;

    private String eventType;
    private String payload;   // raw JSON string of event data
}
