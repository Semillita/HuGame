package dev.hugame.vulkan.buffer;

import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.graphics.spec.buffer.ShaderStorageBuffer;
import dev.hugame.util.Bufferable;
import dev.hugame.util.Logger;
import dev.hugame.vulkan.core.VulkanGraphics;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.system.MemoryUtil;

public class VulkanShaderStorageBuffer<T extends Bufferable> implements ShaderStorageBuffer<T> {
  private static final int SHADER_STORAGE_BUFFER_USAGE_FLAGS =
      VK_BUFFER_USAGE_STORAGE_BUFFER_BIT
          | VK_BUFFER_USAGE_TRANSFER_SRC_BIT
          | VK_BUFFER_USAGE_TRANSFER_DST_BIT;
  private static final int SHADER_STORAGE_BUFFER_PROPERTIES_FLAGS =
      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;

  public static <T extends Bufferable> VulkanShaderStorageBuffer<T> create(
      VulkanGraphics graphics, int bytesPerItem, int maxItems) {
    var bufferSize = bytesPerItem * maxItems;

    var buffer =
        BufferUtils.createPersistentlyMappedBuffer(
            graphics,
            bufferSize,
            SHADER_STORAGE_BUFFER_USAGE_FLAGS,
            SHADER_STORAGE_BUFFER_PROPERTIES_FLAGS);

    return new VulkanShaderStorageBuffer<>(graphics, buffer, bytesPerItem, maxItems);
  }

  private final VulkanGraphics graphics;
  // private VulkanPersistentlyMappedBuffer buffer;
  private VulkanBuffer buffer;
  private final int bytesPerItem;
  private int itemCount;
  private int maxItems;

  private VulkanShaderStorageBuffer(
      VulkanGraphics graphics,
      // VulkanPersistentlyMappedBuffer buffer,
      VulkanBuffer buffer,
      int bytesPerItem,
      int maxItems) {
    this.graphics = graphics;
    this.buffer = buffer;
    this.bytesPerItem = bytesPerItem;
    this.itemCount = 0;
    this.maxItems = maxItems;
  }

  @Override
  public int getItemCount() {
    return itemCount;
  }

  @Override
  public int getMaxItems() {
    return maxItems;
  }

  @Override
  public int getBytesPerItem() {
    return bytesPerItem;
  }

  @Override
  public void allocate(int maxItems) {
    BufferUtils.destroyBuffer(graphics, buffer);

    this.buffer =
        BufferUtils.createDynamicBuffer(
            graphics,
            bytesPerItem * maxItems,
            SHADER_STORAGE_BUFFER_USAGE_FLAGS,
            SHADER_STORAGE_BUFFER_PROPERTIES_FLAGS);

    this.maxItems = maxItems;
  }

  @Override
  public void fill(List<T> items, int maxItems) {
    allocate(maxItems);
    refill(items);
  }

  @Override
  public void refill(List<T> items) {
    var data = MemoryUtil.memCalloc(items.size() * bytesPerItem);
    for (var item : items) {
      data.put(item.getBytes());
    }

    var bytes = new ArrayList<String>();
    for (int i = 0; i < items.size() * bytesPerItem; i++) {
      var b = data.get(i);
      bytes.add(String.format("0x%02X", b));
    }
    data.rewind();
    BufferUtils.fillWithStagingBuffer(graphics, buffer, data);

    itemCount = items.size();
  }

  public VulkanBuffer getBuffer() {
    return buffer;
  }
}
