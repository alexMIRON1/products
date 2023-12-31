package com.test.product.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * entity for table order
 *
 * @author Oleksandr Myronenko
 */
@Entity
@Table(name = "order", schema = "shop")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;
    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;
    @Column(name = "order_purchased_time")
    private Instant orderPurchasedTime;
    @Column(name = "order_total_price")
    private BigDecimal orderTotalPrice;
    @ManyToOne
    @JoinColumn(name = "order_buyer_customer_id")
    private Customer orderBuyerCustomer;
}
