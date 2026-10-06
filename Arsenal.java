package dev.arsenal;

import dev.arsenal.item.GunItem;
import dev.arsenal.item.KnifeHandler;
import dev.arsenal.world.ModWorldgen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Arsenal implements ModInitializer {
    public static final String ID = "arsenal";
    public static final Identifier RELOAD = id("reload");
    public static final Identifier KNIFE = id("knife");
    public static final Identifier AIM = id("aim");
    public static final Identifier RECOIL = id("recoil");
    /** Jogadores que estão mirando (servidor usa para reduzir a dispersão). */
    public static final Set<UUID> AIMING = ConcurrentHashMap.newKeySet();

    public static Identifier id(String path) { return new Identifier(ID, path); }

    @Override
    public void onInitialize() {
        ModEntities.init();
        ModBlocks.init();
        ModItems.init();
        ModWorldgen.init();

        ServerPlayNetworking.registerGlobalReceiver(RELOAD, (server, player, handler, buf, sender) ->
            server.execute(() -> {
                ItemStack s = player.getMainHandStack();
                if (s.getItem() instanceof GunItem g) g.startReload(player, s);
            }));
        ServerPlayNetworking.registerGlobalReceiver(KNIFE, (server, player, handler, buf, sender) ->
            server.execute(() -> KnifeHandler.toggle(player)));
        ServerPlayNetworking.registerGlobalReceiver(AIM, (server, player, handler, buf, sender) -> {
            boolean aim = buf.readBoolean();
            server.execute(() -> {
                if (aim) AIMING.add(player.getUuid()); else AIMING.remove(player.getUuid());
            });
        });
        ServerPlayConnectionEvents.DISCONNECT.register((h, s) -> AIMING.remove(h.getPlayer().getUuid()));
    }
}
