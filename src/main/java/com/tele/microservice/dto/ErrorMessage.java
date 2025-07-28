package com.tele.microservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorMessage(@JsonProperty("field")String field, @JsonProperty("error_message") String message) {
}
