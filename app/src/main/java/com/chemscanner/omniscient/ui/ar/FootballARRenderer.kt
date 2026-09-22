package com.chemscanner.omniscient.ui.ar

import android.content.Context
import android.opengl.GLES30
import android.opengl.Matrix
import android.view.Surface
import com.chemscanner.omniscient.marrow.data.models.FootballUiState
import com.chemscanner.omniscient.marrow.data.models.FootballPlayerInstance
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * SOVEREIGN FOOTBALL AR RENDERER v5.2 (FIXED ARCORE API SYNC).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Zero-asset holographic rendering with fixed API calls.
 */
@Singleton
class FootballARRenderer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var programId: Int = -1
    
    // Vertex Buffers
    private var gridVertexBuffer: FloatBuffer? = null
    private var pillarVertexBuffer: FloatBuffer? = null
    private var sphereVertexBuffer: FloatBuffer? = null
    
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var stadiumAnchor: Anchor? = null
    private var isFallbackMode = false

    private val vertexShaderCode = """
        #version 300 es
        layout(location = 0) in vec3 aPosition;
        layout(location = 1) in vec3 aNormal;
        uniform mat4 uMVPMatrix;
        uniform vec3 uColor;
        out vec3 vColor;
        out float vIntensity;
        void main() {
            gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
            vColor = uColor;
            vIntensity = dot(aNormal, vec3(0.0, 1.0, 0.0)) * 0.5 + 0.5;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        in vec3 vColor;
        in float vIntensity;
        out vec4 fragColor;
        void main() {
            fragColor = vec4(vColor * (vIntensity + 0.3), 0.7); 
        }
    """.trimIndent()

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES30.glCreateShader(type).also { shader ->
            GLES30.glShaderSource(shader, shaderCode)
            GLES30.glCompileShader(shader)
        }
    }

    private fun initGeometries() {
        val gridLines = mutableListOf<Float>()
        for (i in 0..10) {
            val x = (i * 100f - 500f)
            gridLines.addAll(listOf(x, 0f, -750f, x, 0f, 750f)) 
        }
        for (i in 0..15) {
            val z = (i * 100f - 750f)
            gridLines.addAll(listOf(-500f, 0f, z, 500f, 0f, z)) 
        }
        gridVertexBuffer = ByteBuffer.allocateDirect(gridLines.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply { put(gridLines.toFloatArray()); position(0) }
        }

        val pillarData = floatArrayOf(
            -1f, 1f, 1f,  0f, 1f, 0f,  1f, 1f, 1f,  0f, 1f, 0f,  1f, -1f, 1f,  0f, 1f, 0f, -1f, -1f, 1f, 0f, 1f, 0f,
            -1f, 1f, -1f, 0f, 1f, 0f, 1f, 1f, -1f, 0f, 1f, 0f, 1f, -1f, -1f, 0f, 1f, 0f, -1f, -1f, -1f, 0f, 1f, 0f
        )
        pillarVertexBuffer = ByteBuffer.allocateDirect(pillarData.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply { put(pillarData); position(0) }
        }

        val sphereData = floatArrayOf(
            0f, 1f, 0f, 0f, 1f, 0f,  1f, 0f, 0f, 1f, 0f, 0f,  0f, 0f, 1f, 0f, 0f, 1f,
            -1f, 0f, 0f, -1f, 0f, 0f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, -1f, 0f, 0f, -1f, 0f
        )
        sphereVertexBuffer = ByteBuffer.allocateDirect(sphereData.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply { put(sphereData); position(0) }
        }
    }

    private fun ensureInitialized() {
        if (programId != -1) return
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)
        programId = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
        }
        initGeometries()
    }

    fun render(frame: Frame?, uiState: FootballUiState) {
        ensureInitialized()
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        if (frame != null) {
            // FIXED: Correct ARCore API calls
            frame.camera.getViewMatrix(viewMatrix, 0)
            frame.camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)
        } else if (isFallbackMode) {
            Matrix.setLookAtM(viewMatrix, 0, 0f, 2f, 4f, 0f, 0f, 0f, 0f, 1f, 0f)
            Matrix.perspectiveM(projectionMatrix, 0, 45f, 1f, 0.1f, 100f)
        }

        val anchor = stadiumAnchor
        if (anchor != null || isFallbackMode) {
            val anchorMatrix = FloatArray(16)
            if (anchor != null) anchor.pose.toMatrix(anchorMatrix, 0) 
            else Matrix.setIdentityM(anchorMatrix, 0)

            renderQuantumGrid(anchorMatrix, uiState.fieldScale)
            renderEnergySphere(uiState.ball.position, uiState.ball.altitude, uiState.fieldScale, anchorMatrix)
            uiState.players.forEach { player ->
                val color = if (player.team == "USER") floatArrayOf(0f, 0.8f, 1f) else floatArrayOf(1f, 0.2f, 0.2f)
                renderEnergyPillar(player, uiState.fieldScale, anchorMatrix, color)
            }
        }
    }

    private fun renderQuantumGrid(baseMatrix: FloatArray, scale: Float) {
        val gridBuf = gridVertexBuffer ?: return
        GLES30.glUseProgram(programId)
        val mvpHandle = GLES30.glGetUniformLocation(programId, "uMVPMatrix")
        val colorHandle = GLES30.glGetUniformLocation(programId, "uColor")

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale)
        
        val tempM = FloatArray(16)
        Matrix.multiplyMM(tempM, 0, viewMatrix, 0, baseMatrix, 0)
        Matrix.multiplyMM(tempM, 0, tempM, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempM, 0)

        GLES30.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        GLES30.glUniform3f(colorHandle, 0f, 0.5f, 0.6f)

        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, gridBuf)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, gridBuf.limit() / 3)
    }

    private fun renderEnergySphere(pos: androidx.compose.ui.geometry.Offset, alt: Float, scale: Float, baseMatrix: FloatArray) {
        val sphereBuf = sphereVertexBuffer ?: return
        GLES30.glUseProgram(programId)
        val mvpHandle = GLES30.glGetUniformLocation(programId, "uMVPMatrix")
        val colorHandle = GLES30.glGetUniformLocation(programId, "uColor")

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, (pos.x - 500f) * scale, alt * scale + 0.05f, (pos.y - 750f) * scale)
        Matrix.scaleM(modelMatrix, 0, 0.04f, 0.04f, 0.04f)

        val tempM = FloatArray(16)
        Matrix.multiplyMM(tempM, 0, viewMatrix, 0, baseMatrix, 0)
        Matrix.multiplyMM(tempM, 0, tempM, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempM, 0)

        GLES30.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        GLES30.glUniform3f(colorHandle, 1f, 1f, 1f)

        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 6 * 4, sphereBuf)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, sphereBuf.limit() / 6)
    }

    private fun renderEnergyPillar(player: FootballPlayerInstance, scale: Float, baseMatrix: FloatArray, color: FloatArray) {
        val pillarBuf = pillarVertexBuffer ?: return
        GLES30.glUseProgram(programId)
        val mvpHandle = GLES30.glGetUniformLocation(programId, "uMVPMatrix")
        val colorHandle = GLES30.glGetUniformLocation(programId, "uColor")

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, (player.position.x - 500f) * scale, 0.12f, (player.position.y - 750f) * scale)
        Matrix.scaleM(modelMatrix, 0, 0.03f, 0.12f, 0.03f)

        val tempM = FloatArray(16)
        Matrix.multiplyMM(tempM, 0, viewMatrix, 0, baseMatrix, 0)
        Matrix.multiplyMM(tempM, 0, tempM, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempM, 0)

        GLES30.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        
        val dynamicColor = if (player.isSelected) color.map { it * 1.3f }.toFloatArray() else color
        GLES30.glUniform3fv(colorHandle, 1, dynamicColor, 0)

        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 6 * 4, pillarBuf)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, pillarBuf.limit() / 6)
    }

    fun onSurfaceCreated(s: Surface, d: android.view.Display) { ensureInitialized() }
    fun onSurfaceChanged(w: Int, h: Int) { GLES30.glViewport(0, 0, w, h) }
    fun getTextureId(): Int = 1001 
    fun setStadiumAnchor(a: Anchor?) { this.stadiumAnchor = a }
    fun enableFallbackMode() { this.isFallbackMode = true }
    fun releaseSurface() { programId = -1 }
}
