package org.ksever.fundsLoader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.ksever.fundsLoader.exceptions.SerializeException;

public record LoadFundsResponse(@JsonSerialize(using = ToStringSerializer.class)
                                @JsonProperty("id")
                                int id,
                                @JsonSerialize(using = ToStringSerializer.class)
                                @JsonProperty("customer_id")
                                int customerId,
                                boolean accepted) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(LoadFundsResponse response) throws SerializeException {
        try {
            return MAPPER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new SerializeException("Failed to serialize response:" + response, e);
        }
    }
}
