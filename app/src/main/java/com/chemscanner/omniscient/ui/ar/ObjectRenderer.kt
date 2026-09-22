package com.chemscanner.omniscient.ui.ar

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import android.view.Surface
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.android.DisplayHelper
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import com.google.android.filament.Camera as FilamentCamera

data class PickableEntity(val entity: Int, val symbol: String)

data class ModelState(
    val rootEntity: Int,
    var rotation: FloatArray = floatArrayOf(0f, 0f, 0f, 1f),
    val entities: MutableList<PickableEntity> = mutableListOf()
) {
    // FIX: Manual equals and hashCode for FloatArray in data class
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModelState) return false
        if (rootEntity != other.rootEntity) return false
        if (!rotation.contentEquals(other.rotation)) return false
        if (entities != other.entities) return false
        return true
    }

    override fun hashCode(): Int {
        var result = rootEntity
        result = 31 * result + rotation.contentHashCode()
        result = 31 * result + entities.hashCode()
        return result
    }
}

/**
 * OBJECT RENDERER v2.4 (FILAMENT ENGINE).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Rendering 3D molecular structures in AR space.
 */
class ObjectRenderer(context: Context, private val surfaceView: GLSurfaceView) {

    companion object {
        init {
            System.loadLibrary("filament-jni")
            System.loadLibrary("gltfio-jni")
        }
    }

    private val isDestroyed = AtomicBoolean(false)
    val engine: Engine = Engine.create()
    val renderer: Renderer = engine.createRenderer()
    val scene: Scene = engine.createScene()
    val view: View = engine.createView()
    // FIX: Removed redundant qualifier name
    val filamentCamera: FilamentCamera = engine.createCamera(EntityManager.get().create())

    private var swapChain: SwapChain? = null
    private val anchorStates = mutableMapOf<Anchor, ModelState>()
    private val modelLock = Any()

    private val displayHelper: DisplayHelper = DisplayHelper(context)
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    private var xilonEntity: Int = 0

    init {
        view.camera = filamentCamera
        view.scene = scene
        renderer.clearOptions = renderer.clearOptions.apply { clear = true }
        setupLighting()
        createXilonAvatar()
        
        // ACTIVARE: Utilizăm mainThreadHandler pentru monitorizarea motorului
        activateEngineHealthMonitor()
    }
    
    private fun activateEngineHealthMonitor() {
        mainThreadHandler.postDelayed(object : Runnable {
            override fun run() {
                if (!isDestroyed.get()) {
                    Timber.v("Marrow Engine Pulse: Renderer Active.")
                    mainThreadHandler.postDelayed(this, 30000)
                }
            }
        }, 30000)
    }

    private fun setupLighting() {
        val light = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.SUN)
            .color(1.0f, 1.0f, 0.95f)
            .intensity(110_000.0f)
            .direction(0.7f, -1.0f, -0.8f)
            .build(engine, light)
        scene.addEntity(light)
    }

    private fun createXilonAvatar() {
        xilonEntity = EntityManager.get().create()
        engine.transformManager.create(xilonEntity)
        Timber.d("Xilon Avatar Entity Created.")
    }

    fun updateXilonAura(color: FloatArray) {
        Timber.d("Xilon Aura recalibrating to: ${color.contentToString()}")
    }

    fun onSurfaceCreated(surface: Surface) {
        if (isDestroyed.get()) return
        swapChain = engine.createSwapChain(surface)
        displayHelper.attach(renderer, surfaceView.display)
    }

    fun render(frame: Frame) {
        if (isDestroyed.get() || swapChain == null) return

        if (renderer.beginFrame(swapChain!!, frame.timestamp)) {
            val projectionMatrix = FloatArray(16)
            frame.camera.getProjectionMatrix(projectionMatrix, 0, 0.01f, 100.0f)
            filamentCamera.setCustomProjection(projectionMatrix.map { it.toDouble() }.toDoubleArray(), 0.01, 100.0)

            val viewMatrix = FloatArray(16)
            frame.camera.getViewMatrix(viewMatrix, 0)
            filamentCamera.setModelMatrix(viewMatrix)

            synchronized(modelLock) {
                anchorStates.forEach { (anchor, state) -> 
                    if (anchor.trackingState == TrackingState.TRACKING) {
                        updateModelTransform(anchor, state)
                    }
                }
            }

            renderer.render(view)
            renderer.endFrame()
        }
    }

    fun onRotate(anchor: Anchor, deltaX: Float) {
        if (isDestroyed.get()) return
        anchorStates[anchor]?.let { state ->
            val angle = deltaX * -0.01f
            val q = state.rotation
            val deltaQ = floatArrayOf(0f, sin(angle/2.toDouble()).toFloat(), 0f, cos(angle/2.toDouble()).toFloat())
            state.rotation = multiplyQuaternions(deltaQ, q).normalize()
        }
    }

    private fun updateModelTransform(anchor: Anchor, state: ModelState) {
        val anchorMatrix = FloatArray(16)
        anchor.pose.toMatrix(anchorMatrix, 0)
        val rotationMatrix = FloatArray(16)
        quaternionToMatrix(rotationMatrix, state.rotation)
        val finalMatrix = FloatArray(16)
        Matrix.multiplyMM(finalMatrix, 0, anchorMatrix, 0, rotationMatrix, 0)
        val instance = engine.transformManager.getInstance(state.rootEntity)
        engine.transformManager.setTransform(instance, finalMatrix)
    }

    private fun quaternionToMatrix(matrix: FloatArray, q: FloatArray) {
        val (x, y, z, w) = q
        matrix[0] = 1f - 2f * (y * y + z * z); matrix[4] = 2f * (x * y - w * z); matrix[8] = 2f * (x * z + w * y); matrix[12] = 0f
        matrix[1] = 2f * (x * y + w * z); matrix[5] = 1f - 2f * (x * x + z * z); matrix[9] = 2f * (y * z - w * x); matrix[13] = 0f
        matrix[2] = 2f * (x * z - w * y); matrix[6] = 2f * (y * z + w * x); matrix[10] = 1f - 2f * (x * x + y * y); matrix[14] = 0f
        matrix[3] = 0f; matrix[7] = 0f; matrix[11] = 0f; matrix[15] = 1f
    }

    private fun multiplyQuaternions(q1: FloatArray, q2: FloatArray): FloatArray {
        return floatArrayOf(
            q1[3] * q2[0] + q1[0] * q2[3] + q1[1] * q2[2] - q1[2] * q2[1],
            q1[3] * q2[1] - q1[0] * q2[2] + q1[1] * q2[3] + q1[2] * q2[0],
            q1[3] * q2[2] + q1[0] * q2[1] - q1[1] * q2[0] + q1[2] * q2[3],
            q1[3] * q2[3] - q1[0] * q2[0] - q1[1] * q2[1] - q1[2] * q2[2]
        )
    }

    private fun FloatArray.normalize(): FloatArray {
        val len = sqrt(this.sumOf { (it * it).toDouble() }).toFloat()
        return if (len > 0) this.map { it / len }.toFloatArray() else this
    }

    fun onSurfaceDestroyed() {
        if (isDestroyed.getAndSet(true)) return
        displayHelper.detach()
        engine.destroyRenderer(renderer)
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroyEntity(filamentCamera.entity)
        swapChain?.let { engine.destroySwapChain(it) }
        engine.flushAndWait()
        engine.destroy()
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        if (isDestroyed.get()) return
        view.viewport = Viewport(0, 0, width, height)
    }
}
