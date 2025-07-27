package com.tele.microservice.service;

import com.tele.microservice.dto.OrderRequestWrapper;

public interface TrackingNumberService {

    String nextTrackingNumber(OrderRequestWrapper orderRequest) throws Exception;

}
