package com.balex.common.extensions

const val REGEX_PATTERN_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$"
const val REGEX_PATTERN_NOT_NUMBERS = "\\D"
const val REGEX_PATTERN_ONLY_NUMBERS_FIRST_NOT_ZERO = "^[1-9]\\d*$"
const val REGEX_PATTERN_NOT_LETTERS = "[^a-zA-Z]"
const val REGEX_PATTERN_NOT_LATIN_LETTERS_NUMBERS_UNDERSCORE = "[^a-zA-Z0-9_]"
const val REGEX_PATTERN_NOT_ANY_LETTERS_NUMBERS_UNDERSCORE = """[^\p{L}\p{Nd}_]"""