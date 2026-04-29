package edu.hei.school.agricultural.controller.dto;

import edu.hei.school.agricultural.api.model.FinancialAccount;

public class CashAccount implements FinancialAccount {

    public String id;
    public Integer amount;
    public String type= "CASH";

    @Override
    public String toString() {
        return "CashAccount{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                '}';
    }
}