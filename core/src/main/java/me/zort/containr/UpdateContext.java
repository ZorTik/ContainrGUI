package me.zort.containr;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@Getter
public class UpdateContext {
    private final GUI gui;
    private final Player player;

    public void repeat() {
        gui.update(player);
    }
}
