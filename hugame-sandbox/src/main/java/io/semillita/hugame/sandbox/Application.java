package io.semillita.hugame.sandbox;

import dev.hugame.freetype.FreeTypeFontLoader;
import dev.hugame.freetype.MsdfGenFontLoader;
import dev.hugame.graphics.Graphics;
import dev.hugame.desktop.gl.GLGraphics;
import dev.hugame.graphics.text.Font;
import dev.hugame.graphics.model.ModelBuilder;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.application.HuGameApplicationContext;
import dev.hugame.application.SimpleApplicationConfiguration;
import dev.hugame.application.SimpleApplicationListener;
import dev.hugame.application.SimpleHuGameApplication;
import dev.hugame.assimp.AssimpModelLoader;
import dev.hugame.core.Input;
import dev.hugame.graphics.Renderer;
import dev.hugame.core.Window;
import dev.hugame.environment.DirectionalLight;
import dev.hugame.environment.Environment;
import dev.hugame.environment.PointLight;
import dev.hugame.environment.SpotLight;
import dev.hugame.graphics.Batch;
import dev.hugame.graphics.Camera2D;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.material.Materials;
import dev.hugame.graphics.model.Model;
import dev.hugame.input.Key;
import dev.hugame.io.FileHandle;
import dev.hugame.io.FileLocation;
import dev.hugame.model.spec.ModelLoader;
import dev.hugame.ui.Slider;
import dev.hugame.util.TextureLoader;
import dev.hugame.util.Transform;
import dev.hugame.graphics.renderers.model.ModelPipelineShaderGenerator;
import dev.hugame.vulkan.surface.GlfwSurfaceContext;
import dev.hugame.window.DesktopInput;
import dev.hugame.window.DesktopWindow;
import dev.hugame.window.WindowConfiguration;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.Dimension;
import java.util.List;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.glViewport;

public class Application implements SimpleApplicationListener {
	public static void main(String[] args) {
        var modelShaderSourceTest = ModelPipelineShaderGenerator.createShader();
        System.out.println("Generated model vertex shader:");
        System.out.println(modelShaderSourceTest.vertexSource());
        System.out.println("Generated model fragment shader:");
        System.out.println(modelShaderSourceTest.fragmentSource());

		var windowConfig = new WindowConfiguration()
				.width(960)
				.height(540)
				.title("App")
				.x(500)
				.y(300)
				.decorated(true)
				.resizable(true)
				.transparentFramebuffer(true);

		Supplier<SimpleApplicationConfiguration> configurer = () -> {
			var useVulkan = false;

			DesktopWindow window;
			Graphics graphics;
			if (!useVulkan) {
				window = new DesktopWindow(windowConfig, (width, height) -> glViewport(0, 0, width, height), true);
				graphics = new GLGraphics(window);
			} else {
				window = new DesktopWindow(windowConfig, (width, height) -> System.out.println("Resizing viewport"), false);
				graphics = new VulkanGraphics(new GlfwSurfaceContext(window));
			}
			var input = new DesktopInput(window);

			var assimpModelLoader = new AssimpModelLoader(new TextureLoader(graphics));
			return new SimpleApplicationConfiguration(graphics, window, input, List.of(assimpModelLoader));
		};

		var application = new SimpleHuGameApplication(new Application(), configurer);
		application.start();
	}

	private Transform playerTransform;
	private Transform planeTransform;

	Material blueMat;

	float playerX = 0, playerY = 0, playerZ = 0;

	// TODO: Bake the batch into Renderer (abstract renderer implementation
	//       decides whether to batch or just make draw calls)
	private Batch batch;
	private Camera2D camera2D;

	private HugoButton button;
	private Slider slider;

	private Environment environment;

	private Model model;
	private Model plane;
    private Model car;

    private Vector3f initialLeftBeamPosition = new Vector3f(-125, 50, -20);
    private Vector3f initialLeftBeamDirection = new Vector3f(-1, -0.3f, 0);
    private SpotLight leftBeam = new SpotLight(
            initialLeftBeamPosition,
            initialLeftBeamDirection,
            new Vector3f(0.8f, 1, 0.8f),
            1f,
            0.5f);

    private Vector3f initialRightBeamPosition = new Vector3f(-125, 50, 20);
    private Vector3f initialRightBeamDirection = new Vector3f(-1, -0.3f, 0);
    private SpotLight rightBeam = new SpotLight(
            initialRightBeamPosition,
            initialRightBeamDirection,
            new Vector3f(0.8f, 1, 0.8f),
            1f,
            0.5f);

	private Window window;
	private Input input;
	private Renderer renderer;
	private ModelLoader modelLoader;

	private Texture groundTexture;
	private TextureLoader textureLoader;

    private Font qilkaFont;

	@Override
	public void onCreate(HuGameApplicationContext applicationContext) {
		this.window = applicationContext.getWindow();
		this.input = applicationContext.getInput();

		var graphics = applicationContext.getGraphics();
		this.renderer = graphics.getRenderer();

		this.modelLoader = applicationContext.getModelLoader();

		textureLoader = new TextureLoader(graphics);

		playerTransform = new Transform(new Vector3f(0, 2, 0), new Vector3f(0, 0, 0),
				new Vector3f(0.01f, 0.01f, 0.01f));

		planeTransform = new Transform(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0),
				new Vector3f(10, 10, 10));

		batch = graphics.createBatch();
		camera2D = new Camera2D(window, new Vector2f(0, 0), new Dimension(1920, 1080));
		camera2D.update();
		batch.setCamera(camera2D);

		button = new HugoButton(textureLoader.get("/button.png"), textureLoader.get("/hover.png"));
		button.setScreenToWorldCoordinateMapping(camera2D::screenToWorldCoords);

		slider = new Slider(textureLoader.get("/slider_background.png"), textureLoader.get("/slider_thumb.png"));
		slider.setScreenToWorldCoordinateMapping(camera2D::screenToWorldCoords);

		input.setMouseButtonListener((event) -> {
			switch (event.action()) {
				case PRESS -> {
					button.mouseDown();
					slider.mouseDown();
				}
				case RELEASE -> {
					button.mouseUp();
					slider.mouseUp();
				}
			}
		});

		window.addResizeListener((width, height) -> {
			camera2D.updateViewport();
			camera2D.update();
		});

		environment = new Environment();
		var pointLight1 = new PointLight(new Vector3f(-3, 0.1f, 3), new Vector3f(1, 1f, 1), 0.5f);
        var pointLight2 = new PointLight(new Vector3f(3, 1f, -3), new Vector3f(1, 0.5f, 1), 0.5f);
		var directionalLight1 = new DirectionalLight(new Vector3f(-1f, -1f, -1f), new Vector3f(1, 1, 1), 0.05f);
		environment.add(pointLight1);
        environment.add(pointLight2);
		environment.add(directionalLight1);

        environment.add(leftBeam);
        environment.add(rightBeam);

        renderer.updateEnvironment(environment);

		blueMat = Materials.get(new Vector3f(0, 0, 1), new Vector3f(1, 1, 1), new Vector3f(1, 1, 1), 0.5f, -1, -1, -1,
				-1, -1, -1);

		var modelFile = new FileHandle("deccer_cubes_tex.fbx", FileLocation.INTERNAL);
		var resolvedModel = modelLoader.load(modelFile).orElseThrow();
		model = graphics.createModel(resolvedModel);

		this.textureLoader = new TextureLoader(graphics);
		this.groundTexture = textureLoader.get("/landscape.png");

		var resolvedPlaneModel = new ModelBuilder().plane(groundTexture).generate();
		this.plane = graphics.createModel(resolvedPlaneModel);

        var carModelFile = new FileHandle("saab_lowpoly.fbx", FileLocation.INTERNAL);
        var resolvedCarModel = modelLoader.load(carModelFile).orElseThrow();
        this.car = graphics.createModel(resolvedCarModel);

		var camera = renderer.getCamera();
		camera.setPosition(new Vector3f(0, 5, 5));
		camera.lookAt(new Vector3f(0, 0, 0));
		camera.update();

		graphics.setClearColor(0.1f, 0.1f, 0.1f, 1f);

        var freeTypeFontLoader = new FreeTypeFontLoader();
        var msdfGenFontLoader = new MsdfGenFontLoader();
        var maybeResolvedQilkaFont = msdfGenFontLoader.load(new FileHandle("/fonts/Qilka-Bold.ttf", FileLocation.INTERNAL));
        this.qilkaFont = graphics.createFont(maybeResolvedQilkaFont.orElseThrow());
	}

	@Override
	public void onRender() {
		if (input.isKeyPressed(Key.A))
			playerX -= 0.1f;
		if (input.isKeyPressed(Key.D))
			playerX += 0.1f;
		if (input.isKeyPressed(Key.W))
			playerZ -= 0.1f;
		if (input.isKeyPressed(Key.S))
			playerZ += 0.1f;
        if (input.isKeyPressed(Key.Q))
            playerY -= 0.1f;
        if (input.isKeyPressed(Key.E))
            playerY += 0.1f;

        if (input.isKeyPressed(Key.G)) {
            playerTransform.rotation.y -= 0.5f;
        }

        if (input.isKeyPressed(Key.H)) {
            playerTransform.rotation.y += 0.5f;
        }

		var camera = renderer.getCamera();
		var cameraPos = camera.getPosition();
		if (input.isKeyPressed(Key.LEFT))
			cameraPos.x -= 1f;
		if (input.isKeyPressed(Key.RIGHT))
			cameraPos.x += 1f;
		if (input.isKeyPressed(Key.UP))
			cameraPos.z -= 1f;
		if (input.isKeyPressed(Key.DOWN))
			cameraPos.z += 1f;
		if (input.isKeyPressed(Key.SPACE))
			cameraPos.y += 1f;
		if (input.isKeyPressed(Key.ENTER))
			cameraPos.y -= 1f;
		camera.setPosition(cameraPos);
		camera.update();

		if (input.isKeyPressed(Key.ESCAPE)) {
			window.setShouldClose(true);
		}

		playerTransform.position.x = playerX;
        playerTransform.position.y = playerY;
		playerTransform.position.z = playerZ;
		playerTransform.update();

        var transformedLeftBeamPosition = new Vector4f(initialLeftBeamPosition, 1)
                .mul(playerTransform.getMatrix());
        leftBeam.setPosition(transformedLeftBeamPosition.xyz(new Vector3f()));

        var transformedLeftBeamDirection = new Vector4f(initialLeftBeamDirection, 0)
                .mul(playerTransform.getMatrix());
        leftBeam.setDirection(transformedLeftBeamDirection.xyz(new Vector3f()));

        var transformedRightBeamPosition = new Vector4f(initialRightBeamPosition, 1)
                .mul(playerTransform.getMatrix());
        rightBeam.setPosition(transformedRightBeamPosition.xyz(new Vector3f()));

        var transformedRightBeamDirection = new Vector4f(initialRightBeamDirection, 0)
                .mul(playerTransform.getMatrix());
        rightBeam.setDirection(transformedRightBeamDirection.xyz(new Vector3f()));

        renderer.updateEnvironment(environment);

		//renderer.draw(model, playerTransform);
		renderer.draw(plane, planeTransform);
        renderer.draw(car, playerTransform);
		renderer.flush();

		batch.begin();
		batch.draw(groundTexture, 300, 200, 100, 100);
        batch.draw(groundTexture, 500, 200, 100, 100);
        batch.draw(groundTexture, 700, 200, 100, 100);
		batch.end();

        renderer.drawText("Hej, jag heter Hugo", qilkaFont, 100, 550, 400);
        renderer.flushTextRenderer();
	}

	@Override
	public boolean shouldClose() {
		return true;
	}

	@Override
	public void onDestroy() {
		System.out.println("Destroying...");
	}
}
