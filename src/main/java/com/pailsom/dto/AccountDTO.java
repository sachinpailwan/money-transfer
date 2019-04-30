package com.pailsom.dto;

import com.pailsom.model.Account;

import java.math.BigDecimal;

public class AccountDTO {
    private final int id;
    private final BigDecimal amount;
    private final String name;
    private final String type;
    

    public static AccountDTO fromAccount(Account account) {
        return new AccountDTO(account.getId(), account.getAmount().get(),account.getName(),account.getType());
    }

    public AccountDTO(int accountId, BigDecimal moneyAmount,String name ,String type) {
        this.id = accountId;
        this.amount = moneyAmount;
        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
