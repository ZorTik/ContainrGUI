package me.zort.containr;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;
import lombok.Setter;
import me.zort.containr.util.Containers;
import me.zort.containr.util.NBT;
import me.zort.containr.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class GUI implements InventoryHolder, Iterable<Element> {

    private static final String ELEMENT_ID_KEY = "gui_element_id";

    private final Container container;
    private final Inventory inventory;

    @Setter
    private boolean frozen = false;
    private boolean initial = true;

    public GUI(String title, int rows) {
        Inventory inventory = Bukkit.createInventory(this, rows * 9, title);
        this.container = Containers.ofInv(inventory);
        this.inventory = inventory;
    }

    public GUI(InventoryType type, String title) {
        Inventory inventory = Bukkit.createInventory(this, type, title);
        this.container = Containers.ofInv(inventory);
        this.inventory = inventory;
    }

    // User Input
    public void build(Player p) {}

    public void open(Player p) {
        open(p, true);
    }

    public void open(Player p, boolean update) {
        if(!initial && this instanceof Rebuildable) {
            ((Rebuildable) this).rebuild();
        } else if(initial) build(p);
        GUIRepository.add(p.getName(), this);
        if(update) update(p);
        Bukkit.getScheduler().runTaskLater(Bukkit.getServer().getPluginManager().getPlugins()[0], () -> {
            if(p.isOnline()) {
                LinkedList<GUI> temp = new LinkedList<>(
                        GUIRepository.PREV_GUIS.containsKey(p.getName())
                                ? GUIRepository.PREV_GUIS.get(p.getName())
                                : new LinkedList<>()
                );
                p.openInventory(inventory);
                GUIRepository.PREV_GUIS.put(p.getName(), temp);
                initial = false;
            }
        }, 3L);
    }

    public void close(Player p) {
        p.closeInventory();
        GUIRepository.remove(p.getName());
    }

    public void update(Player p) {
        update(p, true);
    }

    public void update(Player p, Class<? extends Element>... clazz) {
        update(p, false, clazz);
    }

    public void update(Player p, boolean clear, Class<? extends Element>... clazz) {
        if(clear) inventory.clear();
        try {
            container.innerContainers().forEach(c -> c.refresh(p));
            Map<Integer, ?> content = container.content(clazz.length > 0 ? Arrays.stream(clazz).collect(Collectors.toList()) : null);
            content.keySet().forEach(slot -> {
                if(slot < inventory.getSize() && slot >= 0 && inventory.getItem(slot) != null) {
                    inventory.setItem(slot, null);
                }
            });
            for(int slot : content.keySet()) {
                Element element = (Element) content.get(slot);
                ItemStack item = element.item(p);
                if(item != null && !item.getType().equals(Material.AIR)) {
                    item = NBT.modifyNBT(item, nbtItem -> nbtItem.setString(ELEMENT_ID_KEY, element.getId()));
                }
                if(slot < inventory.getSize() && slot >= 0) {
                    inventory.setItem(slot, item);
                } else {
                    System.out.println("Cannot complete menu " + getClass().getSimpleName() + " for player " + p.getName() + " because index out of bounds: " + slot + " >= " + inventory.getSize());
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean invokeElement(Player p, ClickType clickType, ItemStack clickedItem) {
        if(clickedItem == null || clickedItem.getType().equals(Material.AIR)) return false;
        if(frozen) return false;
        NBTItem item = new NBTItem(clickedItem);
        String id = item.getString(ELEMENT_ID_KEY);
        if(id == null) return false;
        Optional<Pair<Container, Element>> elementOptional = container.findElementById(id);
        if(!elementOptional.isPresent()) return false;
        Pair<Container, Element> elementPair = elementOptional.get();
        Container container = elementPair.getKey();
        Element element = elementPair.getValue();
        element.action().accept(this, container, p, clickType);
        return true;
    }

    public Inventory getHandle() {
        return inventory;
    }

    @NotNull
    @Override
    public Iterator<Element> iterator() {
        return container.iterator();
    }

}
