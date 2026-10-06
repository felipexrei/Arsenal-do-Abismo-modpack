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
    public static final Set<UUID> AIMING = ConcurrentHashMap.newKeySet()

