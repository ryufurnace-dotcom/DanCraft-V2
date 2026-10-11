package com.dancraft.rpg;

/**
 * De onde vem o XP. Cada fonte tem sua própria "fadiga": repetir a mesma ação rende cada vez menos,
 * e a fadiga se recupera com o tempo. Isso impede farm de uma coisa só e incentiva variar a atividade.
 */
public enum XpSource {
    MINING(120, 2.0),
    WOODCUTTING(80, 1.5),
    FARMING(60, 1.0),
    DIGGING(40, 1.0),
    CRAFTING(60, 1.0),
    SMELTING(40, 1.0),
    FISHING(60, 0.8),
    COMBAT(150, 3.0),
    EXPLORATION(1_000_000, 0),   // descobertas únicas, sem fadiga
    ACHIEVEMENT(1_000_000, 0);   // feitos únicos, sem fadiga

    private final double softCap;
    private final double decayPerSecond;

    XpSource(double softCap, double decayPerSecond) {
        this.softCap = softCap;
        this.decayPerSecond = decayPerSecond;
    }

    /** Fadiga acumulada em que o XP cai pela metade. */
    public double softCap() {
        return softCap;
    }

    public double decayPerSecond() {
        return decayPerSecond;
    }
}
