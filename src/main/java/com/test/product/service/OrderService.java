package com.test.product.service;

import com.test.product.model.entity.Order;
import com.test.product.model.entity.OrderItem;
import com.test.product.model.entity.OrderStatus;
import com.test.product.model.entity.Product;
import com.test.product.model.repository.OrderRepository;
import com.test.product.service.exception.WrongInputException;
import com.test.product.service.job.OrderJobExecutor;
import com.test.product.web.dto.ClientWantedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * this class responsible for main business logic for {@link Order}
 *
 * @author Oleksandr Myronenko
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final OrderItemService orderItemService;
    private final CustomerService customerService;
    private final OrderJobExecutor orderJobExecutor;

    /**
     * @param clientWantedProducts list of client wanted products
     */
    @Transactional
    public Long createOrder(List<ClientWantedProduct> clientWantedProducts) {
        Order order = new Order();
        order.setOrderStatus(OrderStatus.CREATED);
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = new BigDecimal(0);
        for (ClientWantedProduct clientWantedProduct : clientWantedProducts) {
            Product product = retrieveProductFromClientWantedProduct(clientWantedProduct);
            Integer wantedProductQuantity = clientWantedProduct.getWantedProductQuantity();
            orderItemService.createOrderItem(order, product, wantedProductQuantity);
            log.info("order item created");
            Long orderItemId = orderItemService.createOrderItem(order, product, wantedProductQuantity);
            orderItems.add(orderItemService.getOrderItemById(orderItemId));
            log.info("order item was added client wanted products");
            productService.decreaseQuantityProduct(product.getId(), wantedProductQuantity);
            log.info("product quantity was decreased");
            totalPrice = totalPrice.add(BigDecimal.valueOf(product.getProductPrice()));
            log.info("total price was increased -> {}", totalPrice);
            Long clientId = clientWantedProduct.getClientId();
            order.setOrderBuyerCustomer(customerService.getCustomerById(clientId));
            log.info("setting buyer id -> {}", clientId);

        }
        order.setOrderTotalPrice(totalPrice);
        order.setOrderItems(orderItems);
        orderRepository.save(order);
        log.info("schedule check for not paid order");
        orderJobExecutor.scheduleCheckNotPaidOrder(order);
        log.info("order was saved");
        return order.getId();
    }

    /**
     * using for paying order by its id
     *
     * @param orderId order id
     */
    public void payOrderById(Long orderId) {
        Order order = getOrderById(orderId);
        order.setOrderPurchasedTime(Instant.now());
        order.setOrderStatus(OrderStatus.PAID);
        orderRepository.save(order);
        log.info("order was paid -> {}", order.getOrderStatus());
    }

    public Order getOrderById(Long orderId) {
        return orderRepository
                .findById(orderId)
                .orElseThrow(() -> new WrongInputException("such order by this id does not exist, id - " + orderId));
    }

    private Product retrieveProductFromClientWantedProduct(ClientWantedProduct clientWantedProduct) {
        Long clientWantedProductId = clientWantedProduct.getProductId();
        log.info("get client wanted product id -> {}", clientWantedProductId);
        Product product = productService.getProductById(clientWantedProductId);
        log.info("get product by id -> {}", product);
        return product;
    }
}
