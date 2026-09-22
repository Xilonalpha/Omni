package com.chemscanner.omniscient.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chemscanner.omniscient.R
import com.chemscanner.omniscient.marrow.data.models.GeneratedMolecule // FIXED IMPORT

class AlternativesAdapter(private val alternatives: List<GeneratedMolecule>) : RecyclerView.Adapter<AlternativesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alternative, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alternative = alternatives[position]
        holder.alternativeName.text = alternative.smiles // 'smiles' field now holds the name
        
        if (!alternative.justification.isNullOrBlank()) {
            holder.alternativeJustification.text = alternative.justification
            holder.alternativeJustification.visibility = View.VISIBLE
        } else {
            holder.alternativeJustification.visibility = View.GONE
        }
    }

    override fun getItemCount() = alternatives.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alternativeName: TextView = itemView.findViewById(R.id.alternative_name)
        val alternativeJustification: TextView = itemView.findViewById(R.id.alternative_justification)
    }
}
