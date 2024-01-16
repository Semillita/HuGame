package dev.hugame.vulkan.buffer;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.graphics.Camera;
import dev.hugame.vulkan.core.VulkanGraphics;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import org.lwjgl.system.MemoryUtil;

public class VulkanUniformBuffer {
  private static final int UNIFORM_BUFFER_USAGE_FLAGS = VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
  private static final int UNIFORM_BUFFER_PROPERTIES_FLAGS =
      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;

  public static VulkanUniformBuffer create(VulkanGraphics graphics, int bufferSize) {
    var buffer =
        BufferUtils.createPersistentlyMappedBuffer(
            graphics, bufferSize, UNIFORM_BUFFER_USAGE_FLAGS, UNIFORM_BUFFER_PROPERTIES_FLAGS);

    return new VulkanUniformBuffer(buffer, bufferSize);
  }

  private final VulkanPersistentlyMappedBuffer buffer;
  private final int bufferSize;

  private VulkanUniformBuffer(VulkanPersistentlyMappedBuffer buffer, int bufferSize) {
    this.buffer = buffer;
    this.bufferSize = bufferSize;
  }

  public VulkanPersistentlyMappedBuffer getBuffer() {
    return buffer;
  }

  public void update(Consumer<ByteBuffer> fillBuffer) {
    fillBuffer.accept(buffer.getMappedMemory().rewind());
  }

  public void update(Camera camera) {
    var uboBuffer = MemoryUtil.memCalloc(3 * 16 * Float.BYTES);

    var view = camera.getViewMatrix();
    view.get(16 * Float.BYTES, uboBuffer);

    var projection = camera.getProjectionMatrix();
    projection.get(2 * 16 * Float.BYTES, uboBuffer);

    buffer.getMappedMemory().rewind().put(uboBuffer);
  }

  public int getBufferSize() {
    return bufferSize;
  }
}
