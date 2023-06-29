package io.semillita.hugame.editor.game;

import dev.hugame.assimp.AssimpModelLoader;
import dev.hugame.environment.DirectionalLight;
import dev.hugame.environment.Environment;
import dev.hugame.environment.PointLight;
import dev.hugame.environment.SpotLight;
import dev.hugame.graphics.Batch;
import dev.hugame.graphics.Camera2D;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.model.Model;
import dev.hugame.graphics.model.ModelBuilder;
import dev.hugame.io.FileHandle;
import dev.hugame.io.FileLocation;
import dev.hugame.util.Dimensions;
import dev.hugame.util.TextureLoader;
import dev.hugame.util.Transform;
import dev.hugame.vulkan.core.VulkanGraphics;
import java.awt.Canvas;
import java.awt.Dimension;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ShowcaseApplication {
  private final VulkanGraphics graphics;
  private final Batch batch;
  private final Camera2D camera2D;

  private Model model;
  private Model plane;

  private Transform playerTransform;
  private Transform planeTransform;

  private Texture groundTexture;

  private long startNano;

  public ShowcaseApplication(Canvas canvas) {
    this.graphics = new VulkanGraphics(new AwtSurfaceContext(canvas));

    this.batch = graphics.createBatch();
    this.camera2D =
        new Camera2D(
            () -> new Dimensions(canvas.getWidth(), canvas.getHeight()),
            new Vector2f(0, 0),
            new Dimension(1920, 1080));
    camera2D.update();
    batch.setCamera(camera2D);

    var modelLoader = new AssimpModelLoader(new TextureLoader(graphics));
    var modelFile = new FileHandle("deccer_cubes_tex.fbx", FileLocation.INTERNAL);
    var resolvedModel = modelLoader.load(modelFile).orElseThrow();
    this.model = graphics.createModel(resolvedModel);

    var textureLoader = new TextureLoader(graphics);
    this.groundTexture = textureLoader.get("/landscape.png");
    var resolvedPlaneModel = new ModelBuilder().plane(groundTexture).generate();
    this.plane = graphics.createModel(resolvedPlaneModel);

    playerTransform =
        new Transform(
            new Vector3f(0, 2, 0), new Vector3f(0, 0, 0), new Vector3f(0.001f, 0.001f, 0.001f));

    planeTransform =
        new Transform(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(10, 10, 10));

    var renderer = graphics.getRenderer();
    var environment = new Environment();
    var pointLight1 = new PointLight(new Vector3f(2, 0.1f, -2), new Vector3f(1, 0, 0), 0.5f);
    var spotLight1 =
        new SpotLight(
            new Vector3f(0, 1, 0), new Vector3f(-1f, -1, -1), new Vector3f(1, 1, 0), 2, 0.5f);
    var directionalLight1 =
        new DirectionalLight(new Vector3f(-1f, -1f, -1f), new Vector3f(1, 1, 1), 0.5f);
    environment.add(pointLight1);
    environment.add(spotLight1);
    environment.add(directionalLight1);
    renderer.updateEnvironment(environment);

    var camera = renderer.getCamera();
    camera.setPosition(new Vector3f(0, 5, 5));
    camera.lookAt(new Vector3f(0, 0, 0));
    camera.update();

    graphics.create();
    renderer.create();
    graphics.setClearColor(0f, 0f, 1f, 1f);

    startNano = System.nanoTime();
  }

  public void render() {
    graphics.getRenderer().beginFrame();
    // graphics.clear(1f, 1f, 0.5f, 0.0f);
    // graphics.clear(0f, 0f, 1, 1);
    drawObjects();
    graphics.getRenderer().endFrame();
    graphics.swapBuffers();
  }

  private void drawObjects() {
    var camera = graphics.getRenderer().getCamera();
    camera.update();

    var seconds = (System.nanoTime() - startNano) / 1_000_000_000d;
    var radius = 150;
    var x1 = (int) (Math.cos(-seconds) * radius);
    var y1 = (int) (Math.sin(-seconds) * radius);

    var renderer = graphics.getRenderer();
    renderer.draw(model, playerTransform);
    renderer.draw(plane, planeTransform);

    batch.begin();
    batch.draw(groundTexture, -480 + x1, -320 + y1, 100, 100);
    batch.draw(groundTexture, 300 - x1, 200 - y1, 100, 100);
    batch.end();

    renderer.flush();
  }
}
