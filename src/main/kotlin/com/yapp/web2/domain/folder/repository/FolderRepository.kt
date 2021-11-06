package com.yapp.web2.domain.folder.repository

import com.yapp.web2.domain.folder.entity.Folder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FolderRepository : JpaRepository<Folder, Long> {

}