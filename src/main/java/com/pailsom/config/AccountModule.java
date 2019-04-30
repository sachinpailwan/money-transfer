package com.pailsom.config;

import com.google.inject.AbstractModule;
import com.pailsom.repository.AccountRepository;
import com.pailsom.repository.IAccountRepository;

public class AccountModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(IAccountRepository.class).to(AccountRepository.class);
    }
}
