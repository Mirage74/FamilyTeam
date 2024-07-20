package com.balex.familyteam.data.mappers

import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.RegistrationOption

fun mapperFirebaseAdminToEntity(data: Map<String, Any>?): Admin? {
    return data?.let {
        Admin(
            registrationOption = it["isEmailRegistration"] as? RegistrationOption ?: RegistrationOption.EMAIL,
            emailOrPhoneNumber = it["emailOrPhoneNumber"] as? String ?: "",
            isEmailOrPhoneNumberVerified = it["isEmailOrPhoneNumberConfirmed"] as? Boolean ?: false
        )
    }
}