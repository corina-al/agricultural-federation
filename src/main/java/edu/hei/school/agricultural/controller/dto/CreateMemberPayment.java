package edu.hei.school.agricultural.controller.dto;

import edu.hei.school.agricultural.api.model.PaymentMode;

public class CreateMemberPayment {

    public Integer amount;
    public String membershipFeeIdentifier;
    public String accountCreditedIdentifier;
    public PaymentMode paymentMode;
}