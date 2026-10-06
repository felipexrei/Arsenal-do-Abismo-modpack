package dev.arsenal.item;

import dev.arsenal.Arsenal;
import dev.arsenal.Balance;
import dev.arsenal.ModItems;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.ToDoubleFunction;

public class GunItem extends Item {
    public final Balance.Gun gun;

    public GunItem(Balance.Gun gun) {
        super(new FabricItemSettings().maxCount(1));
        this.gun = gun;
    }

    // ---------- estado / acessórios ----------
    private static NbtCompound nbt(ItemStack s) { return s.hasNbt() ? s.getNbt() : new NbtCompound(); }

    public Attachment att(ItemStack s, Attachment.Slot slot) {
        return Attachment.byId(nbt(s).getString("att_" + slot.name()));
    }

    private double prod(ItemStack s, ToDoubleFunction<Attachment> f) {
        double r = 1;
        for (Attachment.Slot sl : Attachment.Slot.values()) {
            Attachment a = att(s, sl);
            if (a != null) r *= f.applyAsDouble(a);
        }
        return r;
    }

    public int magSize(ItemStack s) { return Math.round((float) (gun.mag * prod(s, a -> a.mag))); }
    public int ammo(ItemStack s) { return nbt(s).getInt("ammo"); }
    public boolean hasScope(ItemStack s) { return att(s, Attachment.Slot.SIGHT) == Attachment.SCOPE; }
    public boolean isReloading(ItemStack s, long now) { return nbt(s).getLong("reload_end") > now; }
    public boolean suppressed(ItemStack s) { return att(s, Attachment.Slot.MUZZLE) == Attachment.SUPPRESSOR; }

    /** Multiplicador de FOV quando mirando (menor = mais zoom). */
    public double zoom(ItemStack s) {
        double z = gun == Balance.Gun.SNIPER ? 0.25 : 0.8;
        return hasScope(s) ? z * 0.55 : z;
    }

    private Item ammoItem() { return gun.heavy ? ModItems.AMMO_HEAVY : ModItems.AMMO_LIGHT; }

    // ---------- uso ----------
    @Override
    public TypedActionResult<ItemStack> use(World w, PlayerEntity p, Hand hand) {
        ItemStack s = p.getStackInHand(hand);
        if (w.isClient || !(p instanceof ServerPlayerEntity sp)) return TypedActionResult.success(s);
        if (p.isSneaking()) { stripAttachments(sp, s); return TypedActionResult.success(s); }
        shoot(sp, s);
        return TypedActionResult.success(s);
    }

    private void stripAttachments(ServerPlayerEntity p, ItemStack s) {
        NbtCompound n = s.getOrCreateNbt();
        for (Attachment.Slot sl : Attachment.Slot.values()) {
            Attachment a = Attachment.byId(n.getString("att_" + sl.name()));
            if (a != null) {
                p.getInventory().offerOrDrop(new ItemStack(net.minecraft.registry.Registries.ITEM.get(Arsenal.id(a.id))));
                n.remove("att_" + sl.name());
            }
        }
        n.putInt("ammo", Math.min(n.getInt("ammo"), magSize(s)));
    }

    public void startReload(ServerPlayerEntity p, ItemStack s) {
        NbtCompound n = s.getOrCreateNbt();
        long now = p.getWorld().getTime();
        if (n.getLong("reload_end") > now || n.getInt("ammo") >= magSize(s)) return;
        boolean has = false;
        for (int i = 0; i < p.getInventory().size(); i++) if (p.getInventory().getStack(i).isOf(ammoItem())) { has = true; break; }
        if (!has) { p.sendMessage(Text.literal("Sem munição!").formatted(Formatting.RED), true); return; }
        n.putLong("reload_end", now + Math.round(gun.reload * prod(s, a -> a.reload)));
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.ITEM_CROSSBOW_LOADING_START, SoundCategory.PLAYERS, 1f, 1f);
        p.sendMessage(Text.literal("Recarregando...").formatted(Formatting.YELLOW), true);
    }

    @Override
    public void inventoryTick(ItemStack s, World w, Entity e, int slot, boolean selected) {
        if (w.isClient || !(e instanceof ServerPlayerEntity p) || !s.hasNbt()) return;
        NbtCompound n = s.getNbt();
        long end = n.getLong("reload_end");
        if (end == 0) return;
        if (!selected) { n.putLong("reload_end", 0); return; }
        if (w.getTime() >= end) {
            int need = magSize(s) - n.getInt("ammo"), got = 0;
            for (int i = 0; i < p.getInventory().size() && need > 0; i++) {
                ItemStack st = p.getInventory().getStack(i);
                if (st.isOf(ammoItem())) {
                    int t = Math.min(need, st.getCount());
                    st.decrement(t); need -= t; got += t;
                }
            }
            n.putInt("ammo", n.getInt("ammo") + got);
            n.putLong("reload_end", 0);
            w.playSound(null, p.getBlockPos(), SoundEvents.ITEM_CROSSBOW_LOADING_END, SoundCategory.PLAYERS, 1f, 1f);
        }
    }

    private void shoot(ServerPlayerEntity p, ItemStack s) {
        NbtCompound n = s.getOrCreateNbt();
        ServerWorld w = (ServerWorld) p.getWorld();
        if (n.getLong("reload_end") > w.getTime()) return;
        if (n.getInt("ammo") <= 0) { startReload(p, s); return; }

        boolean aim = Arsenal.AIMING.contains(p.getUuid());
        double spread = gun.spread * prod(s, a -> a.spread)
            * (aim ? 0.35 : 1.0) * (p.isSneaking() ? 0.8 : 1.0)
            * (p.getVelocity().horizontalLengthSquared() > 0.01 ? 1.5 : 1.0);
        float dmg = (float) (gun.damage * prod(s, a -> a.damage));
        double range = gun.range * prod(s, a -> a.range);

        for (int i = 0; i < gun.pellets; i++) fire(p, w, spread, dmg, range);

        n.putInt("ammo", n.getInt("ammo") - 1);
        p.getItemCooldownManager().set(this, (int) Math.max(1, Math.round(gun.delay * prod(s, a -> a.delay))));
        if (!suppressed(s)) w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 1.5f, 0.8f);
        else w.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_DISPENSER_LAUNCH, SoundCategory.PLAYERS, 0.4f, 1.6f);

        PacketByteBuf b = PacketByteBufs.create();
        b.writeFloat((float) (gun.recoil * prod(s, a -> a.recoil) * (aim ? 0.7 : 1.0)));
        ServerPlayNetworking.send(p, Arsenal.RECOIL, b);
    }

    private void fire(ServerPlayerEntity p, ServerWorld w, double spreadDeg, float dmg, double range) {
        Vec3d start = p.getEyePos();
        double rp = Math.toRadians(p.getPitch() + p.getRandom().nextGaussian() * spreadDeg);
        double ry = Math.toRadians(p.getYaw() + p.getRandom().nextGaussian() * spreadDeg);
        Vec3d dir = new Vec3d(-Math.sin(ry) * Math.cos(rp), -Math.sin(rp), Math.cos(ry) * Math.cos(rp));
        Vec3d end = start.add(dir.multiply(range));
        BlockHitResult bh = w.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
        if (bh.getType() != HitResult.Type.MISS) end = bh.getPos();
        EntityHitResult eh = ProjectileUtil.raycast(p, start, end, new Box(start, end).expand(1.0),
            e -> !e.isSpectator() && e.canHit(), start.squaredDistanceTo(end));
        if (eh != null) {
            Entity t = eh.getEntity();
            t.timeUntilRegen = 0;
            t.damage(w.getDamageSources().playerAttack(p), dmg);
            end = eh.getPos();
            w.spawnParticles(ParticleTypes.CRIT, end.x, end.y, end.z, 4, 0.1, 0.1, 0.1, 0.1);
        }
        double len = start.distanceTo(end);
        for (double d = 2; d < len; d += 2.5) {
            Vec3d q = start.add(dir.multiply(d));
            w.spawnParticles(ParticleTypes.ELECTRIC_SPARK, q.x, q.y, q.z, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false; // evita o "balanço" a cada tiro/recarga
    }

    @Override
    public void appendTooltip(ItemStack s, @Nullable World w, List<Text> tip, TooltipContext ctx) {
        tip.add(Text.literal("Dano: " + String.format("%.1f", gun.damage * prod(s, a -> a.damage)) + (gun.pellets > 1 ? " x" + gun.pellets : "")).formatted(Formatting.GRAY));
        tip.add(Text.literal("Munição: " + ammo(s) + "/" + magSize(s)).formatted(Formatting.GRAY));
        for (Attachment.Slot sl : Attachment.Slot.values()) {
            Attachment a = att(s, sl);
            if (a != null) tip.add(Text.literal("+ " + a.id).formatted(Formatting.AQUA));
        }
        tip.add(Text.literal("Agachar + botão direito remove acessórios").formatted(Formatting.DARK_GRAY));
    }
}
