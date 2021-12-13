package com.yapp.web2.domain.folder.repository

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.folder.entity.Folder
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FolderRepository : JpaRepository<Folder, Long> {

    @Query("SELECT f FROM Folder f WHERE f.parentFolder = ?1 and f.index > ?2")
    fun findByIndexGreaterThanPrevFolder(parent: Folder, index: Int): MutableList<Folder>?

    @Query("SELECT f FROM Folder f WHERE f.parentFolder = ?1 and f.index >= ?2")
    fun findByIndexGreaterThanNextFolder(parent: Folder, index: Int): MutableList<Folder>?

    //@Query("SELECT f FROM Folder f join fetch f.children WHERE f.parentFolder is null ")
    @EntityGraph(attributePaths = ["children"])
    @Query("SELECT f FROM Folder f WHERE f.parentFolder is null")
    fun findAllByParentFolderIsNull(): MutableList<Folder>

    @EntityGraph(attributePaths = ["children"])
    @Query(
        "SELECT f FROM Folder f WHERE f.id in " +
                "(  SELECT af.folder " +
                "   FROM AccountFolder af " +
                "   WHERE af.account = ?1)" +
                " AND f.parentFolder IS NULL " +
                " ORDER BY f.index"
    )
    fun findAllByParentFolderIsNull(user: Account): MutableList<Folder>

    @EntityGraph(attributePaths = ["children"])
    @Query(
        "SELECT f FROM Folder as f " +
                "JOIN AccountFolder as af " +
                "ON f.id = af.folder.id " +
                "WHERE af.account = ?1"
    )
    fun findAllByAccount(user: Account): MutableList<Folder>

    @EntityGraph(attributePaths = ["parentFolder", "children", "folders"])
    @Modifying
    @Query("DELETE FROM Folder f WHERE f = ?1")
    fun deleteByFolder(folder: Folder)
}