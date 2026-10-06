package dev.arsenal.item;

/** Cada acessório melhora o equilíbrio (dispersão/recuo) e cobra um preço em outro atributo. */
public enum Attachment {
    //            id           slot         spread recoil dmg   range reload delay mag
    SCOPE        ("scope",      Slot.SIGHT,  0.70,  0.95,  1.00, 1.35, 1.00,  1.10, 1.00),
    SUPPRESSOR   ("suppressor", Slot.MUZZLE, 0.95,  0.80,  0.92, 0.90, 1.00,  1.00, 1.00),
    GRIP         ("grip",       Slot.GRIP,   0.90,  0.60,  1.00, 1.00, 1.10,  1.00, 1.00),
    EXT_MAG      ("ext_mag",    Slot.MAG,    0.95,  0.90,  1.00, 1.00, 1.25,  1.00, 1.50);

    public enum Slot { SIGHT, MUZZLE, GRIP, MAG }

    public final String id;
    public final Slot slot;
    public final double spread, recoil, damage, range, reload, delay, mag;

    Attachment(String id, Slot slot, double spread, double recoil, double damage, double range, double reload, double delay, double mag) {
        this.id = id; this.slot = slot; this.spread = spread; this.recoil = recoil; this.damage = damage;
        this.range = range; this.reload = reload; this.delay = delay; this.mag = mag;
    }

    public static Attachment byId(String id) {
        for (Attachment a : values()) if (a.id.equals(id)) return a;
        return null;
    }
}
