package es.merkle.component.controller;


import es.merkle.component.model.api.ModifyOrderRequest;
import es.merkle.component.service.OrderService;
import es.merkle.component.model.Order;
import es.merkle.component.model.api.CreateOrderRequest;
import es.merkle.component.model.api.SubmitOrderRequest;
import es.merkle.component.model.api.SubmitOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public Order create(@RequestBody CreateOrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    @PostMapping("/modify")
    @ResponseBody
    public Order modify(@RequestBody ModifyOrderRequest orderRequest){
        return orderService.modifyOrder(orderRequest);
    }

    @PostMapping("/submit")
    @ResponseBody
    public SubmitOrderResponse submit(@RequestBody SubmitOrderRequest order) {
        return orderService.submitOrder(order);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<SubmitOrderResponse>> getAllOrders() {
        return new ResponseEntity<>(orderService.getAllOrders(), HttpStatus.OK);
    }

}
