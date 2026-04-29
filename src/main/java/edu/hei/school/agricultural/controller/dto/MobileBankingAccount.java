package edu.hei.school.agricultural.controller.dto;

import edu.hei.school.agricultural.api.model.FinancialAccount;

import java.math.BigDecimal;

public class MobileBankingAccount implements FinancialAccount {

    public String id;
    public String holderName;
    public MobileBankingService mobileBankingService;
    public Integer mobileNumber;
    public BigDecimal amount;
    public String type = "MOBILE_BANKING";

    @Override
    public String toString() {
        return "MobileBankingAccount{" +
                "id='" + id + '\'' +
                ", holderName='" + holderName + '\'' +
                ", mobileBankingService=" + mobileBankingService +
                ", mobileNumber=" + mobileNumber +
                ", amount=" + amount +
                '}';
    }
}