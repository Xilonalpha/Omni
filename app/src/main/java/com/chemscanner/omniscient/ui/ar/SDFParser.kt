package com.chemscanner.omniscient.ui.ar

data class Atom(val x: Float, val y: Float, val z: Float, val type: String)
data class Bond(val fromAtomIndex: Int, val toAtomIndex: Int, val type: Int)
data class ParsedSdf(val atoms: List<Atom>, val bonds: List<Bond>)

object SDFParser {

    fun parse(sdfData: String): ParsedSdf {
        val atoms = mutableListOf<Atom>()
        val bonds = mutableListOf<Bond>()
        val lines = sdfData.lines()

        if (lines.size < 4) return ParsedSdf(emptyList(), emptyList())

        // The 4th line usually contains the counts of atoms and bonds
        val countsLine = lines[3].trim().split("\\[ \t]+".toRegex())
        if (countsLine.size < 2) return ParsedSdf(emptyList(), emptyList())

        val atomCount = countsLine[0].toIntOrNull() ?: 0
        val bondCount = countsLine[1].toIntOrNull() ?: 0

        // Atom block starts from the 5th line (index 4)
        if (lines.size < 4 + atomCount) return ParsedSdf(emptyList(), emptyList())
        val atomLines = lines.subList(4, 4 + atomCount)
        for (line in atomLines) {
            val parts = line.trim().split("\\[ \t]+".toRegex())
            if (parts.size >= 4) {
                try {
                    atoms.add(Atom(parts[0].toFloat(), parts[1].toFloat(), parts[2].toFloat(), parts[3]))
                } catch (e: NumberFormatException) { continue }
            }
        }

        // Bond block starts after the atom block
        if (lines.size < 4 + atomCount + bondCount) return ParsedSdf(atoms, emptyList())
        val bondLines = lines.subList(4 + atomCount, 4 + atomCount + bondCount)
        for (line in bondLines) {
            val parts = line.trim().split("\\[ \t]+".toRegex())
            if (parts.size >= 3) {
                try {
                    // SDF indices are 1-based, so we subtract 1
                    val from = parts[0].toInt() - 1
                    val to = parts[1].toInt() - 1
                    val type = parts[2].toInt()
                    bonds.add(Bond(from, to, type))
                } catch (e: NumberFormatException) { continue }
            }
        }

        return ParsedSdf(atoms, bonds)
    }
}
