package eu.happycoders.structuredconcurrency.quarkus.model;

public record ProductPageResponse(Product product, int daysUntilShippable) {}
