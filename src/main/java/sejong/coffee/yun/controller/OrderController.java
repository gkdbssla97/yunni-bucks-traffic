package sejong.coffee.yun.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sejong.coffee.yun.custom.annotation.MemberId;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.order.OrderPayStatus;
import sejong.coffee.yun.domain.order.OrderStatus;
import sejong.coffee.yun.dto.order.OrderDto;
import sejong.coffee.yun.dto.order.OrderPageDto;
import sejong.coffee.yun.facade.OptimisticLockStockFacade;
import sejong.coffee.yun.mapper.CustomMapper;
import sejong.coffee.yun.service.OrderService;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Validated
public class OrderController {
    private final OrderService orderService;
    private final CustomMapper customMapper;
    private final OptimisticLockStockFacade optimisticLockStockFacade;

    @PostMapping("")
    ResponseEntity<OrderDto.Response> order(@MemberId Long memberId) {

        Order order = orderService.orderWithPessimisticLock(memberId, LocalDateTime.now());

        OrderDto.Response response = customMapper.map(order, OrderDto.Response.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/optimistic")
    ResponseEntity<OrderDto.Response> orderForOptimisticLock(@MemberId Long memberId) throws InterruptedException {

        Order order = optimisticLockStockFacade.order(memberId, LocalDateTime.now());

        OrderDto.Response response = customMapper.map(order, OrderDto.Response.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/cancel")
    ResponseEntity<Void> orderCancel(@RequestParam("orderId") Long orderId) {

        orderService.cancel(orderId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{pageNum}")
    ResponseEntity<OrderPageDto.Response> findAllByMember(@MemberId Long memberId, @PathVariable int pageNum) {

        PageRequest pr = PageRequest.of(pageNum, 10);

        Page<Order> orderPage = orderService.findAllByMemberId(pr, memberId);

        OrderPageDto.Response response = customMapper.map(orderPage, OrderPageDto.Response.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pageNum}/order-status")
    ResponseEntity<OrderPageDto.Response> findOrderStatusAllByMember(@MemberId Long memberId,
                                                                     @PathVariable int pageNum,
                                                                     @RequestParam OrderStatus status) {

        PageRequest pr = PageRequest.of(pageNum, 10);

        Page<Order> orderPage = orderService.findAllByMemberIdAndOrderStatus(pr, memberId, status);

        OrderPageDto.Response response = customMapper.map(orderPage, OrderPageDto.Response.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pageNum}/paid-status")
    ResponseEntity<OrderPageDto.Response> findPayStatusAllByMember(@MemberId Long memberId,
                                                                   @PathVariable int pageNum,
                                                                   @RequestParam OrderPayStatus status) {

        PageRequest pr = PageRequest.of(pageNum, 10);

        Page<Order> orderPage = orderService.findAllByMemberIdAndPayStatus(pr, memberId, status);

        OrderPageDto.Response response = customMapper.map(orderPage, OrderPageDto.Response.class);

        return ResponseEntity.ok(response);
    }
 }
