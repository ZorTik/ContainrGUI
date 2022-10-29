package me.zort.containr.internal.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public final class Items {

    public static ItemStack create(Material mat, int count, short data, Map<Enchantment, Integer> enchantments, String title, String... lore) {
        ItemStack stack = new ItemStack(mat, count, data);
        ItemMeta meta = stack.getItemMeta();
        if(meta != null) {
            if(!enchantments.isEmpty()) {
                enchantments.forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));
            }
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
            if(lore.length > 0) {
                List<String> loreList = Arrays.stream(lore)
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());
                meta.setLore(loreList);
            }
            meta.addItemFlags(ItemFlag.values());
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static ItemStack create(Material mat, String title, String... lore) {
        return create(mat, 1, title, lore);
    }

    public static ItemStack create(Material mat, int count, String title, String... lore) {
        return create(mat, count, (short) 0, title, lore);
    }

    public static ItemStack create(Material mat, int count, short data, String title, String... lore) {
        return create(mat, count, data, Maps.newHashMap(), title, lore);
    }

    public static ItemStack createBook(int count, List<String> pages, String... lore) {
        String randomString = RandomStringUtils.randomAlphabetic(8);
        return createBook(count, randomString, randomString, randomString, pages, lore);
    }

    public static ItemStack createBook(int count, String bookTitle, List<String> pages, String... lore) {
        String randomString = RandomStringUtils.randomAlphabetic(8);
        return createBook(count, randomString, bookTitle, randomString, pages, lore);
    }

    public static ItemStack createBook(int count, String title, String bookTitle, List<String> pages, String... lore) {
        return createBook(count, title, bookTitle, RandomStringUtils.randomAlphabetic(8), pages, lore);
    }

    public static ItemStack createBook(int count, String title, String bookTitle, String author, List<String> pages, String... lore) {
        ItemStack stack = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta meta = (BookMeta) stack.getItemMeta();
        meta.setDisplayName(title);
        meta.setTitle(bookTitle);
        meta.setAuthor(author);
        meta.setPages(pages);
        meta.setLore(Arrays.stream(lore).collect(Collectors.toList()));
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack createSkull(String title, String value) {
        return createSkull(title, Lists.newArrayList(), value);
    }

    public static ItemStack createSkull(String title, List<String> lore, String value) {
        return createSkull(1, title, lore, value);
    }

    public static ItemStack createSkull(int count, String title, List<String> lore, String value) {
        Material mat = Material.matchMaterial("SKULL_ITEM");
        ItemStack head;
        if(mat != null) {
            head = new ItemStack(mat, count, (short) 3);
        } else {
            mat = Material.matchMaterial("PLAYER_HEAD");
            head = new ItemStack(mat);
        }
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(title);
        meta.setLore(lore);
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        PropertyMap properties = profile.getProperties();
        properties.put("textures", new Property("textures", value));
        Field profileField;
        try {
            Class<? extends SkullMeta> metaClass = meta.getClass();
            profileField = metaClass.getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        head.setItemMeta(meta);
        return head;
    }

    public static ItemStack fromConfig(ConfigurationSection section) {
        return fromConfig(section, Maps.newHashMap());
    }

    public static ItemStack fromConfig(ConfigurationSection section, Map<String, String> replacements) {
        Material material = null;
        if(section.contains("type")) {
            material = Material.getMaterial(section.getString("type"));
        }
        if(material == null) {
            material = Material.AIR;
        }
        int amount = 1;
        if(section.contains("amount")) {
            amount = section.getInt("amount");
        }
        String name = null;
        if(section.contains("name")) {
            name = section.getString("name");
        }
        short data = 0;
        if(section.contains("data")) {
            data = (short) section.getInt("data");
        }
        List<String> lore = new ArrayList<>();
        if(section.contains("lore")) {
            lore = section.getStringList("lore");
        }
        Map<Integer, Enchantment> enchantments = new HashMap<>();
        if(section.contains("enchantments") && section.isConfigurationSection("enchantments")) {
            section.getConfigurationSection("enchantments").getKeys(false).forEach(key -> {
                Enchantment enchantment = Enchantment.getByName(key);
                if(enchantment != null) {
                    enchantments.put(section.getInt("enchantments." + key), enchantment);
                }
            });
        }
        ItemStack item = new ItemStack(material, amount, data);
        ItemMeta meta = item.getItemMeta();
        if(name != null) {
            meta.setDisplayName(colorize(repl(name, replacements)));
        }
        if(lore.size() > 0) {
            meta.setLore(colorize(repl(lore, replacements)));
        }
        if(enchantments.size() > 0) {
            enchantments.forEach((level, enchantment) -> meta.addEnchant(enchantment, level, true));
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack blank(Material mat) {
        return blank(mat, 1);
    }

    public static ItemStack blank(Material mat, int count) {
        return blank(mat, count, (short) 0);
    }

    public static ItemStack blank(Material mat, int count, short data) {
        return new ItemStack(mat, count, data);
    }

    public static ItemStack flags(ItemStack stack, ItemFlag... flags) {
        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(flags);
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack addToLore(ItemStack stack, String... addon) {
        return addToLore(stack, Arrays.stream(addon).collect(Collectors.toList()));
    }

    public static ItemStack changeLore(ItemStack stack, List<String> lore) {
        ItemStack clone = stack.clone();
        ItemMeta meta = clone.getItemMeta();
        meta.setLore(lore);
        clone.setItemMeta(meta);
        return clone;
    }

    public static ItemStack addToLore(ItemStack stack, List<String> addon) {
        ItemStack stackCloned = stack.clone();
        ItemMeta meta = stackCloned.getItemMeta();
        List<String> lore = meta.getLore();
        if (addon == null) {
            return stack;
        } else if(lore == null) {
            lore = Lists.newArrayList();
        }
        lore.addAll(addon);
        meta.setLore(lore);
        stackCloned.setItemMeta(meta);
        return stackCloned;
    }

    public static ItemStack removeFromLore(ItemStack stack, int amount) {
        if(amount < 0) amount = 0;
        ItemStack stackCloned = stack.clone();
        ItemMeta meta = stackCloned.getItemMeta();
        final List<String> lore = meta.getLore();
        for(int i = 0; i < amount; i++) {
            if(lore.isEmpty()) break;
            lore.remove(lore.size() - 1);
        }
        meta.setLore(lore);
        stackCloned.setItemMeta(meta);
        return stackCloned;
    }

    private static List<String> colorize(Iterable<String> collection) {
        List<String> list = Lists.newArrayList();
        for(String s : collection) {
            list.add(colorize(s));
        }
        return list;
    }

    private static String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    private static List<String> repl(Iterable<String> collection, Map<String, String> replacements) {
        List<String> list = Lists.newArrayList(collection);
        for(int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if(s != null) {
                s = repl(s, replacements);
            }
            list.add(s);
        }
        return list;
    }

    private static String repl(String string, Map<String, String> replacements) {
        for(Map.Entry<String, String> entry : replacements.entrySet()) {
            string = string.replace(entry.getKey(), entry.getValue());
        }
        return string;
    }

}
