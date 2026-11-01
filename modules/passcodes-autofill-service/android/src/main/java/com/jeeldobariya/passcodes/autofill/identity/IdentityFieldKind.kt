package com.jeeldobariya.passcodes.autofill.identity

/**
 * Datasets never mix partitions: contact details, the birth date and
 * the postal address are offered (and filled) separately, so focusing
 * an email field never exposes the address.
 */
enum class IdentityPartition { CONTACT, BIRTH_DATE, ADDRESS }

/**
 * What a form field asks for. [minConfidence] is how sure the classifier
 * must be before we offer to fill it: contact fields are low-risk, the
 * birth date and address need stronger evidence (two agreeing signals,
 * or an explicit hint).
 */
enum class IdentityFieldKind(val partition: IdentityPartition, val minConfidence: Int) {
    FULL_NAME(IdentityPartition.CONTACT, 40),
    FIRST_NAME(IdentityPartition.CONTACT, 40),
    LAST_NAME(IdentityPartition.CONTACT, 40),
    EMAIL(IdentityPartition.CONTACT, 40),
    PHONE_FULL(IdentityPartition.CONTACT, 40),
    PHONE_NATIONAL(IdentityPartition.CONTACT, 40),
    PHONE_COUNTRY_CODE(IdentityPartition.CONTACT, 40),

    BIRTH_DATE(IdentityPartition.BIRTH_DATE, 55),

    ADDRESS_FULL(IdentityPartition.ADDRESS, 55),
    STREET(IdentityPartition.ADDRESS, 55),
    STREET2(IdentityPartition.ADDRESS, 55),
    CITY(IdentityPartition.ADDRESS, 55),
    REGION(IdentityPartition.ADDRESS, 55),
    POSTAL_CODE(IdentityPartition.ADDRESS, 55),
    COUNTRY(IdentityPartition.ADDRESS, 55);

    val isPhone: Boolean get() = this == PHONE_FULL || this == PHONE_NATIONAL || this == PHONE_COUNTRY_CODE
}
