package org.ksever.fundsLoader.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ksever.fundsLoader.exceptions.DeserializeException;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

public record LoadFundsRequest(int id, int customerId, double amount, LocalDateTime timestamp) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public LoadFundsRequest {
        if (id <= 0) {
            throw new IllegalArgumentException("id should be greater than 0");
        }
        if (customerId <= 0) {
            throw new IllegalArgumentException("customerId should be greater than 0");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount should be greater than 0");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp can't be null");
        }
    }

    public static LoadFundsRequest fromJson(String json) throws DeserializeException {
        try {
            var jsonNode = MAPPER.readTree(json);
            int id = Integer.parseInt(jsonNode.get("id").asText());
            int customerId = Integer.parseInt(jsonNode.get("customer_id").asText());
            var amountStr = jsonNode.get("load_amount").asText();
            var currency = amountStr.substring(0,1);
            if (!currency.equals("$")) {
                throw new DeserializeException("Unsupported currency: " + currency);
            }
            var amount = Double.parseDouble(amountStr.substring(1));
            var timestamp = LocalDateTime.parse(jsonNode.get("time").asText(), ISO_ZONED_DATE_TIME);
            return new LoadFundsRequest(id, customerId, amount, timestamp);
        } catch (JsonProcessingException e) {
            throw new DeserializeException(String.format("Failed to parse LoadFundsRequest from JSON: %s", json), e);
        }
    }
}
