package com.chemscanner.omniscient.ui.ar

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object Sphere {

    data class MeshData(val vertices: FloatBuffer, val indices: ShortBuffer)

    fun generate(radius: Float, rings: Int, sectors: Int): MeshData {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()
        val pi = PI.toFloat()

        for (r in 0..rings) {
            val phi = pi / 2 - r * (pi / rings)
            val y = radius * sin(phi)
            val ringRadius = radius * cos(phi)

            for (s in 0..sectors) {
                val theta = s * (2 * pi / sectors)
                vertices.add(ringRadius * cos(theta))
                vertices.add(y)
                vertices.add(ringRadius * sin(theta))
            }
        }

        for (r in 0 until rings) {
            for (s in 0 until sectors) {
                val first = (r * (sectors + 1) + s).toShort()
                val second = (first + sectors + 1).toShort()

                indices.add(first)
                indices.add(second)
                indices.add((first + 1).toShort())

                indices.add(second)
                indices.add((second + 1).toShort())
                indices.add((first + 1).toShort())
            }
        }

        val vBuf = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices.toFloatArray()).apply { rewind() }
        val iBuf = ByteBuffer.allocateDirect(indices.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer().put(indices.toShortArray()).apply { rewind() }
        
        return MeshData(vBuf, iBuf)
    }
}
