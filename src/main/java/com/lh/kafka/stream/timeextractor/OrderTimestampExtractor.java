package com.lh.kafka.stream.timeextractor;

import com.fasterxml.jackson.databind.JsonNode;
import com.lh.kafka.model.Item;
import com.lh.kafka.model.Order;
import com.lh.kafka.model.User;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class OrderTimestampExtractor implements TimestampExtractor {

	@Override
	public long extract(ConsumerRecord<Object, Object> record) {
		Object value = record.value();
		if (record.value() instanceof Order) {
			Order order = (Order) value;
			return order.getTransactionDate();
		}
		if (value instanceof JsonNode) {
			return ((JsonNode) record.value()).get("transactionDate").longValue();
		}
		if (value instanceof Item) {
			return LocalDateTime.of(2017, 3,1,11,11,11).toEpochSecond(ZoneOffset.UTC) * 1000;
		}
		if (value instanceof User) {
			return LocalDateTime.of(2017, 3,1,11,11,11).toEpochSecond(ZoneOffset.UTC) * 1000;
		}
		return LocalDateTime.of(2017, 3,1,11,11,11).toEpochSecond(ZoneOffset.UTC) * 1000;
	}

}
