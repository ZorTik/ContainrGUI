package me.zort.containr;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class ToggleableElement extends Element {

    @Getter
    private boolean enabled = false;

    @Nullable
    public abstract ItemStack enabled();
    @Nullable
    public abstract ItemStack disabled();

    public void toggle() {
        toggle(!enabled);
    }

    public void toggle(boolean enabled) {
        this.enabled = enabled;
    }

    @Nullable
    @Override
    public ItemStack item(Player player) {
        return enabled ? enabled() : disabled();
    }

}
