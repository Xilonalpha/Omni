package com.chemscanner.omniscient.marrow.data.remote.model

import com.google.gson.annotations.SerializedName

data class PubChemResponse(
    @SerializedName("PropertyTable") val propertyTable: PropertyTable
)

data class PropertyTable(
    @SerializedName("Properties") val properties: List<Properties>
)

data class Properties(
    @SerializedName("CID") val cid: Int? = null,
    @SerializedName("MolecularFormula") val molecularFormula: String? = null,
    @SerializedName("MolecularWeight") val molecularWeight: String? = null,
    @SerializedName("Title") val title: String? = null,
    @SerializedName("IUPACName") val iupacName: String? = null,
    @SerializedName("CanonicalSMILES") val smiles: String? = null,
    @SerializedName("Charge") val charge: Int? = null
)
