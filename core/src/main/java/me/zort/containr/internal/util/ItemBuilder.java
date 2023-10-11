package me.zort.containr.internal.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@AllArgsConstructor
@Getter
public final class ItemBuilder {

    public static @NotNull ItemBuilder fromConfig(ConfigurationSection section) {
        Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        if(section.contains("enchantments")) {
            section.getConfigurationSection("enchantments").getValues(false).forEach((k, v) -> {
                enchantments.put(Enchantment.getByName(k), (int) v);
            });
        }
        return newBuilder()
                .withType(Material.valueOf(section.getString("type")))
                .withData((short) section.getInt("data", 0))
                .withAmount(section.getInt("amount", 1))
                .withName(colorize(section.getString("name", "")))
                .withLore(colorize(section.contains("lore") ? section.getStringList("lore") : Lists.newArrayList()))
                .withEnchantments(enchantments)
                .withCustomModelData(section.getInt("custom_model_data"));
    }

    private static String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private static List<String> colorize(List<String> s) {
        List<String> colored = new ArrayList<>();
        s.forEach(line -> colored.add(colorize(line)));
        return colored;
    }

    public static @NotNull ItemBuilder newBuilder() {
        return new ItemBuilder();
    }

    public static @NotNull ItemBuilder newBuilder(Material material) {
        return newBuilder(material, (short) 0);
    }

    public static @NotNull ItemBuilder newBuilder(Material material, short data) {
        ItemBuilder itemBuilder = new ItemBuilder();
        return itemBuilder
                .withType(material)
                .withData(data);
    }

    public static @NotNull ItemBuilder newBuilder(ItemStack origin) {
        List<String> lore = origin.getItemMeta().getLore() != null ? origin.getItemMeta().getLore() : new ArrayList<>();
        return new ItemBuilder()
                .withType(origin.getType())
                .withData(origin.getData().getData())
                .withAmount(origin.getAmount())
                .withName(origin.getItemMeta().getDisplayName())
                .withLore()
                .lines(lore)
                .and()
                .withEnchantments(origin.getEnchantments())
                .withCustomModelData(origin.getItemMeta().hasCustomModelData()
                        ? origin.getItemMeta().getCustomModelData()
                        : -1);
    }

    private Material type;
    private short data;
    private int amount;
    private String name;
    private List<String> lore;
    private Map<Enchantment, Integer> enchantments;
    private int customModelData;
    private final List<Consumer<ItemStack>> modifiers;

    public ItemBuilder() {
        this.type = Material.AIR;
        this.data = 0;
        this.amount = 1;
        this.name = "";
        this.lore = Lists.newArrayList();
        this.enchantments = Maps.newHashMap();
        this.customModelData = -1;
        this.modifiers = new ArrayList<>();
    }

    public ItemBuilder withType(Material type) {
        this.type = type;
        return this;
    }

    public ItemBuilder withData(short data) {
        this.data = data;
        return this;
    }

    public ItemBuilder withAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public LoreBuilder withLore() {
        return new LoreBuilder(this, lore);
    }

    public ItemBuilder withLore(List<String> lines) {
        this.lore = lines;
        return this;
    }

    public ItemBuilder withEnchantments(Map<Enchantment, Integer> enchantments) {
        this.enchantments = enchantments;
        return this;
    }

    public ItemBuilder withEnchantment(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public ItemBuilder withCustomModelData(int customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    public ItemBuilder withBuildModifier(Consumer<ItemStack> modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    public ItemBuilder enchanted() {
        return withEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
    }

    public ItemBuilder appendLore(String... lines) {
        return appendLore(Lists.newArrayList(lines));
    }

    public ItemBuilder appendLore(List<String> lines) {
        lore.addAll(lines);
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(type, amount, data);
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            enchantments.forEach((enchant, level) -> meta.addEnchant(enchant, level, true));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_UNBREAKABLE,
                    ItemFlag.HIDE_DESTROYS,
                    ItemFlag.HIDE_PLACED_ON,
                    ItemFlag.HIDE_POTION_EFFECTS);
            if (customModelData != -1) {
                meta.setCustomModelData(customModelData);
            }
            item.setItemMeta(meta);
        }
        modifiers.forEach(modifier -> modifier.accept(item));
        return item;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LoreBuilder {

        private final ItemBuilder itemBuilder;
        private final List<String> lines;

        public LoreBuilder lines(String... lines) {
            return lines(Lists.newArrayList(lines));
        }

        public LoreBuilder lines(List<String> lines) {
            this.lines.addAll(lines);
            return this;
        }

        public LoreBuilder line(String line) {
            lines.add(line);
            return this;
        }

        public LoreBuilder blank() {
            return line("");
        }

        public ItemBuilder and() {
            itemBuilder.withLore(lines);
            return itemBuilder;
        }

    }

}
