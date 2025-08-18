package com.thellex.payments.core.adapters

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thellex.payments.R
import com.thellex.payments.data.model.Country
import com.thellex.payments.data.model.getNonSanctionedCountryList
import com.thellex.payments.databinding.ItemCountryBinding

class CountryPickerDialogFragment : DialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CountryAdapter
    private var listener: ((Country) -> Unit)? = null
    private var filteredCountries = getNonSanctionedCountryList().toMutableList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_country_picker, container, false)
        recyclerView = view.findViewById(R.id.country_recycler_view)
        setupRecyclerView()
        setupSearch(view)
        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_border_midnight)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                // Add spacing only between items, not after the last one
                if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
                    outRect.bottom = 18
                }
            }
        })
        adapter = CountryAdapter(filteredCountries) { country ->
            listener?.invoke(country)
            dismiss()
        }
        recyclerView.adapter = adapter
    }

    private fun setupSearch(view: View) {
        val searchEditText = view.findViewById<EditText>(R.id.country_search)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                filteredCountries.clear()
                filteredCountries.addAll(
                    getNonSanctionedCountryList().filter {
                        it.name.lowercase().contains(query) || it.code.lowercase().contains(query)
                    }
                )
                adapter.notifyDataSetChanged()
            }
        })
    }

    fun setOnCountrySelectedListener(listener: (Country) -> Unit) {
        this.listener = listener
    }
}

class CountryAdapter(
    private val countries: List<Country>,
    private val onCountryClick: (Country) -> Unit
) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {

    inner class CountryViewHolder(private val binding: ItemCountryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(country: Country) {
            binding.countryName.text = "${country.name} (${country.code})"
            Glide.with(binding.countryFlag.context)
                .load(country.flagUrl)
                .into(binding.countryFlag)
            binding.root.setOnClickListener { onCountryClick(country) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val binding = ItemCountryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(countries[position])
    }

    override fun getItemCount(): Int = countries.size
}