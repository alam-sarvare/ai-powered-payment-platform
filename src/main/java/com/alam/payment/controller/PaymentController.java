package com.alam.payment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.alam.payment.request.dto.CreatePaymentRequest;
import com.alam.payment.response.dto.PaymentResponse;
import com.alam.payment.response.dto.UpdatePaymentStatusRequest;
import com.alam.payment.service.PaymentService;

import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
	private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        return paymentService.createPayment(request);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(@PathVariable UUID paymentId) {

        return paymentService.getPayment(paymentId);
    }

    @GetMapping
    public List<PaymentResponse> getPaymentsByCustomer(@RequestParam String customerId) {

        return paymentService.getPaymentsByCustomer(customerId);
    }
    
    @PatchMapping("/{paymentId}/status")
    public PaymentResponse updateStatus(@PathVariable UUID paymentId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {

        return paymentService.updateStatus(paymentId,request
        );
    }

}
