package com.yapp.web2.domain.account.repository

import com.yapp.web2.domain.account.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<Account, Long> {
    fun findByEmail(email: String): Account?
}