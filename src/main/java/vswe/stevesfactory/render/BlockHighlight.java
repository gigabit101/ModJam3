package vswe.stevesfactory.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_LINES;

@EventBusSubscriber(modid = StevesFactoryManagerAPI.MODID, value = Dist.CLIENT, bus = Bus.FORGE)
public final class BlockHighlight {

    private static List<BlockHighlight> highlights = new ArrayList<>();

    public static void createHighlight(BlockPos pos, int expire) {
        long expireTime = Minecraft.getInstance().world.getGameTime() + expire;
        highlights.add(new BlockHighlight(pos, expireTime));
    }

    @SubscribeEvent
    public static void renderWorldLast(RenderWorldLastEvent event) {
        for (BlockHighlight highlight : highlights) {
            highlight.render(event);
        }
        highlights.removeIf(BlockHighlight::isExpired);
    }

    private final BlockPos pos;
    private final long expireTime;

    public BlockHighlight(BlockPos pos, long expireTime) {
        this.pos = pos;
        this.expireTime = expireTime;
    }

    public void render(RenderWorldLastEvent event) {
        renderOutline(event, pos);
    }

    public boolean isExpired() {
        return Minecraft.getInstance().world.getGameTime() > expireTime;
    }

    public static void renderOutline(RenderWorldLastEvent event, BlockPos c) {
        val vpos = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        val ms = event.getMatrixStack();
        ms.push();
        ms.translate(-vpos.x, -vpos.y, -vpos.z);
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(3);

        val tess = Tessellator.getInstance();
        val builder = tess.getBuffer();
        float mx = c.getX();
        float my = c.getY();
        float mz = c.getZ();
        builder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        WorldRenderer.drawBoundingBox(ms, builder, mx, my, mz, mx + 1, my + 1, mz + 1, 1F, 0F, 0F, 1F);
        tess.draw();

        RenderSystem.enableTexture();
        ms.pop();
    }
}
