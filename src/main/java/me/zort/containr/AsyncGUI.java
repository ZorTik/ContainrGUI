package me.zort.containr;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public abstract class AsyncGUI extends GUI {

    private final ExecutorService executorService;

    public AsyncGUI(String title, int rows, ExecutorService executorService) {
        super(title, rows);
        this.executorService = executorService;
    }

    @Override
    public void update(Player p, boolean clear, @Nullable Class<? extends Element>... clazz) {
        CompletableFuture.runAsync(() -> super.update(p, clear, clazz), executorService);
    }

}
