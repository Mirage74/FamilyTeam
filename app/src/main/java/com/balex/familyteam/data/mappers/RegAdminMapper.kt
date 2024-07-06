package com.balex.familyteam.data.mappers

import com.balex.familyteam.domain.entity.Admin

fun mapperFirebaseAdminToEntity(data: Map<String, Any>?): Admin? {
    return data?.let {
        Admin(
            isEmailRegistration = it["isEmailRegistration"] as? Boolean ?: true,
            emailOrPhoneNumber = it["emailOrPhoneNumber"] as? String ?: "",
            isEmailOrPhoneNumberConfirmed = it["isEmailOrPhoneNumberConfirmed"] as? Boolean ?: false
        )
    }
}