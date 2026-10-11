package com.dancraft.rpg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Dados de RPG de um jogador: XP, nível, atributos, Excelia e Falna. */
public class PlayerRpg {
    public static final int MAX_LEVEL = 100;
    public static final int MAX_FALNA_LEVEL = 10;
    public static final int POINTS_PER_LEVEL = 3;
    public static final int POINTS_PER_FALNA_LEVEL = 5;
    public static final int MAX_ATTRIBUTE_VALUE = 200;

    /** Excelia total exigida para chegar a cada nível da Falna (índice = nível alvo). */
    private static final long[] FALNA_EXCELIA = {0, 0, 100, 300, 700, 1300, 2200, 3500, 5200, 7500, 10500};

    private int level = 1;
    private double xp = 0;
    private int statPoints = 0;
    private final int[] attributes = new int[RpgAttribute.values().length];
    private long excelia = 0;
    private int falnaLevel = 0; // 0 = ainda não recebeu a Falna

    private final Set<String> firstKills = new HashSet<>();
    private final Set<String> visitedBiomes = new HashSet<>();

    // Estado temporário (não é salvo)
    private final Map<XpSource, Double> fatigue = new EnumMap<>(XpSource.class);
    private boolean dirty = false;
    private double pendingGain = 0;

    // ---------- Progressão de nível ----------

    /** XP necessário para sair do nível informado. */
    public static int xpForLevel(int level) {
        return (int) Math.round(30 + 12.0 * level + 0.8 * level * level);
    }

    public int getLevel() {
        return level;
    }

    public double getXp() {
        return xp;
    }

    public int xpToNext() {
        return xpForLevel(level);
    }

    public double xpProgress() {
        if (level >= MAX_LEVEL) return 1.0;
        return Math.min(1.0, xp / xpToNext());
    }

    /** Soma XP e devolve quantos níveis foram ganhos. */
    public int addXp(double amount) {
        if (level >= MAX_LEVEL || amount <= 0) return 0;
        xp += amount;
        int gained = 0;
        while (level < MAX_LEVEL && xp >= xpToNext()) {
            xp -= xpToNext();
            level++;
            statPoints += POINTS_PER_LEVEL;
            gained++;
        }
        if (level >= MAX_LEVEL) xp = 0;
        dirty = true;
        return gained;
    }

    // ---------- Atributos ----------

    public int getStatPoints() {
        return statPoints;
    }

    public int getAttribute(RpgAttribute attribute) {
        return attributes[attribute.ordinal()];
    }

    public boolean spendPoint(RpgAttribute attribute) {
        if (!hasFalna() || statPoints <= 0) return false;
        if (attributes[attribute.ordinal()] >= MAX_ATTRIBUTE_VALUE) return false;
        attributes[attribute.ordinal()]++;
        statPoints--;
        dirty = true;
        return true;
    }

    // ---------- Excelia e Falna ----------

    public long getExcelia() {
        return excelia;
    }

    public void addExcelia(long amount) {
        if (amount <= 0) return;
        excelia += amount;
        dirty = true;
    }

    public boolean hasFalna() {
        return falnaLevel >= 1;
    }

    public int getFalnaLevel() {
        return falnaLevel;
    }

    public void grantFalna() {
        if (falnaLevel < 1) {
            falnaLevel = 1;
            dirty = true;
        }
    }

    public void setFalnaLevel(int value) {
        falnaLevel = Math.max(0, Math.min(MAX_FALNA_LEVEL, value));
        dirty = true;
    }

    public long nextFalnaExcelia() {
        int target = Math.min(MAX_FALNA_LEVEL, falnaLevel + 1);
        return FALNA_EXCELIA[target];
    }

    public int nextFalnaPlayerLevel() {
        int target = Math.min(MAX_FALNA_LEVEL, falnaLevel + 1);
        return (target - 1) * 10;
    }

    /** Motivo pelo qual a Falna ainda não pode subir, ou null se pode. */
    public String falnaBlockReason() {
        if (!hasFalna()) return "Você ainda não recebeu a Falna.";
        if (falnaLevel >= MAX_FALNA_LEVEL) return "Sua Falna já está no nível máximo.";
        if (excelia < nextFalnaExcelia()) {
            return "Excelia insuficiente: " + excelia + "/" + nextFalnaExcelia() + ".";
        }
        if (level < nextFalnaPlayerLevel()) {
            return "Nível insuficiente: " + level + "/" + nextFalnaPlayerLevel() + ".";
        }
        return null;
    }

    /** Sobe a Falna se os requisitos forem cumpridos. */
    public boolean upgradeFalna() {
        if (falnaBlockReason() != null) return false;
        falnaLevel++;
        statPoints += POINTS_PER_FALNA_LEVEL;
        dirty = true;
        return true;
    }

    // ---------- Registros de descobertas ----------

    /** @return true se for a primeira vez. */
    public boolean markFirstKill(String entityId) {
        boolean added = firstKills.add(entityId);
        if (added) dirty = true;
        return added;
    }

    /** @return true se for a primeira vez. */
    public boolean markBiome(String biomeId) {
        boolean added = visitedBiomes.add(biomeId);
        if (added) dirty = true;
        return added;
    }

    // ---------- Fadiga (anti-farm) ----------

    /** Registra a ação e devolve o multiplicador de XP (1.0 = sem fadiga). */
    public double applyFatigue(XpSource source, double rawAmount) {
        double current = fatigue.getOrDefault(source, 0.0);
        double multiplier = 1.0 / (1.0 + current / source.softCap());
        fatigue.put(source, current + rawAmount);
        return multiplier;
    }

    public void decayFatigue(double seconds) {
        fatigue.replaceAll((source, value) -> Math.max(0, value - source.decayPerSecond() * seconds));
    }

    // ---------- Sincronização ----------

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean value) {
        dirty = value;
    }

    public void addPendingGain(double amount) {
        pendingGain += amount;
    }

    public double takePendingGain() {
        double value = pendingGain;
        pendingGain = 0;
        return value;
    }

    // ---------- Persistência ----------

    public CompoundTag save() {
        CompoundTag tag = toSyncTag();
        ListTag kills = new ListTag();
        firstKills.forEach(id -> kills.add(StringTag.valueOf(id)));
        tag.put("FirstKills", kills);
        ListTag biomes = new ListTag();
        visitedBiomes.forEach(id -> biomes.add(StringTag.valueOf(id)));
        tag.put("Biomes", biomes);
        return tag;
    }

    public void load(CompoundTag tag) {
        fromSyncTag(tag);
        firstKills.clear();
        for (Tag t : tag.getList("FirstKills", Tag.TAG_STRING)) firstKills.add(t.getAsString());
        visitedBiomes.clear();
        for (Tag t : tag.getList("Biomes", Tag.TAG_STRING)) visitedBiomes.add(t.getAsString());
    }

    /** Versão enxuta para enviar ao cliente (sem as listas de descobertas). */
    public CompoundTag toSyncTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Level", level);
        tag.putDouble("Xp", xp);
        tag.putInt("Points", statPoints);
        tag.putIntArray("Attributes", attributes);
        tag.putLong("Excelia", excelia);
        tag.putInt("Falna", falnaLevel);
        return tag;
    }

    public void fromSyncTag(CompoundTag tag) {
        level = Math.max(1, Math.min(MAX_LEVEL, tag.getInt("Level")));
        xp = tag.getDouble("Xp");
        statPoints = tag.getInt("Points");
        int[] saved = tag.getIntArray("Attributes");
        for (int i = 0; i < attributes.length; i++) attributes[i] = i < saved.length ? saved[i] : 0;
        excelia = tag.getLong("Excelia");
        falnaLevel = tag.getInt("Falna");
    }

    public void copyFrom(PlayerRpg other) {
        load(other.save());
        dirty = true;
    }

    public void reset() {
        load(new PlayerRpg().save());
        fatigue.clear();
        dirty = true;
    }
}
