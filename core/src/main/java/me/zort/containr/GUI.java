package me.zort.containr;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import me.zort.containr.builder.PatternGUIBuilder;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.zort.containr.factory.BasicInventoryFactory;
import me.zort.containr.factory.CustomInventoryFactory;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A GUI is a main entrance of a menu.
 * It collects contents from its main Container and renders them on
 * the final Minecraft GUI.
 *
 * @author ZorTik
 */
@Getter
public abstract class GUI extends ContainerHolder implements InventoryHolder, Cloneable {

    @NotNull
    public static PatternGUIBuilder ofPattern(@NotNull final Collection<String> pattern) {
        return ofPattern(pattern.toArray(new String[0]));
    }

    @NotNull
    public static PatternGUIBuilder ofPattern(@NotNull final String[] pattern) {
        return new PatternGUIBuilder("", pattern);
    }

    private static final String ELEMENT_ID_KEY = "gui_element_id";

    private final Container container;
    private final Inventory inventory;
    private final String title;

    @Setter
    private boolean frozen = false;
    private List<Integer> normalItemSlots;
    private boolean initial = true;
    @Getter(onMethod_ = @NotNull)
    private final List<NormalEditHandler> normalEditHandlers;
    @Getter(value = AccessLevel.PROTECTED, onMethod_ = @NotNull)
    private final Map<CloseReason, List<Consumer<Player>>> closeHandlers;

    public GUI(@NotNull final String title, final int rows) {
        this(new BasicInventoryFactory(title, rows));
    }

    @ApiStatus.ScheduledForRemoval
    @Deprecated
    public GUI(final InventoryType type, @NotNull final String title) {
        this(new CustomInventoryFactory(type, title));
    }

    public GUI(final InventoryFactory inventoryFactory) {
        InventoryInfo info = inventoryFactory.createInventory(this);
        this.container = Containers.ofInv(info.getInventory());
        this.inventory = info.getInventory();
        this.title = info.getTitle();
        this.normalEditHandlers = Collections.synchronizedList(new ArrayList<>());
        this.closeHandlers = Maps.newConcurrentMap();
        setNormalItemSlots();
    }

    // User Input
    @ApiStatus.OverrideOnly
    public abstract void build(Player p);

    /**
     * Adds handler that is invoked when normal item slot changes.
     * Example: Item is put on normal item slot.
     *
     * @param handler Handler to be invoked.
     */
    public final void onNormalEdit(@NotNull NormalEditHandler handler) {
        normalEditHandlers.add(handler);
    }

    public final void onClose(@NotNull CloseReason reason, @NotNull Consumer<Player> handler) {
        this.closeHandlers.computeIfAbsent(reason, k -> Collections.synchronizedList(new ArrayList<>())).add(handler);
    }

    public void open(@NotNull Player p) {
        open(p, true);
    }

    public void open(@NotNull Player p, boolean update) {
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
        close(p, CloseReason.BY_METHOD);
    }

    @ApiStatus.Internal
    public void close(Player p, CloseReason reason) {
        GUIRepository.remove(p.getName());
        p.closeInventory();

        List<Consumer<Player>> handlers = closeHandlers.get(reason);
        if(handlers == null)
            return;

        handlers.forEach(handler -> {
            try {
                handler.accept(p);
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void update(Player p) {
        update(p, true);
    }

    public void update(Player p, Class<? extends Element>... clazz) {
        update(p, false, clazz);
    }

    public void update(Player p, boolean clear, Class<? extends Element>... clazz) {
        if(clear) {
            clearInventory();
        }
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

    private void clearInventory() {
        clearInventory(true);
    }

    private void clearInventory(final boolean keepNormalItems) {
        Map<Integer, ItemStack> normalItems = normalItemSlots
                .stream()
                .filter(i -> inventory.getItem(i) != null)
                .collect(Collectors.toMap(i -> i, inventory::getItem));
        inventory.clear();
        if(keepNormalItems) {
            normalItems.forEach(inventory::setItem);
        }
    }

    public final boolean invokeElement(final Player p,
                                       final ClickType clickType,
                                       final ItemStack clickedItem) {
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
        element.click(new ContextClickInfo(this, container, element, p, clickType));
        return true;
    }

    public final void setNormalItemSlots(Integer... slots) {
        setNormalItemSlots(Arrays.stream(slots).collect(Collectors.toList()));
    }

    public final void setNormalItemSlots(List<Integer> normalItemSlots) {
        this.normalItemSlots = Collections.synchronizedList(normalItemSlots);
    }

    @NotNull
    public final List<Integer> getNormalItemSlots() {
        return new ArrayList<>(normalItemSlots);
    }

    @NotNull
    public final Map<Integer, ItemStack> getNormalItems() {
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

    /**
     * @deprecated Use {@link #getInventory()}
     */
    @Deprecated
    public final @NotNull Inventory getHandle() {
        return inventory;
    }

    public final @NotNull Inventory getInventory() {
        return inventory;
    }

    @SneakyThrows
    @Override
    public GUI clone() {
        return (GUI) super.clone();
    }

    public interface NormalEditHandler {
        void onEdit(Player player, int slot);
    }

    public enum CloseReason {
        BY_METHOD,
        BY_PLAYER
    }

}
