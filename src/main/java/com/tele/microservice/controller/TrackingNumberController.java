package com.tele.microservice.controller;

import com.tele.microservice.dto.ErrorMessage;
import com.tele.microservice.dto.OrderRequestWrapper;
import com.tele.microservice.dto.OrderResponseWrapper;
import com.tele.microservice.exception.IllegalInputFormatException;
import com.tele.microservice.exception.InvalidDateException;
import com.tele.microservice.exception.NumberGeneratorException;
import com.tele.microservice.service.OrderRecordService;
import com.tele.microservice.service.TrackingNumberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Log4j2
public class TrackingNumberController {

    private final TrackingNumberService trackingNumberService;
    private final OrderRecordService orderRecordService;

    @Operation(
            summary = "Generate next unique tracking number",
            description = "Returns a 1–16 char alphanumeric tracking code and its creation timestamp."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = OrderResponseWrapper.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/next-tracking-number")
    public ResponseEntity<OrderResponseWrapper> getNextTrackingNumber(

            @Parameter(description = "Origin country ISO 3166-1 α-2 (e.g. \"MY\")", required = true, example = "MY")
            @RequestParam("origin_country_id") String originCountryId,

            @Parameter(description = "Destination country ISO 3166-1 α-2 (e.g. \"ID\")", required = true, example = "ID")
            @RequestParam("destination_country_id") String destinationCountryId,

            @Parameter(description = "Order weight in kg, up to 3 decimals (e.g. \"1.234\")", required = true, example = "2.500")
            @RequestParam("weight") double weight,

            @Parameter(description = "Order creation timestamp in RFC 3339 (e.g. \"2018-11-20T19:29:32+08:00\")", required = true)
            @RequestParam("created_at") String createdAtStr,

            @Parameter(description = "Customer UUID", required = true, example = "de619854-b59b-425e-9db4-943979e1bd49")
            @RequestParam("customer_id") UUID customerId,

            @Parameter(description = "Customer name (e.g. \"RedBox Logistics\")", required = true, example = "RedBox Logistics")
            @RequestParam("customer_name") String customerName,

            @Parameter(description = "Customer slug-case (e.g. \"redbox-logistics\")", required = true, example = "redbox-logistics")
            @RequestParam("customer_slug") String customerSlug

    ) throws Exception {
        OffsetDateTime createdAt;
        try {
            createdAt = OffsetDateTime.parse(createdAtStr);
        } catch (DateTimeParseException ex) {
            throw new InvalidDateException("created_at must be valid RFC 3339");
        }

        UUID orderId = UUID.randomUUID();
        var request = new OrderRequestWrapper(
                orderId,
                originCountryId,
                destinationCountryId,
                weight,
                createdAt,
                customerId,
                customerName,
                customerSlug
        );

        String trackingNumber = trackingNumberService.nextTrackingNumber(request);
        orderRecordService.saveOrder(request, trackingNumber);

        var response = new OrderResponseWrapper(trackingNumber, OffsetDateTime.now());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler({
            IllegalInputFormatException.class,
            InvalidDateException.class,
            NumberGeneratorException.class
    })
    public ResponseEntity<ErrorMessage> handleBadRequest(Exception e) {
        log.error("Input error: {}", e.getMessage());
        String field = null;
        if(e instanceof IllegalInputFormatException errInput){
            field = errInput.getField();
        }
        return ResponseEntity
                .badRequest()
                .body(new ErrorMessage(field, e.getMessage()));
    }
}