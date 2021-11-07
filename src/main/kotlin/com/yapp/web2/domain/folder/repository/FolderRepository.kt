package com.yapp.web2.domain.folder.repository

import com.yapp.web2.domain.folder.entity.Folder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FolderRepository : JpaRepository<Folder, Long> {

    @Query("SELECT f FROM Folder f WHERE f.parentFolder = ?1 and f.index > ?2")
    fun findByIndexGreaterThan(parent: Folder, index: Int): MutableList<Folder>?
}