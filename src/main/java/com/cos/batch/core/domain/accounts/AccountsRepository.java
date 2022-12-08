package com.cos.batch.core.domain.accounts;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepository  extends JpaRepository<Accounts, Integer> {
}