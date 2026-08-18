package com.alam.payment.service;

import com.alam.payment.entity.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentStateMachine {

	public boolean isValidTransition(PaymentStatus current, PaymentStatus next) {

		if (current == null || next == null) {
			return false;
		}

		return switch (current) {

		case PENDING -> next == PaymentStatus.PROCESSING || next == PaymentStatus.FAILED;

		case PROCESSING -> next == PaymentStatus.SUCCESS || next == PaymentStatus.FAILED;

		case SUCCESS -> false;

		case FAILED -> false;
		};
	}
}