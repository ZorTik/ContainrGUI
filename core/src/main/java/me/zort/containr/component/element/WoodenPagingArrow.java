package me.zort.containr.component.element;

import lombok.RequiredArgsConstructor;
import me.zort.containr.ContextClickInfo;
import me.zort.containr.Element;
import me.zort.containr.PagedContainer;
import me.zort.containr.internal.util.Items;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class WoodenPagingArrow extends Element {

    private static final String L_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWVkNzg4MjI1NzYzMTdiMDQ4ZWVhOTIyMjdjZDg1ZjdhZmNjNDQxNDhkY2I4MzI3MzNiYWNjYjhlYjU2ZmExIn19fQ==";
    private static final String R_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzE1NDQ1ZGExNmZhYjY3ZmNkODI3ZjcxYmFlOWMxZDJmOTBjNzNlYjJjMWJkMWVmOGQ4Mzk2Y2Q4ZTgifX19";

    private final PagedContainer container;
    private final boolean left;
    private final String title;

    @Override
    public void click(ContextClickInfo info) {
        if (left)
            container.previousPage();
        else
            container.nextPage();
    }

    @Override
    public @Nullable ItemStack item(Player player) {
        return Items.createSkull(title, left ? L_TEXTURE : R_TEXTURE);
    }
}
