package com.pailsom.repository;

import com.pailsom.dto.AccountDTO;
import com.pailsom.exceptions.InvalidDepositAmountException;
import com.pailsom.exceptions.InvalidTransferException;
import com.pailsom.exceptions.NotEnoughMoneyException;
import com.pailsom.model.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IAccountRepository {
    public Account createAccount(String name,String type);
    public AccountDTO deposit(int id, BigDecimal amount) throws InvalidDepositAmountException;
    public List<AccountDTO> transfer(Integer from, Integer to, BigDecimal amount) throws NotEnoughMoneyException, InvalidTransferException;
    public AccountDTO getUser(int id);
}
