package eu.happycoders.structuredconcurrency.quarkus.service;

import static eu.happycoders.structuredconcurrency.quarkus.service.SleepUtil.sleepApproximately;

import eu.happycoders.structuredconcurrency.quarkus.model.Product;
import eu.happycoders.structuredconcurrency.quarkus.model.Supplier;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class ProductService {

  private static final Supplier SUPPLIER1 = new Supplier("Supplier 1", "supplier1.example.com");

  private static final Product PRODUCT1 = new Product("1", "Product 1", SUPPLIER1);

  private static final Product PRODUCT2 = new Product("2", "Product 1", SUPPLIER1);

  public Optional<Product> getProduct(String productId) throws InterruptedException {
    if (ThreadLocalRandom.current().nextFloat() > 0.5) {
      sleepApproximately(500);
      throw new RuntimeException("ProductService failed");
    }

    sleepApproximately(1000);

    if (productId.equals(PRODUCT1.productId())) {
      return Optional.of(PRODUCT1);
    } else if (productId.equals(PRODUCT2.productId())) {
      return Optional.of(PRODUCT2);
    } else {
      return Optional.empty();
    }
  }
}
