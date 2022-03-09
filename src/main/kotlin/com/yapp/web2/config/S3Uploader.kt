package com.yapp.web2.config

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.PutObjectRequest
import com.yapp.web2.exception.BusinessException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.*

@Component
class S3Uploader(
    private val amazonS3Client: AmazonS3Client,
    @Value("\${cloud.aws.s3.bucket}")
    val bucket: String
) {
    fun upload(multiPartFile: MultipartFile, dirName: String): String {
        val uploadFile: File = convert(multiPartFile)

        val url = upload(uploadFile, dirName)
        removeNewFile(uploadFile)
        return url
    }

    private fun upload(uploadFile: File, dirName: String): String {
        val fileName: String = dirName + "/" + UUID.randomUUID()
        return putS3(uploadFile, fileName)
    }

    private fun putS3(uploadFile: File, fileName: String): String {
        amazonS3Client.putObject(PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead))
        return amazonS3Client.getUrl(bucket, fileName).toString()
    }

    private fun removeNewFile(targetFile: File) {
        if (targetFile.delete()) return
        throw BusinessException("삭제하려는 파일이 존재하지 않습니다")
    }

    private fun convert(file: MultipartFile): File {
        val convertFile = File(System.getProperty("user.dir") + "/" + file.originalFilename)
        val contentType = Files.probeContentType(convertFile.toPath())
        if (!contentType.startsWith("image")) throw BusinessException("이미지가 아닙니다")

        convertFile.createNewFile()
        val fos = FileOutputStream(convertFile)
        fos.write(file.bytes)

        return convertFile
    }
}