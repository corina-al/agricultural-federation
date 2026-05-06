package edu.hei.school.agricultural.controller.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents a financial account. Maps to the OAS3 'oneOf' definition between
 * CashAccount, MobileBankingAccount, and BankAccount.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CashAccount.class, name = "CASH"),
        @JsonSubTypes.Type(value = MobileBankingAccount.class, name = "MOBILE_BANKING"),
        @JsonSubTypes.Type(value = BankAccount.class, name = "BANK")
})
public interface FinancialAccount {
    String getId();
}
