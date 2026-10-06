package dev.arsenal.client;

import dev.arsenal.Arsenal;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

/** Renderer genérico (modelo humanoide) com textura e escala próprias. */
public class ArsenalRenderer<T extends MobEntity> extends BipedEntityRenderer<T, BipedEntityModel<T>> {
    private final Identifier texture;
    private final float scale;

    public ArsenalRenderer(EntityRendererFactory.Context ctx, String textureId, float scale) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f * scale);
        this.texture = Arsenal.id("textures/entity/" + textureId + ".png");
        this.scale = scale;
    }

    @Override public Identifier getTexture(T entity) { return texture; }

    @Override
    protected void scale(T entity, MatrixStack matrices, float amount) {
        matrices.scale(scale, scale, scale);
    }
}
