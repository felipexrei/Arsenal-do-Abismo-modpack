package dev.arsenal.entity;

/** Mobs "comuns" do mod, um por dimensão (e também espalhados no Nether/End). */
public enum MobKind {
    //         id               vida  dano  vel   atira  fogo   kbRes  ovoA      ovoB
    RENEGADE ("renegade",       24,   4.0,  0.27, true,  false, 0.0,  0x4a5d3a, 0xc2b280),
    STALKER  ("abyss_stalker",  42,   8.0,  0.33, false, true,  0.2,  0x1b1b2f, 0x6a2c91),
    SENTINEL ("sky_sentinel",   36,   7.0,  0.26, false, false, 0.5,  0xdfe6ee, 0x4fa3d9);

    public final String id;
    public final double health, damage, speed, kbRes;
    public final boolean ranged, fireImmune;
    public final int eggA, eggB;

    MobKind(String id, double health, double damage, double speed, boolean ranged, boolean fireImmune, double kbRes, int eggA, int eggB) {
        this.id = id; this.health = health; this.damage = damage; this.speed = speed;
        this.ranged = ranged; this.fireImmune = fireImmune; this.kbRes = kbRes; this.eggA = eggA; this.eggB = eggB;
    }
}
