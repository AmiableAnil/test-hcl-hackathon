package com.fulfilment.application.monolith.fulfillment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("fulfillment")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FulfillmentResource {

  @Inject FulfillmentService fulfillmentService;

  @GET
  public List<FulfillmentAssociationDto> getAll(
      @QueryParam("productId") Long productId,
      @QueryParam("warehouseCode") String warehouseCode,
      @QueryParam("storeId") Long storeId) {

    List<FulfillmentAssociation> associations;

    if (productId != null) {
      associations = fulfillmentService.getByProduct(productId);
    } else if (warehouseCode != null) {
      associations = fulfillmentService.getByWarehouse(warehouseCode);
    } else if (storeId != null) {
      associations = fulfillmentService.getByStore(storeId);
    } else {
      associations = fulfillmentService.getAll();
    }

    return associations.stream().map(this::toDto).toList();
  }

  @POST
  @Transactional
  public Response create(FulfillmentAssociationDto dto) {
    try {
      FulfillmentAssociation created =
          fulfillmentService.createAssociation(dto.productId, dto.warehouseCode, dto.storeId);
      return Response.status(Response.Status.CREATED).entity(toDto(created)).build();
    } catch (FulfillmentValidationException e) {
      throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
    }
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("id") Long id) {
    try {
      fulfillmentService.deleteAssociation(id);
      return Response.noContent().build();
    } catch (FulfillmentValidationException e) {
      throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
    }
  }

  private FulfillmentAssociationDto toDto(FulfillmentAssociation association) {
    FulfillmentAssociationDto dto = new FulfillmentAssociationDto();
    dto.id = association.id;
    dto.productId = association.productId;
    dto.warehouseCode = association.warehouseCode;
    dto.storeId = association.storeId;
    return dto;
  }

  public static class FulfillmentAssociationDto {
    public Long id;
    public Long productId;
    public String warehouseCode;
    public Long storeId;
  }
}
