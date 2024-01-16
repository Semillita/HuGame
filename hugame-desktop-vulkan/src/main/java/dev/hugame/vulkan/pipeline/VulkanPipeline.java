package dev.hugame.vulkan.pipeline;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import dev.hugame.vulkan.core.VulkanGraphics;
import dev.hugame.vulkan.layout.DescriptorFactory;
import dev.hugame.vulkan.layout.VulkanDescriptorSetLayout;
import dev.hugame.vulkan.pipeline.shader.ShaderFactory;
import dev.hugame.vulkan.pipeline.shader.ShaderType;
import dev.hugame.vulkan.pipeline.shader.ShaderUtils;
import org.lwjgl.vulkan.*;

// TODO: Make this class, or the shaders, own the descriptors.
// TODO: Create the pipeline and descriptors through a builder
public class VulkanPipeline {
  private static final String SHADER_ENTRYPOINT_METHOD_NAME = "main";

  public static VulkanPipeline create(
      VulkanGraphics graphics,
      DescriptorFactory descriptorFactory,
      VulkanDescriptorSetLayout descriptorSetLayout,
      String vertexShaderSource,
      String fragmentShaderSource) {
    var logicalDevice = graphics.getDevice().getLogical();

    try (var memoryStack = stackPush()) {
      var vertexSpirVSource = ShaderUtils.compile(vertexShaderSource, ShaderType.VERTEX);
      var fragmentSpirVSource = ShaderUtils.compile(fragmentShaderSource, ShaderType.FRAGMENT);

      var vertexShaderModuleHandle = ShaderFactory.createShaderModule(graphics, vertexSpirVSource);
      var fragmentShaderModuleHandle =
          ShaderFactory.createShaderModule(graphics, fragmentSpirVSource);

      var shaderStageBuffer = VkPipelineShaderStageCreateInfo.calloc(2, memoryStack);
      shaderStageBuffer
          .get(0)
          .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
          .stage(VK_SHADER_STAGE_VERTEX_BIT)
          .module(vertexShaderModuleHandle)
          .pName(memoryStack.UTF8(SHADER_ENTRYPOINT_METHOD_NAME));

      shaderStageBuffer
          .get(1)
          .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
          .stage(VK_SHADER_STAGE_FRAGMENT_BIT)
          .module(fragmentShaderModuleHandle)
          .pName(memoryStack.UTF8(SHADER_ENTRYPOINT_METHOD_NAME));

      var dynamicStateCreateInfo =
          VkPipelineDynamicStateCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
              .pDynamicStates(
                  memoryStack.ints(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR));

      var vertexBindingDescriptions = descriptorFactory.getBindingDescriptions(memoryStack);
      var vertexAttributeDescriptions = descriptorFactory.getAttributeDescriptions(memoryStack);

      var vertexInputStateCreateInfo =
          VkPipelineVertexInputStateCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
              .pVertexBindingDescriptions(vertexBindingDescriptions)
              .pVertexAttributeDescriptions(vertexAttributeDescriptions);

      var inputAssemblyStateCreateInfo =
          VkPipelineInputAssemblyStateCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
              .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
              .primitiveRestartEnable(false);

      var viewportStateCreateInfo =
          VkPipelineViewportStateCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
              .viewportCount(1)
              .scissorCount(1);

      var rasterizationStateCreateInfo =
          VkPipelineRasterizationStateCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
              .depthClampEnable(false)
              .rasterizerDiscardEnable(false)
              .polygonMode(VK_POLYGON_MODE_FILL)
              .lineWidth(1)
              .cullMode(VK_CULL_MODE_BACK_BIT)
              .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
              .depthBiasEnable(false)
              .depthBiasConstantFactor(0)
              .depthBiasClamp(0)
              .depthBiasSlopeFactor(0);

      var multisampleStateCreateInfo =
          VkPipelineMultisampleStateCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
              .sampleShadingEnable(false)
              .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT)
              .minSampleShading(1)
              .pSampleMask(null)
              .alphaToCoverageEnable(false)
              .alphaToOneEnable(false);

      var colorBlendAttachmentStateBuffer =
          VkPipelineColorBlendAttachmentState.calloc(1, memoryStack);
      colorBlendAttachmentStateBuffer
          .get(0)
          .colorWriteMask(
              VK_COLOR_COMPONENT_R_BIT
                  | VK_COLOR_COMPONENT_G_BIT
                  | VK_COLOR_COMPONENT_B_BIT
                  | VK_COLOR_COMPONENT_A_BIT)
          .blendEnable(true)
          .srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA)
          .dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA)
          .colorBlendOp(VK_BLEND_OP_ADD)
          .srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
          .dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO)
          .alphaBlendOp(VK_BLEND_OP_ADD);

      var colorBlendStateCreateInfo =
          VkPipelineColorBlendStateCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
              .logicOpEnable(false)
              .logicOp(VK_LOGIC_OP_COPY)
              .pAttachments(colorBlendAttachmentStateBuffer);

      var pipelineLayoutCreateInfo =
          VkPipelineLayoutCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
              .pSetLayouts(memoryStack.longs(descriptorSetLayout.getHandle()))
              .pPushConstantRanges(null);

      var pipelineLayoutHandleBuffer = memoryStack.callocLong(1);

      if (vkCreatePipelineLayout(
              logicalDevice, pipelineLayoutCreateInfo, null, pipelineLayoutHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create pipeline layout");
      }

      var pipelineLayoutHandle = pipelineLayoutHandleBuffer.get(0);

      var renderPass = VulkanRenderPass.create(graphics);

      var depthStencilStateCreateInfo =
          VkPipelineDepthStencilStateCreateInfo.calloc(memoryStack)
              .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
              .depthTestEnable(true)
              .depthWriteEnable(true)
              .depthCompareOp(VK_COMPARE_OP_LESS)
              .depthBoundsTestEnable(false)
              .minDepthBounds(0)
              .maxDepthBounds(1)
              .stencilTestEnable(false);

      var pipelineCreateInfoBuffer = VkGraphicsPipelineCreateInfo.calloc(1, memoryStack);
      pipelineCreateInfoBuffer
          .get(0)
          .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
          .pStages(shaderStageBuffer)
          .pVertexInputState(vertexInputStateCreateInfo)
          .pInputAssemblyState(inputAssemblyStateCreateInfo)
          .pViewportState(viewportStateCreateInfo)
          .pRasterizationState(rasterizationStateCreateInfo)
          .pMultisampleState(multisampleStateCreateInfo)
          .pDepthStencilState(null)
          .pColorBlendState(colorBlendStateCreateInfo)
          .pDynamicState(dynamicStateCreateInfo)
          .layout(pipelineLayoutHandle)
          .renderPass(renderPass.getHandle())
          .subpass(0)
          .basePipelineHandle(VK_NULL_HANDLE)
          .basePipelineIndex(-1)
          .pDepthStencilState(depthStencilStateCreateInfo);

      var pipelineHandleBuffer = memoryStack.callocLong(1);
      if (vkCreateGraphicsPipelines(
              logicalDevice, VK_NULL_HANDLE, pipelineCreateInfoBuffer, null, pipelineHandleBuffer)
          != VK_SUCCESS) {
        throw new RuntimeException("[HuGame] Failed to create graphics pipeline");
      }

      var pipelineHandle = pipelineHandleBuffer.get(0);

      vkDestroyShaderModule(logicalDevice, vertexShaderModuleHandle, null);
      vkDestroyShaderModule(logicalDevice, fragmentShaderModuleHandle, null);

      return new VulkanPipeline(pipelineHandle, pipelineLayoutHandle, renderPass);
    }
  }

  private final long handle;
  private final long layoutHandle;
  private final VulkanRenderPass renderPass;

  private VulkanPipeline(long handle, long layoutHandle, VulkanRenderPass renderPass) {
    this.handle = handle;
    this.layoutHandle = layoutHandle;
    this.renderPass = renderPass;
  }

  public long getHandle() {
    return handle;
  }

  public long getLayoutHandle() {
    return layoutHandle;
  }

  public VulkanRenderPass getRenderPass() {
    return renderPass;
  }
}
