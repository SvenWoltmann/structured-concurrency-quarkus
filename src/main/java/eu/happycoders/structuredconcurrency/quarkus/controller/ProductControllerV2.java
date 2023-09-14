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

@Path("/v2/product")
@Produces(MediaType.APPLICATION_JSON)
public class ProductControllerV2 {

  private final ProductService productService;
  private final SupplierService supplierService;
  private final WarehouseService warehouseService;

  public ProductControllerV2(
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
      availabilityFuture = executor.submit(() -> warehouseService.isAvailable(productId));
      productFuture = executor.submit(() -> productService.getProduct(productId));
    }

    Product product = productFuture.get().orElseThrow(NotFoundException::new);
    boolean available = availabilityFuture.get();

    int shipsInDays =
        available ? 0 : supplierService.getDeliveryTime(product.supplier(), productId);

    return new ProductPageResponse(product, shipsInDays);
  }
}
