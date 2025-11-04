package io.semillita.hugame.sandbox;

import dev.hugame.application.HuGameApplicationContext;
import dev.hugame.application.SimpleApplicationConfiguration;
import dev.hugame.application.SimpleApplicationListener;
import dev.hugame.application.SimpleHuGameApplication;
import dev.hugame.assimp.AssimpModelLoader;
import dev.hugame.core.Input;
import dev.hugame.desktop.gl.GLGraphics;
import dev.hugame.environment.DirectionalLight;
import dev.hugame.environment.Environment;
import dev.hugame.environment.PointLight;
import dev.hugame.environment.SpotLight;
import dev.hugame.graphics.Graphics;
import dev.hugame.graphics.Renderer;
import dev.hugame.graphics.model.Model;
import dev.hugame.input.Key;
import dev.hugame.model.spec.ResolvedMaterial;
import dev.hugame.model.spec.ResolvedMesh;
import dev.hugame.model.spec.ResolvedModel;
import dev.hugame.model.spec.ResolvedVertex;
import dev.hugame.util.TextureLoader;
import dev.hugame.util.Transform;
import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.surface.GlfwSurfaceContext;
import dev.hugame.window.DesktopInput;
import dev.hugame.window.DesktopWindow;
import dev.hugame.window.WindowConfiguration;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class MeshGen implements SimpleApplicationListener {
	public static void main(String[] args) {
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
			var useVulkan = true;

			var window = new DesktopWindow(windowConfig, (width, height) -> System.out.println("Resizing viewport"), false);
			var graphics = new VulkanGraphics(new GlfwSurfaceContext(window));
			var input = new DesktopInput(window);

			var assimpModelLoader = new AssimpModelLoader(new TextureLoader(graphics));
			return new SimpleApplicationConfiguration(graphics, window, input, List.of(assimpModelLoader));
		};

		var application = new SimpleHuGameApplication(new MeshGen(), configurer);
		application.start();
	}

	private Model model;
	private Renderer renderer;
	private Input input;
	private Transform transform;

	@Override
	public void onCreate(HuGameApplicationContext applicationContext) {
		/*var heightMap = new int[][] {
				{0, 1, 2},
				{0, 2, 4},
				{1, 3, 2}
		};*/

		var heightMap = new int[][] {
				{0, 0, 0, 1},
				{1, 1, 1, 1},
				{0, 0, 3, 1},
				{1, 2, 3, 1}
		};

		var vertices = new ArrayList<ResolvedVertex>();
		var indices = new ArrayList<Integer>();

		var scale = 5;
		var yScale = 2;
		for (int x = 0; x < heightMap.length - 1; x++) {
			for (int z = 0; z < heightMap[0].length - 1; z++) {
				var offset = vertices.size();

				// 3------2
				// | \    |
				// |    \ |
				// 0------1

				var vertexPos0 = new Vector3f(x * scale, heightMap[x][z] * yScale, -z * scale);
				var vertexPos1 = new Vector3f((x+1) * scale, heightMap[x+1][z] * yScale, -z * scale);
				var vertexPos2 = new Vector3f((x+1) * scale, heightMap[x+1][z+1] * yScale, -(z+1) * scale);
				var vertexPos3 = new Vector3f(x * scale, heightMap[x][z+1] * yScale, -(z+1) * scale);

				//var normal0 = new Vector3f(vertexPos0).min(vertexPos1).cross(new Vector3f(vertexPos0).min(vertexPos3));
				var normal1 = new Vector3f(vertexPos1).min(vertexPos2).cross(new Vector3f(vertexPos1).min(vertexPos3));
				var normal0 = new Vector3f(vertexPos3).min(vertexPos1).cross(new Vector3f(vertexPos0).min(vertexPos1));
				//var normal1 = new Vector3f(vertexPos3).min(vertexPos2).cross(new Vector3f(vertexPos1).min(vertexPos2));

				var textureCoordinates = new Vector2f();

				vertices.add(new ResolvedVertex(vertexPos1, normal0, textureCoordinates));
				vertices.add(new ResolvedVertex(vertexPos3, normal0, textureCoordinates));
				vertices.add(new ResolvedVertex(vertexPos0, normal0, textureCoordinates));

				vertices.add(new ResolvedVertex(vertexPos2, normal1, textureCoordinates));
				vertices.add(new ResolvedVertex(vertexPos3, normal1, textureCoordinates));
				vertices.add(new ResolvedVertex(vertexPos1, normal1, textureCoordinates));

				indices.addAll(IntStream.of(0, 1, 2, 3, 4, 5).map(i -> i + offset).boxed().toList());
			}
		}

		var resolvedModel = new ResolvedModel(
				List.of(
						new ResolvedMesh(
								vertices,
								indices,
								0)),
				List.of(
						new ResolvedMaterial(
								Optional.of(new Vector3f(1, 1, 1)),
								Optional.empty(),
								Optional.empty(),
								Optional.empty())));

		var graphics = applicationContext.getGraphics();
		graphics.setClearColor(1, 0, 0, 0.5f);
		model = graphics.createModel(resolvedModel);
		renderer = graphics.getRenderer();
		this.input = applicationContext.getInput();

		this.transform = Transform.identity();

		var camera = renderer.getCamera();
		camera.setPosition(new Vector3f(0, 15, 10));
		camera.lookAt(new Vector3f(0, 0, 0));
		camera.update();

		var environment = new Environment();
		var pointLight1 = new PointLight(new Vector3f(0, 10, 0), new Vector3f(1, 0, 1), 1f);
		var spotLight1 = new SpotLight(new Vector3f(0, 20, 0), new Vector3f(0, -1, 0), new Vector3f(0, 1, 1), 5, 0.5f);
		var directionalLight1 = new DirectionalLight(new Vector3f(1f, -1f, -1f), new Vector3f(1, 1, 1), 0.5f);
		environment.add(pointLight1);
		environment.add(spotLight1);
		environment.add(directionalLight1);
		renderer.updateEnvironment(environment);
	}

	@Override
	public void onRender() {
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

		renderer.draw(model, transform);
		renderer.flush();
	}

	@Override
	public void onDestroy() {

	}
}
