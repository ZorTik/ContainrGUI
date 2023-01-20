package me.zort.containr;

import java.util.function.Consumer;

public interface ContainerBuilder<C extends Container> {

    ContainerBuilder<C> size(int width, int height);
    ContainerBuilder<C> init(Consumer<C> onInitFunction);
    C build();
    void set(Container target, int relativeIndex);

    static <T extends Container> ContainerBuilder<T> newBuilder(Class<T> typeClass) {
        if(typeClass == StaticContainer.class) {
            return (ContainerBuilder<T>) new StaticContainerBuilder();
        } else if(typeClass == PagedContainer.class) {
            return (ContainerBuilder<T>) new PagedContainerBuilder();
        } else {
            throw new IllegalArgumentException("Unsupported container type: " + typeClass.getName());
        }
    }

    class StaticContainerBuilder extends StaticContainer implements ContainerBuilder<StaticContainer> {

        private Consumer<StaticContainer> onInitFunction = c -> {};
        private int[] size = new int[] {1, 1};

        public StaticContainerBuilder() {
            super(1, 1);
        }

        @Override
        public ContainerBuilder<StaticContainer> size(int width, int height) {
            this.size = new int[] {width, height};
            return this;
        }

        @Override
        public void init() {
            onInitFunction.accept(this);
        }

        @Override
        public ContainerBuilder<StaticContainer> init(Consumer<StaticContainer> onInitFunction) {
            this.onInitFunction = onInitFunction;
            return this;
        }

        @Override
        public StaticContainer build() {
            changeSelection(size[0], size[1]);
            return this;
        }

        @Override
        public void set(Container target, int relativeIndex) {
            target.setContainer(build(), relativeIndex);
        }

    }

    class PagedContainerBuilder extends PagedContainer implements ContainerBuilder<PagedContainer> {

        private Consumer<PagedContainer> onInitFunction = c -> {};
        private int[] size = new int[] {1, 1};

        public PagedContainerBuilder() {
            super(1, 1);
        }

        @Override
        public ContainerBuilder<PagedContainer> size(int width, int height) {
            this.size = new int[] {width, height};
            return this;
        }

        @Override
        public void init() {
            onInitFunction.accept(this);
        }

        @Override
        public ContainerBuilder<PagedContainer> init(Consumer<PagedContainer> onInitFunction) {
            this.onInitFunction = onInitFunction;
            return this;
        }

        @Override
        public PagedContainer build() {
            changeSelection(size[0], size[1]);
            return this;
        }

        @Override
        public void set(Container target, int relativeIndex) {
            target.setContainer(build(), relativeIndex);
        }

    }

}
