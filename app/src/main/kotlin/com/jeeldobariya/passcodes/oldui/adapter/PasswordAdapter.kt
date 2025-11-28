package com.jeeldobariya.passcodes.oldui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.jeeldobariya.passcodes.databinding.PasswordListItemBinding
import com.jeeldobariya.passcodes.domain.modals.PasswordModal

class PasswordAdapter(
    private val context: Context,
    private var passwordList: List<PasswordModal> // Use mutable list if you need to update it
) : BaseAdapter() {

    // You might want to provide a way to update the list and notify the adapter
    fun updateData(newList: List<PasswordModal>) {
        passwordList = newList
        notifyDataSetChanged() // Notify the ListView that the data has changed
    }

    override fun getCount(): Int {
        return passwordList.size
    }

    override fun getItem(position: Int): Any {
        return passwordList[position]
    }

    override fun getItemId(position: Int): Long {
        // It's often better to return the unique ID of the item if available,
        // for better performance with adapters, especially if you had stable IDs.
        // For a simple BaseAdapter, position is fine if IDs are not stable or not needed.
        return passwordList[position].id.toLong() // Assuming 'id' is your primary key
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: PasswordListItemBinding // Use val for late initialization via 'if' block

        val rowView: View // Declare a variable to hold the final view

        if (convertView == null) {
            binding = PasswordListItemBinding.inflate(LayoutInflater.from(context), parent, false)
            rowView = binding.root // Get the root view from the binding
            rowView.tag = binding // Store the binding object as a tag
        } else {
            // Re-use existing view, retrieve binding from tag
            binding = convertView.tag as PasswordListItemBinding
            rowView = convertView // The convertView is already the rowView
        }

        val password = passwordList[position]
        binding.tvDomain.text = password.domain
        binding.tvUsername.text = password.username

        return rowView
    }
}
