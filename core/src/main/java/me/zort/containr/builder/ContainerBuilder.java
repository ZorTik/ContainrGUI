package me.zort.containr.builder;

import lombok.RequiredArgsConstructor;
import me.zort.containr.Container;
import me.zort.containr.PagedContainer;
import me.zort.containr.StaticContainer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface ContainerBuilder<C extends Container> {

    ContainerBuilder<C> size(int width, int height);
    ContainerBuilder<C> init(Consumer<C> onInitFunction);
    C build();
    void set(Container target, int relativeIndex);

    @SuppressWarnings("unchecked")
    static <T extends Container> ContainerBuilder<T> newBuilder(Class<T> typeClass) {
        BuilderContainerFactory<T> factory;
        if(typeClass == StaticContainer.class) {
            factory = (init, x, y) -> (T) new StaticContainer(x, y) {
                @Override
                public void init() {init.accept((T) this);}
            };
        } else if(typeClass == PagedContainer.class) {
            factory = (init, x, y) -> (T) new PagedContainer(x, y) {
                @Override
                public void init() {init.accept((T) this);}
            };
        } else {
            throw new IllegalArgumentException("Unsupported container type: " + typeClass.getName());
        }
        return new BasicContainerBuilder<>(factory);
    }

    @RequiredArgsConstructor
    class BasicContainerBuilder<T extends Container> implements ContainerBuilder<T> {
        private final BuilderContainerFactory<T> factory;
        private Consumer<T> onInitFunction = c -> {};
        private int[] size = new int[] {1, 1};

        @Override
        public final ContainerBuilder<T> size(int width, int height) {
            this.size = new int[] {width, height};
            return this;
        }

        @Override
        public final ContainerBuilder<T> init(@NotNull Consumer<T> onInitFunction) {
            this.onInitFunction = onInitFunction;
            return this;
        }

        @Override
        public final void set(@NotNull Container target, int relativeIndex) {
            onInitFunction = onInitFunction.andThen(c -> c.setContainer(relativeIndex, c));
        }

        @Override
        public T build() {
            return factory.create(onInitFunction, size[0], size[1]);
        }
    }

    interface BuilderContainerFactory<T extends Container> {
        T create(Consumer<T> onInitFunction, int width, int height);
    }

}
