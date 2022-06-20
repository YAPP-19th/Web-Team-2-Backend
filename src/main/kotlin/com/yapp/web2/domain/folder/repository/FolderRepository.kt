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

    fun findFolderById(id: Long): Folder?

    @Query("SELECT f FROM Folder f WHERE f.parentFolder = :parent and f.index > :index")
    fun findByIndexGreaterThanPrevFolder(parent: Folder, index: Int): MutableList<Folder>?

    @Query("SELECT f FROM Folder f WHERE f.parentFolder = :parent and f.index >= :index")
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
            "   WHERE af.account = :user)" +
            " AND f.parentFolder IS NULL " +
            " ORDER BY f.index"
    )
    fun findAllByParentFolderIsNull(user: Account): MutableList<Folder>

    @EntityGraph(attributePaths = ["children"])
    @Query(
        "SELECT f FROM Folder as f " +
            "JOIN AccountFolder as af " +
            "ON f.id = af.folder.id " +
            "WHERE af.account = :user"
    )
    fun findAllByAccount(user: Account): MutableList<Folder>

    @EntityGraph(attributePaths = ["parentFolder", "children", "folders"])
    @Modifying
    @Query("DELETE FROM Folder f WHERE f = :folder")
    fun deleteByFolder(folder: Folder)

    @Query("SELECT COUNT(f) FROM Folder as f " +
            "JOIN AccountFolder as af " +
            "ON f.id = af.folder.id " +
            "WHERE af.account.id = :userId " +
            "AND f.parentFolder is null")
    fun findAllByParentFolderCount(userId: Long): Int

    @Query("SELECT COUNT(f) FROM Folder as f " +
        "JOIN AccountFolder as af " +
        "ON f.id = af.folder.id " +
        "WHERE af.account.id = :userId " +
        "AND f.parentFolder.id = :parentId")
    fun findAllByFolderCount(userId: Long, parentId: Long): Int
}