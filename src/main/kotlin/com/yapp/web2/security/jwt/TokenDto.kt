package com.yapp.web2.security.jwt

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel
data class TokenDto(

    @ApiModelProperty(value = "AccessToken", example = "yJhbGclOaJIUzUxMiJ9. ...")
    val accessToken: String,

    @ApiModelProperty(value = "RefreshToken", example = "xlnGclOaJIXzUxMiJ9. ...")
    val refreshToken: String
)