package com.yapp.web2.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3Config(
    @Value("\${cloud.aws.credentials.accessKey}")
    private val accessKey: String,
    @Value("\${cloud.aws.credentials.secretKey}")
    private val secretKey: String,
    @Value("\${cloud.aws.region.static}")
    private val region: String
) {
    @Bean
    fun amazonS3Client(): AmazonS3Client {
        val awsCreds = BasicAWSCredentials(accessKey, secretKey)
        return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(AWSStaticCredentialsProvider(awsCreds))
            .build() as AmazonS3Client
    }
}