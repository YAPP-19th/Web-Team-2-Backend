package com.yapp.web2.common

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [PasswordValidator::class])
@Target(ANNOTATION_CLASS, CLASS, CONSTRUCTOR, FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CustomPassword(

    val message: String = "영문 대소문자, 숫자, 특수문자 중 2종류 이상을 조합하여 8~16자의 비밀번호를 생성해주세요.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

