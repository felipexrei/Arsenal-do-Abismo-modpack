package dev.arsenal;

/** TODO de balanceamento: mexa só aqui. */
public final class Balance {
    private Balance() {}

    public enum Gun {
        //       id        dano   delay mag reload spread range pellets recoil heavy
        PISTOL ("pistol",   5.0f,  8,   12,  40,   1.5f,   60,  1,      1.5f,  false),
        RIFLE  ("rifle",    4.0f,  3,   30,  60,   2.5f,   90,  1,      1.0f,  false),
        SHOTGUN("shotgun",  2.5f, 25,    6,  70,   7.0f,   25,  8,      4.0f,  true),
        SNIPER ("sniper",  18.0f, 40,    5,  80,   0.2f,  200,  1,      6.0f,  true);

        public final String id;
        public final float damage;
        public final int delay, mag, reload;
        public final float spread;
        public final int range, pellets;
        public final float recoil;
        public final boolean heavy;

        Gun(String id, float damage, int delay, int mag, int reload, float spread, int range, int pellets, float recoil, boolean heavy) {
            this.id = id; this.damage = damage; this.delay = delay; this.mag = mag; this.reload = reload;
            this.spread = spread; this.range = range; this.pellets = pellets; this.recoil = recoil; this.heavy = heavy;
        }
    }
}
