package com.chemscanner.omniscient.ui.ar

import android.content.Context
import android.opengl.GLES30
import android.view.Surface
import com.google.android.filament.*
import com.google.android.filament.android.DisplayHelper
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ExperimentARRenderer(context: Context, private val surfaceView: android.opengl.GLSurfaceView) {

    companion object {
        init {
            System.loadLibrary("filament-jni")
            System.loadLibrary("gltfio-jni")
        }
    }

    val engine: Engine = Engine.create()
    private val renderer: Renderer = engine.createRenderer()
    private val scene: Scene = engine.createScene()
    private val view: View = engine.createView()
    private val camera: Camera = engine.createCamera(EntityManager.get().create())

    private var swapChain: SwapChain? = null
    private val displayHelper: DisplayHelper = DisplayHelper(context)

    private val material: Material
    private val materialInstance: MaterialInstance
    private val vertexBuffer: VertexBuffer
    private val indexBuffer: IndexBuffer

    private val placedAnchors = mutableMapOf<Anchor, Int>()
    private val fallbackEntities = mutableListOf<Int>()

    init {
        view.camera = camera
        view.scene = scene
        // Initial state: clear background if AR is not yet active
        renderer.clearOptions = renderer.clearOptions.apply { 
            clear = true
        }

        val materialBuffer = readUncompressedAsset(context, "materials/lit.filamat")
        material = Material.Builder().payload(materialBuffer, materialBuffer.remaining()).build(engine)
        materialInstance = material.createInstance().apply {
            setParameter("baseColor", Colors.RgbaType.SRGB, 0.8f, 0.8f, 0.0f, 1.0f)
            setParameter("metallic", 0.0f)
            setParameter("roughness", 0.4f)
        }

        val (verts, indices) = generateSphere(0.1f, 16, 16)
        vertexBuffer = VertexBuffer.Builder()
            .bufferCount(1)
            .vertexCount(verts.capacity() / 3)
            .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3, 0, 12)
            .build(engine)
        vertexBuffer.setBufferAt(engine, 0, verts)

        indexBuffer = IndexBuffer.Builder()
            .indexCount(indices.capacity())
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(engine)
        indexBuffer.setBuffer(engine, indices)

        setupLighting()
    }

    private fun setupLighting() {
        scene.skybox = Skybox.Builder().color(0.1f, 0.15f, 0.25f, 1.0f).build(engine)
        val light = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.SUN)
            .color(1.0f, 1.0f, 0.95f)
            .intensity(100_000.0f)
            .direction(0.7f, -1.0f, -0.8f)
            .sunAngularRadius(1.9f)
            .castShadows(true)
            .build(engine, light)
        scene.addEntity(light)
    }

    fun onSurfaceCreated(surface: Surface) {
        swapChain = engine.createSwapChain(surface)
        displayHelper.attach(renderer, surfaceView.display)
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        view.viewport = Viewport(0, 0, width, height)
    }

    fun render(frame: Frame) {
        if (swapChain == null) return

        // In AR mode, we don't want to clear because ARCore renders the camera feed
        renderer.clearOptions = renderer.clearOptions.apply { clear = false }

        if (renderer.beginFrame(swapChain!!, frame.timestamp)) {
            val projectionMatrix = FloatArray(16)
            frame.camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)
            camera.setCustomProjection(projectionMatrix.map { it.toDouble() }.toDoubleArray(), 0.1, 100.0)

            val viewMatrix = FloatArray(16)
            frame.camera.getViewMatrix(viewMatrix, 0)
            camera.setModelMatrix(viewMatrix)

            renderer.render(view)
            renderer.endFrame()
        }
    }

    fun renderFallback(timestamp: Long) {
        if (swapChain == null) return

        // In 3D fallback mode, we MUST clear to avoid black screen/artifacts
        renderer.clearOptions = renderer.clearOptions.apply { clear = true }

        if (renderer.beginFrame(swapChain!!, timestamp)) {
            val aspect = view.viewport.width.toFloat() / view.viewport.height.toFloat()
            camera.setProjection(45.0, aspect.toDouble(), 0.1, 100.0, Camera.Fov.VERTICAL)
            
            // Look at center from slightly above and back
            camera.lookAt(0.0, 0.5, 2.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0)

            renderer.render(view)
            renderer.endFrame()
        }
    }

    fun addLabel(anchor: Anchor) {
        val entity = createEntity()
        scene.addEntity(entity)

        val transformManager = engine.transformManager
        val instance = transformManager.create(entity)
        val matrix = FloatArray(16)
        anchor.pose.toMatrix(matrix, 0)
        transformManager.setTransform(instance, matrix)

        placedAnchors[anchor] = instance
    }

    fun addLabelAtDefault() {
        val entity = createEntity()
        scene.addEntity(entity)

        val transformManager = engine.transformManager
        val instance = transformManager.create(entity)
        
        // Place it at origin for 3D fallback
        val matrix = FloatArray(16)
        android.opengl.Matrix.setIdentityM(matrix, 0)
        // Offset slightly if multiple items are added, or just keep at center for demo
        android.opengl.Matrix.translateM(matrix, 0, (fallbackEntities.size * 0.2f), 0f, 0f)
        
        transformManager.setTransform(instance, matrix)
        fallbackEntities.add(entity)
    }

    private fun createEntity(): Int {
        val entity = EntityManager.get().create()
        RenderableManager.Builder(1)
            .boundingBox(Box(-0.1f, -0.1f, -0.1f, 0.1f, 0.1f, 0.1f))
            .material(0, materialInstance)
            .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vertexBuffer, indexBuffer)
            .culling(true)
            .build(engine, entity)
        return entity
    }

    private fun readUncompressedAsset(context: Context, assetName: String): ByteBuffer {
        context.assets.open(assetName).use { input ->
            val bytes = input.readBytes()
            return ByteBuffer.allocateDirect(bytes.size).put(bytes).apply { rewind() }
        }
    }
    
    private fun generateSphere(radius: Float, stacks: Int, sectors: Int): Pair<java.nio.FloatBuffer, java.nio.ShortBuffer> { val vertices = mutableListOf<Float>(); val indices = mutableListOf<Short>(); val pi = PI.toFloat(); for (i in 0..stacks) { val stackAngle = pi / 2 - i * (pi / stacks); val xy = radius * cos(stackAngle); val z = radius * sin(stackAngle); for (j in 0..sectors) { val sectorAngle = j * (2 * pi / sectors); vertices.addAll(listOf(xy * cos(sectorAngle), xy * sin(sectorAngle), z)); } }; for (i in 0 until stacks) { for (j in 0 until sectors) { val first = (i * (sectors + 1)) + j; val second = first + sectors + 1; indices.addAll(listOf(first.toShort(), second.toShort(), (first + 1).toShort())); indices.addAll(listOf(second.toShort(), (second + 1).toShort(), (first + 1).toShort())); } }; return Pair(ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices.toFloatArray()).apply { rewind() }, ByteBuffer.allocateDirect(indices.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer().put(indices.toShortArray()).apply { rewind() }); }
}
