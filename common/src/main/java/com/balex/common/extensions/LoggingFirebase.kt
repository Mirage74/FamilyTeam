package com.balex.common.extensions

import com.google.firebase.crashlytics.FirebaseCrashlytics

fun logExceptionToFirebase(text: String, exceptionMessage: String) {
    FirebaseCrashlytics.getInstance()
        .recordException(Exception("$text: $exceptionMessage"))
}

fun logTextToFirebase(text: String) {
    FirebaseCrashlytics.getInstance().log(text)
}

