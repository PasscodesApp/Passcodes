package com.jeeldobariya.passcodes.autofill.identity

import com.jeeldobariya.passcodes.autofill.information.AutofillInformation
import java.time.ZoneId

/** What a planned dataset is for; the service maps this to a title string. */
enum class PlanKind { CONTACT_BUNDLE, EMAIL_ALTERNATIVE, PHONE_ALTERNATIVE, BIRTH_DATE, ADDRESS }

data class PlannedField<T>(val id: T, val spec: FieldSpec)

/**
 * One suggestion the user can tap. [summary] is what an *unlocked*
 * suggestion previews ("Jane · jane@x.com"); a locked suggestion must
 * never show it.
 */
data class PlannedDataset<T>(
    val kind: PlanKind,
    val partition: IdentityPartition,
    val selection: Selection,
    val summary: String,
    val fields: List<PlannedField<T>>
)

/**
 * Decides which datasets to offer for the identity fields found on a
 * form. Each dataset stays inside one partition, and only contains
 * fields we can actually fill right now.
 *
 *  - contact: one "fill my details" bundle of the default email/phone/
 *    name, plus one single-purpose dataset per additional email / phone;
 *  - address: its own dataset;
 *  - birth date: its own dataset.
 */
object IdentityDatasetPlanner {

    fun <T> plan(
        info: AutofillInformation,
        fields: List<PlannedField<T>>,
        maxDatasets: Int,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<PlannedDataset<T>> {
        if (info.isEmpty || fields.isEmpty() || maxDatasets <= 0) return emptyList()

        val planned = mutableListOf<PlannedDataset<T>>()

        fun resolvable(selection: Selection, candidates: List<PlannedField<T>>): List<Pair<PlannedField<T>, FillValue>> =
            candidates.mapNotNull { f ->
                IdentityFiller.resolve(info, selection, f.spec, zone)?.let { f to it }
            }

        fun byPartition(p: IdentityPartition) = fields.filter { it.spec.kind.partition == p }

        // Contact bundle (default email / phone).
        val contact = byPartition(IdentityPartition.CONTACT)
        val bundle = resolvable(Selection(), contact)
        if (bundle.isNotEmpty()) {
            planned += PlannedDataset(
                kind = PlanKind.CONTACT_BUNDLE,
                partition = IdentityPartition.CONTACT,
                selection = Selection(),
                summary = contactSummary(bundle),
                fields = bundle.map { it.first }
            )
        }

        // Address.
        val address = resolvable(Selection(), byPartition(IdentityPartition.ADDRESS))
        if (address.isNotEmpty()) {
            planned += PlannedDataset(
                kind = PlanKind.ADDRESS,
                partition = IdentityPartition.ADDRESS,
                selection = Selection(),
                summary = info.effectiveFullAddress().take(SUMMARY_MAX),
                fields = address.map { it.first }
            )
        }

        // Birth date.
        val birth = resolvable(Selection(), byPartition(IdentityPartition.BIRTH_DATE))
        if (birth.isNotEmpty()) {
            planned += PlannedDataset(
                kind = PlanKind.BIRTH_DATE,
                partition = IdentityPartition.BIRTH_DATE,
                selection = Selection(),
                summary = info.birthDate,
                fields = birth.map { it.first }
            )
        }

        // Additional emails / phones, each only for its own fields.
        val emailFields = contact.filter { it.spec.kind == IdentityFieldKind.EMAIL }
        info.orderedEmails().forEachIndexed { index, email ->
            if (index == 0 || emailFields.isEmpty()) return@forEachIndexed
            val selection = Selection(emailIndex = index)
            val filled = resolvable(selection, emailFields)
            if (filled.isNotEmpty()) {
                planned += PlannedDataset(
                    kind = PlanKind.EMAIL_ALTERNATIVE,
                    partition = IdentityPartition.CONTACT,
                    selection = selection,
                    summary = labelled(email.label, email.address),
                    fields = filled.map { it.first }
                )
            }
        }

        val phoneFields = contact.filter { it.spec.kind.isPhone }
        info.orderedPhones().forEachIndexed { index, phone ->
            if (index == 0 || phoneFields.isEmpty()) return@forEachIndexed
            val selection = Selection(phoneIndex = index)
            val filled = resolvable(selection, phoneFields)
            if (filled.isNotEmpty()) {
                val shown = (if (phone.countryCode.isNotEmpty()) "+${phone.countryCode} " else "") + phone.number
                planned += PlannedDataset(
                    kind = PlanKind.PHONE_ALTERNATIVE,
                    partition = IdentityPartition.CONTACT,
                    selection = selection,
                    summary = labelled(phone.label, shown),
                    fields = filled.map { it.first }
                )
            }
        }

        return planned.take(maxDatasets)
    }

    private fun labelled(label: String, value: String) = if (label.isBlank()) value else "$label · $value"

    /** Up to two of name / email / phone, in that order. */
    private fun <T> contactSummary(resolved: List<Pair<PlannedField<T>, FillValue>>): String {
        val order = listOf(
            listOf(IdentityFieldKind.FULL_NAME),
            listOf(IdentityFieldKind.FIRST_NAME, IdentityFieldKind.LAST_NAME),
            listOf(IdentityFieldKind.EMAIL),
            listOf(IdentityFieldKind.PHONE_FULL, IdentityFieldKind.PHONE_NATIONAL)
        )
        val parts = mutableListOf<String>()
        for (group in order) {
            val texts = group.mapNotNull { kind ->
                (resolved.firstOrNull { it.first.spec.kind == kind }?.second as? FillValue.Text)?.value
            }
            if (texts.isNotEmpty()) parts += texts.joinToString(" ")
            if (parts.size == 2) break
        }
        return parts.joinToString(" · ").take(SUMMARY_MAX)
    }

    private const val SUMMARY_MAX = 60
}
