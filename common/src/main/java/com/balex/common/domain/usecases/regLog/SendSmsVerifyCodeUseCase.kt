package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

class SendSmsVerifyCodeUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    //suspend operator fun invoke(phoneNumber: String,  nickName: String, displayName: String, password: String, activity: Activity) =
        //repository.sendSmsVerifyCode(phoneNumber, nickName, displayName, password, activity)
}