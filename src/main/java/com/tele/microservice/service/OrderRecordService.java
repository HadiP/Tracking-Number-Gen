package com.tele.microservice.service;

import com.tele.microservice.dto.OrderRequestWrapper;
import com.tele.microservice.entity.OrderRecord;

public interface OrderRecordService {

    OrderRecord saveOrder(OrderRequestWrapper orderRequest, String trackingNumber);

}
