package me.zort.containr.internal.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class ItemBuilder {

    public static ItemBuilder newBuilder() {
        return new ItemBuilder();
    }

    public static ItemBuilder newBuilder(Material material) {
        return newBuilder(material, (short) 0);
    }

    public static ItemBuilder newBuilder(Material material, short data) {
        ItemBuilder itemBuilder = new ItemBuilder();
        return itemBuilder
                .withType(material)
                .withData(data);
    }

    public static ItemBuilder newBuilder(ItemStack origin) {
        List<String> lore = origin.getItemMeta().getLore() != null ? origin.getItemMeta().getLore() : new ArrayList<>();
        return new ItemBuilder()
                .withType(origin.getType())
                .withData(origin.getData().getData())
                .withAmount(origin.getAmount())
                .withName(origin.getItemMeta().getDisplayName())
                .withLore()
                .lines(lore)
                .and()
                .withEnchantments(origin.getEnchantments());
    }

    private Material type;
    private short data;
    private int amount;
    private String name;
    private List<String> lore;
    private Map<Enchantment, Integer> enchantments;

    public ItemBuilder() {
        this.type = Material.AIR;
        this.data = 0;
        this.amount = 1;
        this.name = "";
        this.lore = Lists.newArrayList();
        this.enchantments = Maps.newHashMap();
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
            item.setItemMeta(meta);
        }
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
