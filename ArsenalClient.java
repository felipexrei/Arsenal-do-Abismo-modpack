package dev.arsenal.client;

import dev.arsenal.Arsenal;
import dev.arsenal.ModEntities;
import dev.arsenal.ModItems;
import dev.arsenal.entity.BossKind;
import dev.arsenal.entity.MobKind;
import dev.arsenal.item.GunItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ArsenalClient implements ClientModInitializer {
    public static boolean aiming = false;
    public static double zoomCur = 1.0;
    private static double zoomTarget = 1.0;

    private static KeyBinding reloadKey, knifeKey, aimKey;

    @Override
    public void onInitializeClient() {
        reloadKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.arsenal.reload", GLFW.GLFW_KEY_R, "key.categories.arsenal"));
        knifeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.arsenal.knife", GLFW.GLFW_KEY_F, "key.categories.arsenal"));
        aimKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.arsenal.aim", GLFW.GLFW_KEY_G, "key.categories.arsenal"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null) { aiming = false; zoomTarget = 1.0; zoomCur = 1.0; return; }
            while (reloadKey.wasPressed()) ClientPlayNetworking.send(Arsenal.RELOAD, PacketByteBufs.create());
            while (knifeKey.wasPressed()) ClientPlayNetworking.send(Arsenal.KNIFE, PacketByteBufs.create());

            ItemStack held = mc.player.getMainHandStack();
            boolean now = aimKey.isPressed() && held.getItem() instanceof GunItem && mc.currentScreen == null;
            if (now != aiming) {
                aiming = now;
                PacketByteBuf b = PacketByteBufs.create();
                b.writeBoolean(now);
                ClientPlayNetworking.send(Arsenal.AIM, b);
            }
            zoomTarget = (aiming && held.getItem() instanceof GunItem g) ? g.zoom(held) : 1.0;
            zoomCur += (zoomTarget - zoomCur) * 0.35;
        });

        ClientPlayNetworking.registerGlobalReceiver(Arsenal.RECOIL, (mc, handler, buf, sender) -> {
            float kick = buf.readFloat();
            mc.execute(() -> { if (mc.player != null) mc.player.setPitch(mc.player.getPitch() - kick); });
        });

        // HUD de munição
        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            ItemStack s = mc.player.getMainHandStack();
            if (s.getItem() instanceof GunItem g) {
                boolean rel = mc.world != null && g.isReloading(s, mc.world.getTime());
                Text t = Text.literal(g.ammo(s) + " / " + g.magSize(s) + (rel ? "  recarregando..." : ""));
                ctx.drawTextWithShadow(mc.textRenderer, t, ctx.getScaledWindowWidth() - 130, ctx.getScaledWindowHeight() - 40, 0xFFFFFF);
                if (aiming) ctx.drawCenteredTextWithShadow(mc.textRenderer, "+", ctx.getScaledWindowWidth() / 2, ctx.getScaledWindowHeight() / 2 - 4, 0xFFFFFF);
            }
        });

        // Predicados dos modelos 3D (mirando / com mira telescópica)
        for (GunItem g : ModItems.GUNS) {
            ModelPredicateProviderRegistry.register(g, Arsenal.id("aiming"), (stack, world, entity, seed) ->
                (entity != null && entity == MinecraftClient.getInstance().player && aiming) ? 1.0f : 0.0f);
            ModelPredicateProviderRegistry.register(g, Arsenal.id("scope"), (stack, world, entity, seed) ->
                g.hasScope(stack) ? 1.0f : 0.0f);
        }

        // Renderers
        EntityRendererRegistry.register(ModEntities.MINER, ctx -> new ArsenalRenderer<>(ctx, "miner", 1.0f));
        for (MobKind k : MobKind.values())
            EntityRendererRegistry.register(ModEntities.MOB.get(k), ctx -> new ArsenalRenderer<>(ctx, k.id, 1.0f));
        for (BossKind k : BossKind.values())
            EntityRendererRegistry.register(ModEntities.BOSS.get(k), ctx -> new ArsenalRenderer<>(ctx, k.entityId(), k.scale));
    }
}
