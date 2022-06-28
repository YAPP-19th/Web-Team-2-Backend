package com.yapp.web2.domain.account

import com.yapp.web2.domain.account.entity.Account

class AccountDto {
    companion object {
        fun accountToFolderBelongAccountInfo(account: Account): FolderBelongAccountInfo {
            return FolderBelongAccountInfo(account.id!!, account.name, account.image)
        }
    }

    data class FolderBelongAccountListDto(
        val list: MutableList<FolderBelongAccountInfo>
    )

    data class FolderBelongAccountInfo(
        val id : Long,
        val name : String,
        val profileImage : String
    )
}