package me.zort.containr.component.element;

import lombok.RequiredArgsConstructor;
import me.zort.containr.ContextClickInfo;
import me.zort.containr.Element;
import me.zort.containr.PagedContainer;
import me.zort.containr.internal.util.Items;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class PagingArrowElement extends Element {

    public static final String L_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWVkNzg4MjI1NzYzMTdiMDQ4ZWVhOTIyMjdjZDg1ZjdhZmNjNDQxNDhkY2I4MzI3MzNiYWNjYjhlYjU2ZmExIn19fQ==";
    public static final String R_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzE1NDQ1ZGExNmZhYjY3ZmNkODI3ZjcxYmFlOWMxZDJmOTBjNzNlYjJjMWJkMWVmOGQ4Mzk2Y2Q4ZTgifX19";

    private final AtomicReference<PagedContainer> containerReference;
    private final boolean left;
    private final String title;
    private final String texture;

    public PagingArrowElement(@NotNull PagedContainer container, boolean left, String title) {
        this(new AtomicReference<>(container), left, title, left ? L_TEXTURE : R_TEXTURE);
    }

    public PagingArrowElement(@NotNull AtomicReference<PagedContainer> containerRef, boolean left, String title) {
        this(containerRef, left, title, left ? L_TEXTURE : R_TEXTURE);
    }

    @Override
    public void click(ContextClickInfo info) {
        PagedContainer container = containerReference.get();
        if (container == null) return;
        if (left)
            container.previousPage();
        else
            container.nextPage();
        info.getGui().update(info.getPlayer());
    }

    @Override
    public @Nullable ItemStack item(Player player) {
        return Items.createSkull(title, texture);
    }
}
