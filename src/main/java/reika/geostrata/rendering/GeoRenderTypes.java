package reika.geostrata.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import reika.geostrata.GeoStrata;

public class GeoRenderTypes extends RenderType {
    public static final RenderType SPIKE = RenderType.create("spike",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            256, false, false,

            RenderType.CompositeState.builder()
            .setShaderState(POSITION_TEX_SHADER)
            .setTextureState(new TextureStateShard(new ResourceLocation(GeoStrata.MODID, "textures/block/deco/0.png"), false, false))
            .setTransparencyState(NO_TRANSPARENCY)
            .setLightmapState(LIGHTMAP)
            .setCullState(NO_CULL)
            .createCompositeState(false));


    public GeoRenderTypes(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

}