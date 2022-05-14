package com.yapp.web2.common

import com.yapp.web2.util.Message
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class PasswordValidator : ConstraintValidator<CustomPassword, String> {

    override fun isValid(password: String, context: ConstraintValidatorContext): Boolean {
        // 특수문자 + (영문자 or 숫자) 조합으로 8~16자 사이
        val passwordRegex = "^(?=.*[0-9a-zA-Z])(?=.*[!@#\$%^&*])(?=\\S+\$).{8,16}\$"
        val regex = Regex(passwordRegex)
        val isValidPassword = password.matches(regex)

        if (!isValidPassword) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(Message.PASSWORD_VALID_MESSAGE)
                .addConstraintViolation()
        }
        return isValidPassword
    }
}