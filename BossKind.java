package dev.arsenal.entity;

import net.minecraft.entity.boss.BossBar;

public enum BossKind {
    //            id         vida   dano  vel   escala armadura cor                     fogo
    COLOSSUS("colossus",     300,   14,   0.25, 2.2f,  12,     BossBar.Color.RED,       false),
    WRAITH  ("wraith",       260,   11,   0.36, 1.5f,   4,     BossBar.Color.PURPLE,    false),
    DRAKE   ("drake",        420,   16,   0.30, 1.9f,   8,     BossBar.Color.GREEN,     true),
    VOID    ("void",         650,   20,   0.32, 2.1f,  14,     BossBar.Color.PINK,      true);

    public final String id;
    public final double health, damage, speed, armor;
    public final float scale;
    public final BossBar.Color color;
    public final boolean fireImmune;

    BossKind(String id, double health, double damage, double speed, float scale, double armor, BossBar.Color color, boolean fireImmune) {
        this.id = id; this.health = health; this.damage = damage; this.speed = speed;
        this.scale = scale; this.armor = armor; this.color = color; this.fireImmune = fireImmune;
    }

    public String entityId() { return "boss_" + id; }
}
