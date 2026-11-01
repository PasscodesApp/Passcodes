package com.jeeldobariya.passcodes.autofill.settings

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.jeeldobariya.passcodes.autofill.R
import com.jeeldobariya.passcodes.autofill.information.AddressInfo
import com.jeeldobariya.passcodes.autofill.information.AutofillInformation
import com.jeeldobariya.passcodes.autofill.information.AutofillInformationStore
import com.jeeldobariya.passcodes.autofill.information.EmailEntry
import com.jeeldobariya.passcodes.autofill.information.PhoneEntry
import com.jeeldobariya.passcodes.autofill.security.DeviceSecurity
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Where the personal details used for Autofill (name, phone numbers,
 * emails, birth date, address) are entered — entirely on-device. There
 * is no React Native equivalent of this screen: the data is written
 * straight to [AutofillInformationStore] from here, and RN only ever
 * gets a read-only copy if it asks for one.
 *
 * Reached from [AutofillSettingsActivity] by tapping the "Personal
 * details" card.
 */
class AutofillPersonalDetailsActivity : AppCompatActivity() {

    private lateinit var lockWarningCard: MaterialCardView
    private lateinit var firstNameField: TextInputEditText
    private lateinit var lastNameField: TextInputEditText
    private lateinit var fullNameField: TextInputEditText
    private lateinit var birthDateRow: View
    private lateinit var birthDateValue: TextView
    private lateinit var birthDateClear: ImageButton
    private lateinit var phonesContainer: LinearLayout
    private lateinit var phonesLimit: TextView
    private lateinit var addPhoneButton: MaterialButton
    private lateinit var emailsContainer: LinearLayout
    private lateinit var emailsLimit: TextView
    private lateinit var addEmailButton: MaterialButton
    private lateinit var streetField: TextInputEditText
    private lateinit var street2Field: TextInputEditText
    private lateinit var cityField: TextInputEditText
    private lateinit var regionField: TextInputEditText
    private lateinit var postalCodeField: TextInputEditText
    private lateinit var countryField: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var clearButton: MaterialButton

    /** ISO "yyyy-MM-dd", or null until the user picks one. */
    private var birthDateIso: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autofill_personal_details)
        WindowCompat.enableEdgeToEdge(window)

        bindViews()

        findViewById<MaterialToolbar>(R.id.details_toolbar).setNavigationOnClickListener { finish() }

        lockWarningCard.visibility = if (DeviceSecurity.isDeviceSecure(this)) View.GONE else View.VISIBLE

        birthDateRow.setOnClickListener { showDatePicker() }
        birthDateClear.setOnClickListener { setBirthDate(null) }

        addPhoneButton.setOnClickListener { addPhoneRow(PhoneEntry()) }
        addEmailButton.setOnClickListener { addEmailRow(EmailEntry()) }

        saveButton.isEnabled = false
        clearButton.isEnabled = false
        saveButton.setOnClickListener { save() }
        clearButton.setOnClickListener { confirmClear() }

        loadExistingDetails()
    }

    private fun bindViews() {
        lockWarningCard = findViewById(R.id.details_lock_warning_card)
        firstNameField = findViewById(R.id.details_first_name)
        lastNameField = findViewById(R.id.details_last_name)
        fullNameField = findViewById(R.id.details_full_name)
        birthDateRow = findViewById(R.id.details_birth_date_row)
        birthDateValue = findViewById(R.id.details_birth_date_value)
        birthDateClear = findViewById(R.id.details_birth_date_clear)
        phonesContainer = findViewById(R.id.details_phones_container)
        phonesLimit = findViewById(R.id.details_phones_limit)
        addPhoneButton = findViewById(R.id.details_add_phone)
        emailsContainer = findViewById(R.id.details_emails_container)
        emailsLimit = findViewById(R.id.details_emails_limit)
        addEmailButton = findViewById(R.id.details_add_email)
        streetField = findViewById(R.id.details_address_street)
        street2Field = findViewById(R.id.details_address_street2)
        cityField = findViewById(R.id.details_address_city)
        regionField = findViewById(R.id.details_address_region)
        postalCodeField = findViewById(R.id.details_address_postal_code)
        countryField = findViewById(R.id.details_address_country)
        saveButton = findViewById(R.id.details_save_button)
        clearButton = findViewById(R.id.details_clear_button)
    }

    private fun loadExistingDetails() {
        lifecycleScope.launch {
            val info = AutofillInformationStore.get(applicationContext)

            firstNameField.setText(info.firstName)
            lastNameField.setText(info.lastName)
            fullNameField.setText(info.fullName)
            setBirthDate(info.birthDate.ifBlank { null })

            info.phones.forEach(::addPhoneRow)
            info.emails.forEach(::addEmailRow)

            val a = info.address
            streetField.setText(a.street)
            street2Field.setText(a.street2)
            cityField.setText(a.city)
            regionField.setText(a.region)
            postalCodeField.setText(a.postalCode)
            countryField.setText(a.country)

            saveButton.isEnabled = true
            clearButton.isEnabled = true
        }
    }

    // -- Birth date ---------------------------------------------------

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        birthDateIso?.let { iso ->
            val (y, m, d) = iso.split("-").map(String::toInt)
            cal.set(y, m - 1, d)
        }
        DatePickerDialog(
            this,
            { _, year, month, day -> setBirthDate("%04d-%02d-%02d".format(year, month + 1, day)) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun setBirthDate(iso: String?) {
        birthDateIso = iso
        if (iso == null) {
            birthDateValue.setText(R.string.autofill_details_field_birth_date_placeholder)
            birthDateValue.alpha = 0.6f
            birthDateClear.visibility = View.GONE
            return
        }
        val (y, m, d) = iso.split("-").map(String::toInt)
        val cal = Calendar.getInstance().apply { set(y, m - 1, d) }
        birthDateValue.text = DateFormat.getMediumDateFormat(this).format(cal.time)
        birthDateValue.alpha = 1f
        birthDateClear.visibility = View.VISIBLE
    }

    // -- Phone / email rows --------------------------------------------

    private fun addPhoneRow(initial: PhoneEntry) {
        val row = LayoutInflater.from(this).inflate(R.layout.autofill_detail_phone_item, phonesContainer, false)
        row.findViewById<TextInputEditText>(R.id.phone_item_label).setText(initial.label)
        row.findViewById<TextInputEditText>(R.id.phone_item_code).setText(initial.countryCode)
        row.findViewById<TextInputEditText>(R.id.phone_item_number).setText(initial.number)

        val toggle = row.findViewById<ImageButton>(R.id.phone_item_default_toggle)
        row.setTag(R.id.phone_item_default_toggle, initial.isDefault)
        updateDefaultIcon(toggle, initial.isDefault)
        toggle.setOnClickListener { setDefaultPhoneRow(row) }

        row.findViewById<ImageButton>(R.id.phone_item_remove).setOnClickListener {
            phonesContainer.removeView(row)
            updatePhoneAddState()
        }

        phonesContainer.addView(row)
        updatePhoneAddState()
    }

    private fun addEmailRow(initial: EmailEntry) {
        val row = LayoutInflater.from(this).inflate(R.layout.autofill_detail_email_item, emailsContainer, false)
        row.findViewById<TextInputEditText>(R.id.email_item_label).setText(initial.label)
        row.findViewById<TextInputEditText>(R.id.email_item_address).setText(initial.address)

        val toggle = row.findViewById<ImageButton>(R.id.email_item_default_toggle)
        row.setTag(R.id.email_item_default_toggle, initial.isDefault)
        updateDefaultIcon(toggle, initial.isDefault)
        toggle.setOnClickListener { setDefaultEmailRow(row) }

        row.findViewById<ImageButton>(R.id.email_item_remove).setOnClickListener {
            emailsContainer.removeView(row)
            updateEmailAddState()
        }

        emailsContainer.addView(row)
        updateEmailAddState()
    }

    /** Only one phone can be "default" at a time — tapping a star clears the others. */
    private fun setDefaultPhoneRow(selected: View) {
        for (row in phonesContainer.children) {
            val isSelected = row === selected
            row.setTag(R.id.phone_item_default_toggle, isSelected)
            updateDefaultIcon(row.findViewById(R.id.phone_item_default_toggle), isSelected)
        }
    }

    private fun setDefaultEmailRow(selected: View) {
        for (row in emailsContainer.children) {
            val isSelected = row === selected
            row.setTag(R.id.email_item_default_toggle, isSelected)
            updateDefaultIcon(row.findViewById(R.id.email_item_default_toggle), isSelected)
        }
    }

    private fun updateDefaultIcon(toggle: ImageButton, isDefault: Boolean) {
        toggle.setImageResource(if (isDefault) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
        toggle.imageTintList = ContextCompat.getColorStateList(
            this,
            if (isDefault) R.color.md_primary else R.color.md_on_surface_variant
        )
    }

    private fun updatePhoneAddState() {
        val atLimit = phonesContainer.childCount >= AutofillInformation.MAX_PHONES
        addPhoneButton.isEnabled = !atLimit
        phonesLimit.visibility = if (atLimit) View.VISIBLE else View.GONE
        phonesLimit.text = getString(R.string.autofill_details_limit_phones, AutofillInformation.MAX_PHONES)
    }

    private fun updateEmailAddState() {
        val atLimit = emailsContainer.childCount >= AutofillInformation.MAX_EMAILS
        addEmailButton.isEnabled = !atLimit
        emailsLimit.visibility = if (atLimit) View.VISIBLE else View.GONE
        emailsLimit.text = getString(R.string.autofill_details_limit_emails, AutofillInformation.MAX_EMAILS)
    }

    // -- Save / clear ---------------------------------------------------

    private fun collect(): AutofillInformation {
        val phones = phonesContainer.children.map { row ->
            PhoneEntry(
                label = row.findViewById<TextInputEditText>(R.id.phone_item_label).text?.toString().orEmpty(),
                countryCode = row.findViewById<TextInputEditText>(R.id.phone_item_code).text?.toString().orEmpty(),
                number = row.findViewById<TextInputEditText>(R.id.phone_item_number).text?.toString().orEmpty(),
                isDefault = row.getTag(R.id.phone_item_default_toggle) as? Boolean ?: false
            )
        }.toList()

        val emails = emailsContainer.children.map { row ->
            EmailEntry(
                label = row.findViewById<TextInputEditText>(R.id.email_item_label).text?.toString().orEmpty(),
                address = row.findViewById<TextInputEditText>(R.id.email_item_address).text?.toString().orEmpty(),
                isDefault = row.getTag(R.id.email_item_default_toggle) as? Boolean ?: false
            )
        }.toList()

        return AutofillInformation(
            firstName = firstNameField.text?.toString().orEmpty(),
            lastName = lastNameField.text?.toString().orEmpty(),
            fullName = fullNameField.text?.toString().orEmpty(),
            birthDate = birthDateIso.orEmpty(),
            emails = emails,
            phones = phones,
            address = AddressInfo(
                street = streetField.text?.toString().orEmpty(),
                street2 = street2Field.text?.toString().orEmpty(),
                city = cityField.text?.toString().orEmpty(),
                region = regionField.text?.toString().orEmpty(),
                postalCode = postalCodeField.text?.toString().orEmpty(),
                country = countryField.text?.toString().orEmpty()
            )
        )
    }

    /**
     * Saves, then leaves the screen — the write completes (shielded
     * with [NonCancellable]) before `finish()`, so a quick tap back
     * never loses it.
     */
    private fun save() {
        saveButton.isEnabled = false
        clearButton.isEnabled = false
        val requested = collect()
        lifecycleScope.launch {
            withContext(NonCancellable) {
                AutofillInformationStore.set(applicationContext, requested)
            }
            Toast.makeText(applicationContext, R.string.autofill_details_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun confirmClear() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.autofill_details_clear_confirm_title)
            .setMessage(R.string.autofill_details_clear_confirm_body)
            .setPositiveButton(R.string.autofill_details_clear_all) { _, _ -> clear() }
            .setNegativeButton(R.string.autofill_action_cancel, null)
            .show()
    }

    private fun clear() {
        saveButton.isEnabled = false
        clearButton.isEnabled = false
        lifecycleScope.launch {
            withContext(NonCancellable) {
                AutofillInformationStore.clear(applicationContext)
            }
            Toast.makeText(applicationContext, R.string.autofill_details_cleared, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
