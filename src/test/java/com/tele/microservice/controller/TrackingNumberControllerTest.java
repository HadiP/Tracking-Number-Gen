package com.tele.microservice.controller;

import com.tele.microservice.dto.ErrorMessage;
import com.tele.microservice.exception.IllegalInputFormatException;
import com.tele.microservice.exception.NumberGeneratorException;
import com.tele.microservice.exception.InvalidDateException;
import com.tele.microservice.service.OrderRecordService;
import com.tele.microservice.service.TrackingNumberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrackingNumberControllerTest {

    @Mock
    private TrackingNumberService trackingNumberService;

    @Mock
    private OrderRecordService orderRecordService;

    @InjectMocks
    private TrackingNumberController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void whenValidRequest_thenReturns200AndBody() throws Exception {
        String expectedTracking = "ABC123XYZ";
        given(trackingNumberService.nextTrackingNumber(any()))
                .willReturn(expectedTracking);

        mockMvc.perform(get("/next-tracking-number")
                        .param("origin_country_id", "MY")
                        .param("destination_country_id", "ID")
                        .param("weight", "1.234")
                        .param("created_at", "2021-06-15T12:34:56+07:00")
                        .param("customer_id", "de619854-b59b-425e-9db4-943979e1bd49")
                        .param("customer_name", "RedBox Logistics")
                        .param("customer_slug", "redbox-logistics")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.tracking_number").value(expectedTracking))
                .andExpect(jsonPath("$.created_at").exists());
    }

    @Test
    void whenInvalidDateFormat_thenReturns400WithErrorMessage() throws Exception {
        mockMvc.perform(get("/next-tracking-number")
                        .param("origin_country_id", "MY")
                        .param("destination_country_id", "ID")
                        .param("weight", "1.234")
                        .param("created_at", "NOT-A-DATE")
                        .param("customer_id", "de619854-b59b-425e-9db4-943979e1bd49")
                        .param("customer_name", "RedBox Logistics")
                        .param("customer_slug", "redbox-logistics")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error_message")
                        .value("created_at must be valid RFC 3339"));
    }

    @Test
    void whenInvalidCountryCode_thenReturns400() throws Exception {
        mockMvc.perform(get("/next-tracking-number")
                        .param("origin_country_id", "XYZ")
                        .param("destination_country_id", "ID")
                        .param("weight", "1.234")
                        .param("created_at", "2021-06-15T12:34:56+07:00")
                        .param("customer_id", "de619854-b59b-425e-9db4-943979e1bd49")
                        .param("customer_name", "RedBox Logistics")
                        .param("customer_slug", "redbox-logistics")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.field")
                        .value("origin_country_id"))
                .andExpect(jsonPath("$.error_message")
                        .value("Origin country must be ISO 3166-1 alpha-2 format"));
    }

    @Test
    void whenServiceThrowsNumberGeneratorException_thenReturns400WithErrorMessage() throws Exception {
        doThrow(new NumberGeneratorException("Generation failed"))
                .when(trackingNumberService)
                .nextTrackingNumber(any());

        mockMvc.perform(get("/next-tracking-number")
                        .param("origin_country_id", "MY")
                        .param("destination_country_id", "ID")
                        .param("weight", "1.234")
                        .param("created_at", "2021-06-15T12:34:56+07:00")
                        .param("customer_id", "de619854-b59b-425e-9db4-943979e1bd49")
                        .param("customer_name", "RedBox Logistics")
                        .param("customer_slug", "redbox-logistics")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error_message")
                        .value("Generation failed"));
    }
}