package me.zort.containr.component.gui;

import me.zort.containr.Element;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Deprecated
public abstract class AsyncAnimatedGUI extends AnimatedGUI {

    private final ExecutorService executorService;

    public AsyncAnimatedGUI(String title, int rows, int period, TimeUnit unit, ExecutorService executorService) {
        super(title, rows, period, unit);
        this.executorService = executorService;
    }

    @Override
    public void update(Player p, boolean clear, @Nullable Class<? extends Element>... clazz) {
        CompletableFuture.runAsync(() -> super.update(p, clear, clazz), executorService);
    }

}
