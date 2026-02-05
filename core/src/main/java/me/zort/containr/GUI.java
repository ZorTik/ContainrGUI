package me.zort.containr;

import com.google.common.collect.Maps;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.zort.containr.builder.PatternGUIBuilder;
import me.zort.containr.factory.BasicInventoryFactory;
import me.zort.containr.internal.util.Containers;
import me.zort.containr.internal.util.NBT;
import me.zort.containr.internal.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.zort.containr.util.Util.runCatching;

/**
 * A GUI is a main entrance of a menu.
 * It collects contents from its main Container and renders them on
 * the final Minecraft GUI.
 *
 * @author ZorTik
 */
@ApiStatus.AvailableSince("0.1")
@Getter
public abstract class GUI extends ContainerHolder implements InventoryHolder, Cloneable {

    public static @NotNull PatternGUIBuilder ofPattern(@NotNull final Collection<String> pattern) {
        return ofPattern(pattern.toArray(new String[0]));
    }

    public static @NotNull PatternGUIBuilder ofPattern(@NotNull final String[] pattern) {
        return new PatternGUIBuilder("", pattern);
    }

    private final Container container;
    @Getter(onMethod_ = @NotNull)
    private final List<NormalEditHandler> normalEditHandlers;
    @Getter(value = AccessLevel.PROTECTED, onMethod_ = @NotNull)
    private final Map<CloseReason, List<Consumer<Player>>> closeHandlers;
    private Inventory inventory;
    private String title;

    @Setter private boolean frozen = false;
    private boolean initial = true;
    private List<Integer> normalItemSlots;

    public GUI(final @NotNull String title, final int rows) {
        this(new BasicInventoryFactory(title, rows));
    }

    public GUI(final @NotNull InventoryFactory inventoryFactory) {
        InventoryInfo info = inventoryFactory.createInventory(this);
        this.container = Containers.ofInv(info.getInventory());
        this.inventory = info.getInventory();
        this.title = info.getTitle();
        this.normalEditHandlers = Collections.synchronizedList(new ArrayList<>());
        this.closeHandlers = Maps.newConcurrentMap();
        setNormalItemSlots();
    }

    // User should implement a gui content filling logic here.
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

    public final void setNormalItemSlots(Integer... slots) {
        setNormalItemSlots(Arrays.stream(slots).collect(Collectors.toList()));
    }

    public final void setNormalItemSlots(List<Integer> normalItemSlots) {
        this.normalItemSlots = Collections.synchronizedList(normalItemSlots);
    }

    public final @NotNull List<Integer> getNormalItemSlots() {
        return new ArrayList<>(normalItemSlots);
    }

    public void open(@NotNull Player p) {
        open(p, true);
    }

    public void open(@NotNull Player p, boolean update) {
        reopen(p, inventory, update, true);
    }

    public void reopen(@NotNull Player p,
                       @NotNull Inventory inventory) {
        reopen(p, inventory, true, true);
    }

    public void reopen(@NotNull Player p,
                       @NotNull Inventory inventory,
                       boolean update,
                       boolean emitEvents) {
        this.inventory = inventory;
        if(!initial && this instanceof Rebuildable) {
            ((Rebuildable) this).rebuild();
            ((Rebuildable) this).rebuild(p);
        } else if(initial) build(p);
        GUIRepository.add(p.getName(), this);

        if(initial) {
            // Init loop
            initializeModel(p);
        }

        if(update) update(p);

        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugins()[0], () -> {
            if(p.isOnline()) {
                LinkedList<GUI> temp = new LinkedList<>(
                        GUIRepository.PREV_GUIS.containsKey(p.getName())
                                ? GUIRepository.PREV_GUIS.get(p.getName())
                                : new LinkedList<>()
                );
                p.openInventory(inventory);
                GUIRepository.PREV_GUIS.put(p.getName(), temp);
                initial = false;
                if (emitEvents) {
                    getContainer().emitEventRecursively(new Container.Event.OpenEvent(this, p));
                }
            }
        }, 3L);
    }

    private void initializeModel(Player p) {
        updateContextInfo(p);
        List<Container> initList = new ArrayList<>();
        while(true) {
            // Init containers using lowering waves.
            final int initSizeBef = initList.size();
            for (Container inner : container.innerContainers()) {
                if (initList.contains(inner)) {
                    continue;
                }
                inner.doInit();
                initList.add(inner);
            }
            if(initList.size() == initSizeBef) {
                // No more containers initialized, breaking
                // the init loop.
                break;
            }
        }
    }

    private void updateContextInfo(Player p) {
        container.setLastUpdateContext(new UpdateContext(this, p));
    }

    public void close(Player p) {
        close(p, CloseReason.BY_METHOD);
    }

    void close(Player p, CloseReason reason) {
        GUIRepository.remove(p.getName());
        p.closeInventory();

        List<Consumer<Player>> handlers = closeHandlers.get(reason);
        if(handlers == null) return;

        handlers.forEach(handler -> runCatching(() -> handler.accept(p)));
    }

    public void update(@NotNull Player p) {
        update(p, true);
    }

    public void update(@NotNull Player p, Class<? extends Element>... clazz) {
        update(p, false, clazz);
    }

    public void update(@NotNull Player p, boolean clear, Class<? extends Element>... clazz) {
        if(clear) clearInventory();

        Objects.requireNonNull(p, "Player cannot be null");

        try {
            initializeModel(p);
            container.innerContainers().forEach(c -> c.refresh(p));
            Map<Integer, ?> content = container.content(clazz.length > 0 ? Arrays.asList(clazz) : null);
            content.keySet().forEach(slot -> {
                if(slot < inventory.getSize() && slot >= 0 && inventory.getItem(slot) != null) inventory.setItem(slot, null);
            });
            for(int slot : content.keySet()) {
                Element element = (Element) content.get(slot);
                ItemStack item = element.item(p);
                if(item != null && !item.getType().equals(Material.AIR)) {
                    item = NBT.modifyNBT(item, nbtItem -> nbtItem.setString(Constants.ELEMENT_ID_KEY, element.getId()));
                }
                if(slot < inventory.getSize() && slot >= 0) {
                    item = element.postProcessItem(p, item);
                    inventory.setItem(slot, item);
                } else {
                    String err = "Cannot complete menu %s for player %s because index out of bounds: %d >= %d";
                    throw new IndexOutOfBoundsException(String.format(err, getClass().getSimpleName(), p.getName(), slot, inventory.getSize()));
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearInventory() {
        clearInventory(true);
    }

    private void clearInventory(boolean keepNormalItems) {
        Map<Integer, ItemStack> normalItems = normalItemSlots.stream()
                .filter(i -> inventory.getItem(i) != null)
                .collect(Collectors.toMap(i -> i, inventory::getItem));

        inventory.clear();

        if(keepNormalItems) normalItems.forEach(inventory::setItem);
    }

    protected final void invokeElement(final @NotNull Player p, final @NotNull ClickType clickType,
                                       final @NotNull ItemStack clickedItem, final @Nullable ItemStack cursorItem) {
        requireNonNull(p, clickType, clickedItem);

        String id = new NBTItem(clickedItem).getString(Constants.ELEMENT_ID_KEY);
        Pair<Container, Element> elementPair = container.findElementById(id).orElse(null);
        if(elementPair == null) return;
        Element element = elementPair.getValue();
        element.click(new ContextClickInfo(this, elementPair.getKey(), element, p, clickType, cursorItem));
    }

    public final @NotNull Map<Integer, ItemStack> getNormalItems() {
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
    @ApiStatus.ScheduledForRemoval
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

    private static void requireNonNull(Object... objects) {
        for(Object object : objects) {
            Objects.requireNonNull(object);
        }
    }

    public interface NormalEditHandler {
        void onEdit(Player player, int slot);
    }

    public enum CloseReason {
        BY_METHOD,
        BY_PLAYER
    }

}
