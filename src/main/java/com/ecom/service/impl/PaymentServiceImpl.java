package com.ecom.service.impl;

import com.ecom.config.StripeProperties;
import com.ecom.dto.payment.PaymentIntentRequest;
import com.ecom.dto.payment.PaymentIntentResponse;
import com.ecom.dto.payment.PaymentResponse;
import com.ecom.entity.Order;
import com.ecom.entity.Payment;
import com.ecom.entity.enums.OrderStatus;
import com.ecom.entity.enums.PaymentStatus;
import com.ecom.exception.BadRequestException;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.mapper.PaymentMapper;
import com.ecom.repository.OrderRepository;
import com.ecom.repository.PaymentRepository;
import com.ecom.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StripeProperties stripeProperties;

    @Override
    @Transactional
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) {
        // Find the order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        // Check if payment already exists for this order
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BadRequestException("Payment already exists for this order");
        }

        try {
            // Convert amount to cents (Stripe uses smallest currency unit)
            long amountInCents = order.getTotal().multiply(BigDecimal.valueOf(100)).longValue();

            // Create payment intent with Stripe
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(stripeProperties.getCurrency())
                    .putMetadata("orderId", order.getId().toString())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Create and save payment entity
            Payment payment = Payment.builder()
                    .order(order)
                    .stripePaymentIntentId(paymentIntent.getId())
                    .stripeClientSecret(paymentIntent.getClientSecret())
                    .amount(order.getTotal())
                    .currency(stripeProperties.getCurrency())
                    .status(PaymentStatus.PENDING)
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            log.info("Payment intent created successfully for order: {}, paymentIntentId: {}",
                    order.getId(), paymentIntent.getId());

            return PaymentIntentResponse.builder()
                    .paymentId(savedPayment.getId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .amount(order.getTotal())
                    .currency(stripeProperties.getCurrency())
                    .status(paymentIntent.getStatus())
                    .build();

        } catch (StripeException e) {
            log.error("Error creating payment intent for order: {}", request.getOrderId(), e);
            throw new BadRequestException("Failed to create payment intent: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
        return PaymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        return PaymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature for webhook", e);
            throw new BadRequestException("Invalid webhook signature");
        }

        log.info("Received webhook event: {}", event.getType());

        // Handle the event based on type
        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;
            case "payment_intent.canceled":
                handlePaymentIntentCanceled(event);
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new BadRequestException("Failed to deserialize payment intent"));

        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for paymentIntentId: " + paymentIntent.getId()));

        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setPaymentMethodId(paymentIntent.getPaymentMethod());

        // Update order status
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.PROCESSING);

        paymentRepository.save(payment);
        orderRepository.save(order);

        log.info("Payment succeeded for order: {}, paymentIntentId: {}", order.getId(), paymentIntent.getId());
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new BadRequestException("Failed to deserialize payment intent"));

        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for paymentIntentId: " + paymentIntent.getId()));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureMessage(paymentIntent.getLastPaymentError() != null ?
                paymentIntent.getLastPaymentError().getMessage() : "Payment failed");

        paymentRepository.save(payment);

        log.warn("Payment failed for order: {}, paymentIntentId: {}, reason: {}",
                payment.getOrder().getId(), paymentIntent.getId(), payment.getFailureMessage());
    }

    private void handlePaymentIntentCanceled(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new BadRequestException("Failed to deserialize payment intent"));

        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for paymentIntentId: " + paymentIntent.getId()));

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        log.info("Payment cancelled for order: {}, paymentIntentId: {}",
                payment.getOrder().getId(), paymentIntent.getId());
    }
}
