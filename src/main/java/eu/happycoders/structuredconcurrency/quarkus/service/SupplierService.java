package eu.happycoders.structuredconcurrency.quarkus.service;

import static eu.happycoders.structuredconcurrency.quarkus.service.SleepUtil.sleepApproximately;

import eu.happycoders.structuredconcurrency.quarkus.model.Supplier;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class SupplierService {

  public int getDeliveryTime(Supplier supplier, String productId) throws InterruptedException {
    sleepApproximately(500);

    return ThreadLocalRandom.current().nextInt(1, 5);
  }
}
