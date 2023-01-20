package me.zort.containr;

import com.google.common.collect.Lists;
import lombok.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class GUITutorial<T extends GUI> {

    public static class GUITutorialBuilder<T extends GUI> {

        private final T gui;
        private Consumer<Player> aSyncPreAction = p -> {};
        private final List<Part<T>> parts;
        private Consumer<Player> aSyncPostAction = p -> {};

        public GUITutorialBuilder(T gui) {
            this.parts = Lists.newLinkedList();
            this.gui = gui;
        }

        public GUITutorialBuilder<T> onStart(Consumer<Player> consumer) {
            this.aSyncPreAction = consumer;
            return this;
        }

        @SafeVarargs
        public final GUITutorialBuilder<T> parts(Part<T>... parts) {
            parts(Arrays.asList(parts));
            return this;
        }

        public GUITutorialBuilder<T> parts(@NotNull Collection<Part<T>> parts) {
            this.parts.addAll(parts);
            return this;
        }

        public GUITutorialBuilder<T> part(@NotNull Part<T> part) {
            this.parts.add(part);
            return this;
        }

        public GUITutorialBuilder<T> onEnd(@NotNull Consumer<Player> consumer) {
            this.aSyncPostAction = consumer;
            return this;
        }

        public GUITutorial<T> build() {
            return new GUITutorial<>(gui, aSyncPreAction, parts, aSyncPostAction);
        }

    }

    private final T gui;
    private final transient Consumer<Player> aSyncPreAction;
    private final transient List<Part<T>> parts;
    private final transient Consumer<Player> aSyncPostAction;

    public void run(Player p, Executor executor) {
        gui.setFrozen(true);
        Iterator<Part<T>> it = parts.iterator();
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> aSyncPreAction.accept(p), executor);
        while(it.hasNext()) {
            Part<T> next = it.next();
            future = future.whenComplete((o, ex) -> {
                if(!p.isOnline()) return;
                Object target = next.target.apply(gui);
                long uptime = 0L;
                boolean marked = false;
                while(uptime < next.time) {
                    if(marked) {
                        next.mark.accept(target);
                    }
                    marked = !marked;
                    try {
                        Thread.sleep(next.interval);
                    } catch (InterruptedException e) {}
                    uptime += next.interval;
                }
                if(ex != null) {
                    ex.printStackTrace();
                }
            });
            if(!it.hasNext()) {
                future = future.whenComplete((o, ex) -> {
                    if(!p.isOnline()) return;
                    aSyncPostAction.accept(p);
                    gui.setFrozen(false);
                });
            }
        }
    }

    @AllArgsConstructor
    public static class Part<T extends GUI> {

        private final String title;
        private final long interval;
        private final long time;

        private Function<T, Object> target;
        private Consumer<Object> mark;
        private Consumer<Object> unMark;

    }

}
