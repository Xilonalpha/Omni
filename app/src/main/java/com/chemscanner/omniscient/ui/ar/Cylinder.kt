package com.chemscanner.omniscient.ui.ar

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object Cylinder {

    data class MeshData(val vertices: FloatBuffer, val indices: ShortBuffer)

    fun generate(radius: Float, height: Float, sectors: Int): MeshData {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()
        val pi = PI.toFloat()
        val h2 = height / 2f

        // Vertices for the side (tubular part)
        for (i in 0..1) { // 0 for top ring, 1 for bottom ring
            val y = if (i == 0) h2 else -h2
            for (s in 0..sectors) {
                val theta = s * (2 * pi / sectors)
                vertices.add(radius * cos(theta))
                vertices.add(y)
                vertices.add(radius * sin(theta))
            }
        }

        // Indices for the side
        for (s in 0 until sectors) {
            val top = s.toShort()
            val bottom = (s + sectors + 1).toShort()
            
            indices.add(top)
            indices.add(bottom)
            indices.add((top + 1).toShort())

            indices.add(bottom)
            indices.add((bottom + 1).toShort())
            indices.add((top + 1).toShort())
        }

        // Caps (Top and Bottom)
        // Top center
        val topCenterIndex = (vertices.size / 3).toShort()
        vertices.add(0f); vertices.add(h2); vertices.add(0f)
        for (s in 0..sectors) {
            val theta = s * (2 * pi / sectors)
            vertices.add(radius * cos(theta)); vertices.add(h2); vertices.add(radius * sin(theta))
        }
        for (s in 0 until sectors) {
            indices.add(topCenterIndex)
            indices.add((topCenterIndex + s + 1).toShort())
            indices.add((topCenterIndex + s + 2).toShort())
        }

        // Bottom center
        val bottomCenterIndex = (vertices.size / 3).toShort()
        vertices.add(0f); vertices.add(-h2); vertices.add(0f)
        for (s in 0..sectors) {
            val theta = s * (2 * pi / sectors)
            vertices.add(radius * cos(theta)); vertices.add(-h2); vertices.add(radius * sin(theta))
        }
        for (s in 0 until sectors) {
            indices.add(bottomCenterIndex)
            indices.add((bottomCenterIndex + s + 2).toShort()) // CCW
            indices.add((bottomCenterIndex + s + 1).toShort())
        }

        val vBuf = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices.toFloatArray()).apply { rewind() }
        val iBuf = ByteBuffer.allocateDirect(indices.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer().put(indices.toShortArray()).apply { rewind() }
        
        return MeshData(vBuf, iBuf)
    }
}
