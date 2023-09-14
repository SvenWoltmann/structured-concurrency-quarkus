package eu.happycoders.structuredconcurrency.quarkus.model;

public record Product(String productId, String name, Supplier supplier) {}
