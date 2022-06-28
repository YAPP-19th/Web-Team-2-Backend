package com.yapp.web2.common

import com.yapp.web2.util.Message
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class PasswordValidator : ConstraintValidator<CustomPassword, String> {

    override fun isValid(password: String, context: ConstraintValidatorContext?): Boolean {
        // 영문 대소문자, 숫자, 특수문자 중 2종류 이상을 조합하여 8~16자의 비밀번호
        val passwordRegex = "^((?=.*[0-9])(?=.*[a-zA-Z])|(?=.*[0-9])(?=.*[!@#$%^&*?\\-_])|(?=.*[a-zA-Z])(?=.*[!@#$%^&*?\\-_]))(?=\\S+\$).{8,16}\$"
        val regex = Regex(passwordRegex)
        val isValidPassword = password.matches(regex)

        if (!isValidPassword) {
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(Message.PASSWORD_VALID_MESSAGE)
                ?.addConstraintViolation()
        }
        return isValidPassword
    }
}