package eu.happycoders.structuredconcurrency.quarkus.service;

import static eu.happycoders.structuredconcurrency.quarkus.service.SleepUtil.sleepApproximately;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class WarehouseService {

  public boolean isAvailable(String productId) throws InterruptedException {
    if (ThreadLocalRandom.current().nextFloat() > 0.5) {
      sleepApproximately(500);
      throw new RuntimeException("WarehouseService failed");
    }

    sleepApproximately(1000);
    return ThreadLocalRandom.current().nextBoolean();
  }
}
