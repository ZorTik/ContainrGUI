package me.zort.containr;

import com.google.common.collect.Maps;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.zort.containr.internal.util.Containers;
import me.zort.containr.internal.util.NBT;
import me.zort.containr.internal.util.Pair;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
public class GUI implements InventoryHolder, Iterable<Element> {

    private static final String ELEMENT_ID_KEY = "gui_element_id";

    private final Container container;
    private final Inventory inventory;

    @Setter
    private boolean frozen = false;
    private List<Integer> normalItemSlots;
    private boolean initial = true;
    @Getter
    private final List<NormalEditHandler> normalEditHandlers;
    @Getter(AccessLevel.PROTECTED)
    private final Map<CloseReason, List<Consumer<Player>>> closeHandlers;

    public GUI(String title, int rows) {
        Inventory inventory = Bukkit.createInventory(this, rows * 9, title);
        this.container = Containers.ofInv(inventory);
        this.inventory = inventory;
        this.normalEditHandlers = Collections.synchronizedList(new ArrayList<>());
        this.closeHandlers = Maps.newConcurrentMap();
        setNormalItemSlots();
    }

    public GUI(InventoryType type, String title) {
        Inventory inventory = Bukkit.createInventory(this, type, title);
        this.container = Containers.ofInv(inventory);
        this.inventory = inventory;
        this.normalEditHandlers = Collections.synchronizedList(new ArrayList<>());
        this.closeHandlers = Maps.newConcurrentMap();
        setNormalItemSlots();
    }

    // User Input
    public void build(Player p) {}

    /**
     * Adds handler that is invoked when normal item slot changes.
     * Example: Item is put on normal item slot.
     *
     * @param handler Handler to be invoked.
     */
    public void onNormalEdit(NormalEditHandler handler) {
        normalEditHandlers.add(handler);
    }

    public void onClose(CloseReason reason, Consumer<Player> handler) {
        this.closeHandlers.computeIfAbsent(reason, k -> Collections.synchronizedList(new ArrayList<>())).add(handler);
    }

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
        GUIRepository.remove(p.getName());
        p.closeInventory();
        handleClose(p, CloseReason.BY_METHOD);
    }

    public void handleClose(Player p, CloseReason reason) {
        if(closeHandlers.containsKey(reason)) {
            closeHandlers.get(reason).forEach(handler -> {
                try {
                    handler.accept(p);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            });
        }
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

    public void setNormalItemSlots(Integer... slots) {
        setNormalItemSlots(Arrays.stream(slots).collect(Collectors.toList()));
    }

    public void setNormalItemSlots(List<Integer> normalItemSlots) {
        this.normalItemSlots = Collections.synchronizedList(normalItemSlots);
    }

    public List<Integer> getNormalItemSlots() {
        return new ArrayList<>(normalItemSlots);
    }

    public Map<Integer, ItemStack> getNormalItems() {
        Map<Integer, Element> content = container.content((List<Class<? extends Element>>) null);
        Map<Integer, ItemStack> items = Maps.newHashMap();
        for(int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if(item != null && !item.getType().equals(Material.AIR) && !content.containsKey(i)) {
                items.put(i, item);
            }
        }
        return items;
    }

    public Inventory getHandle() {
        return inventory;
    }

    @NotNull
    @Override
    public Iterator<Element> iterator() {
        return container.iterator();
    }

    public interface NormalEditHandler {
        void onEdit(Player player, int slot);
    }

    public enum CloseReason {
        BY_METHOD,
        BY_PLAYER
    }

}
