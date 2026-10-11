package com.dancraft.rpg;

import java.util.Locale;
import java.util.UUID;

/** Os cinco atributos do personagem (Falna). */
public enum RpgAttribute {
    STRENGTH("Força", UUID.fromString("5d1b2a10-0001-4c3e-9a11-d4a2c0de0001")),
    AGILITY("Agilidade", UUID.fromString("5d1b2a10-0002-4c3e-9a11-d4a2c0de0002")),
    ENDURANCE("Resistência", UUID.fromString("5d1b2a10-0003-4c3e-9a11-d4a2c0de0003")),
    DEXTERITY("Destreza", UUID.fromString("5d1b2a10-0004-4c3e-9a11-d4a2c0de0004")),
    MAGIC("Magia", UUID.fromString("5d1b2a10-0005-4c3e-9a11-d4a2c0de0005"));

    // Bônus por ponto (ponto de partida de balanceamento)
    public static final double DAMAGE_PER_STRENGTH = 0.10;
    public static final double SPEED_PER_AGILITY = 0.004;      // +0,4% por ponto
    public static final double HEALTH_PER_ENDURANCE = 0.5;
    public static final double ATTACK_SPEED_PER_DEXTERITY = 0.004;
    public static final double LUCK_PER_DEXTERITY = 0.04;

    private final String label;
    private final UUID modifierId;

    RpgAttribute(String label, UUID modifierId) {
        this.label = label;
        this.modifierId = modifierId;
    }

    public String label() {
        return label;
    }

    public UUID modifierId() {
        return modifierId;
    }

    /** Texto curto do efeito para a tela de status. */
    public String describe(int value) {
        return switch (this) {
            case STRENGTH -> String.format(Locale.ROOT, "+%.1f dano", value * DAMAGE_PER_STRENGTH);
            case AGILITY -> String.format(Locale.ROOT, "+%.1f%% velocidade", value * SPEED_PER_AGILITY * 100);
            case ENDURANCE -> String.format(Locale.ROOT, "+%.1f vida", value * HEALTH_PER_ENDURANCE);
            case DEXTERITY -> String.format(Locale.ROOT, "+%.1f%% vel. ataque", value * ATTACK_SPEED_PER_DEXTERITY * 100);
            case MAGIC -> "mana (em breve)";
        };
    }
}
