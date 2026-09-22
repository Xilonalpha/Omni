package com.chemscanner.omniscient.ui.ar

import android.content.Context
import android.opengl.GLES30
import android.view.Surface
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.Skybox
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import java.nio.ByteBuffer

class DemonstrationARRenderer(context: Context, private val surfaceView: android.opengl.GLSurfaceView) {

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

    private val assetLoader: AssetLoader
    private val resourceLoader: ResourceLoader

    private val loadedAssets = mutableMapOf<String, com.google.android.filament.gltfio.FilamentAsset>()

    init {
        view.camera = camera
        view.scene = scene
        renderer.clearOptions = renderer.clearOptions.apply { clear = false }

        assetLoader = AssetLoader(engine, UbershaderProvider(engine), EntityManager.get())
        resourceLoader = ResourceLoader(engine)

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

        if (renderer.beginFrame(swapChain!!, frame.timestamp)) {
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

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

    fun addModel(anchor: Anchor, modelName: String, modelBuffer: ByteBuffer) {
        val asset = assetLoader.createAsset(modelBuffer)
        if (asset != null) {
            resourceLoader.loadResources(asset)
            asset.releaseSourceData()
            scene.addEntities(asset.entities)
            loadedAssets[modelName] = asset

            val transformManager = engine.transformManager
            val instance = transformManager.getInstance(asset.root)
            val matrix = FloatArray(16)
            anchor.pose.toMatrix(matrix, 0)
            transformManager.setTransform(instance, matrix)
        }
    }

    fun showModel(modelName: String) {
        loadedAssets[modelName]?.let { scene.addEntities(it.entities) }
    }

    fun hideModel(modelName: String) {
        loadedAssets[modelName]?.let { asset ->
            asset.entities.forEach { scene.removeEntity(it) }
        }
    }
}
