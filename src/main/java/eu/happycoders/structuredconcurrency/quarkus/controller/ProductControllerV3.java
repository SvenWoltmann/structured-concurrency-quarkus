package eu.happycoders.structuredconcurrency.quarkus.controller;

import eu.happycoders.structuredconcurrency.quarkus.model.Product;
import eu.happycoders.structuredconcurrency.quarkus.model.ProductPageResponse;
import eu.happycoders.structuredconcurrency.quarkus.service.ProductService;
import eu.happycoders.structuredconcurrency.quarkus.service.SupplierService;
import eu.happycoders.structuredconcurrency.quarkus.service.WarehouseService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Future.State;

@Path("/v3/product")
@Produces(MediaType.APPLICATION_JSON)
public class ProductControllerV3 {

  private final ProductService productService;
  private final SupplierService supplierService;
  private final WarehouseService warehouseService;

  public ProductControllerV3(
      ProductService productService,
      SupplierService supplierService,
      WarehouseService warehouseService) {
    this.productService = productService;
    this.supplierService = supplierService;
    this.warehouseService = warehouseService;
  }

  @GET
  @Path("/{productId}")
  @RunOnVirtualThread
  public ProductPageResponse getProduct(@PathParam("productId") String productId) throws Throwable {
    Future<Boolean> availabilityFuture;
    Future<Optional<Product>> productFuture;

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      availabilityFuture =
          executor.submit(
              () -> {
                try {
                  return warehouseService.isAvailable(productId);
                } catch (Exception e) {
                  executor.shutdownNow();
                  throw e;
                }
              });

      productFuture =
          executor.submit(
              () -> {
                try {
                  return productService.getProduct(productId);
                } catch (Exception e) {
                  executor.shutdownNow();
                  throw e;
                }
              });
    }

    if (productFuture.state() == State.SUCCESS && availabilityFuture.state() == State.SUCCESS) {
      Product product = productFuture.resultNow().orElseThrow(NotFoundException::new);
      boolean available = availabilityFuture.resultNow();

      int shipsInDays =
          available ? 0 : supplierService.getDeliveryTime(product.supplier(), productId);

      return new ProductPageResponse(product, shipsInDays);
    } else if (productFuture.state() == State.FAILED
        && !(productFuture.exceptionNow() instanceof InterruptedException)) {
      throw productFuture.exceptionNow();
    } else if (availabilityFuture.state() == State.FAILED) {
      throw availabilityFuture.exceptionNow();
    } else if (productFuture.state() == State.FAILED) {
      throw productFuture.exceptionNow();
    } else {
      throw new IllegalStateException();
    }
  }
}
