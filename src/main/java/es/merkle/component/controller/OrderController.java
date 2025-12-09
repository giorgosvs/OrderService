package es.merkle.component.controller;


import es.merkle.component.exception.InvalidOrderException;
import es.merkle.component.model.api.ModifyOrderRequest;
import es.merkle.component.service.OrderService;
import es.merkle.component.model.Order;
import es.merkle.component.model.api.CreateOrderRequest;
import es.merkle.component.model.api.SubmitOrderRequest;
import es.merkle.component.model.api.SubmitOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "order-service")
@RequiredArgsConstructor
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Order> create(@RequestBody CreateOrderRequest orderRequest) {
        //use @Valid
        if(orderRequest.getCustomerId() == null || orderRequest.getCustomerId().isBlank()) {
            throw new InvalidOrderException("customerId must be provided");
        }
        Order order = orderService.createOrder(orderRequest);
        return new ResponseEntity<>(order, HttpStatus.OK); //should be 201 CREATED
    }

    @PostMapping("/modify")
    @ResponseBody
    public ResponseEntity<Order> modify(@RequestBody ModifyOrderRequest orderRequest){
        Order order = orderService.modifyOrder(orderRequest);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<SubmitOrderResponse> submit(@RequestBody SubmitOrderRequest order) {
        SubmitOrderResponse response = orderService.submitOrder(order);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

}
