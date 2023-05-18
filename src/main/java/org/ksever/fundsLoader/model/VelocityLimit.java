package org.ksever.fundsLoader.model;

public record VelocityLimit(int maxAmount, Unit unit, Period period) {

    public VelocityLimit {
        if (maxAmount < 0) {
            throw new IllegalArgumentException("maxAmount must be positive");
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit must not be null");
        }
        if (period == null) {
            throw new IllegalArgumentException("period must not be null");
        }
    }

    public String errorMessage() {
        return String.format("Exceeded the maximum limit of %s per %s (limit=%d)", unit().name(), period().name(), maxAmount);
    }
}

