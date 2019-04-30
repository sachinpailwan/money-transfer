package com.pailsom.repository;

import com.pailsom.dto.AccountDTO;
import com.pailsom.exceptions.InvalidDepositAmountException;
import com.pailsom.exceptions.InvalidTransferException;
import com.pailsom.exceptions.NotEnoughMoneyException;
import com.pailsom.model.Account;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Singleton;

import static com.google.inject.internal.util.Lists.newArrayList;

@Singleton
public class AccountRepository implements IAccountRepository{
    private AtomicInteger userCounter = new AtomicInteger(0);
    private Map<Integer, Account> users = new ConcurrentHashMap<>();
    private Map<Integer, Object> locks = new ConcurrentHashMap<>();

    @Override
    public Account createAccount(String name,String type) {
        int id = userCounter.incrementAndGet();
        Account newAccount = new Account(id, new AtomicReference<>(BigDecimal.ZERO),name,type);
        users.put(id, new Account(id, new AtomicReference<>(BigDecimal.ZERO),name,type));
        return newAccount;
    }

    public Account deleteAccount(int id) {
        return users.remove(id);
    }

    @Override
    public AccountDTO deposit(int id, BigDecimal amount) throws InvalidDepositAmountException {
        Account account = users.get(id);
        if (account == null) return null;
        if(null==amount || amount.compareTo(new BigDecimal("0"))<0)
            throw new InvalidDepositAmountException();
        account.getAmount().updateAndGet(current -> current.add(amount));
        return AccountDTO.fromAccount(account);
    }

    @Override
    public List<AccountDTO> transfer(Integer from, Integer to, BigDecimal amount) throws NotEnoughMoneyException, InvalidTransferException {
        if (from == null || to == null) return null;
        if (Objects.equals(from,to)) throw new InvalidTransferException();
        Account accountFrom = users.get(from);
        Account accountTo = users.get(to);
        if (accountFrom == null || accountTo == null) return null;
        synchronized (locks.computeIfAbsent(Math.min(from, to), integer -> new Object())) {
            synchronized (locks.computeIfAbsent(Math.max(from, to), integer -> new Object())) {
                if (accountFrom.getAmount().get().compareTo(amount) < 0)
                    throw new NotEnoughMoneyException("User with id " + from + " has not enough money to transfer to user with id " + to);
                accountFrom.getAmount().updateAndGet(current -> current.add(amount.negate()));
                accountTo.getAmount().updateAndGet(current -> current.add(amount));
                return newArrayList(AccountDTO.fromAccount(accountFrom), AccountDTO.fromAccount(accountTo));

            }
        }

    }


    public AccountDTO getUser(int account) {
        return AccountDTO.fromAccount(users.get(account));
    }
}
