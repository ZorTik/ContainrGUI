package me.zort.containr.evt;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EvtBus<T> {

    private final Map<UUID, ListenerWrapper<T>> entries = new ConcurrentHashMap<>();

    public <E extends T> UUID on(Class<E> evt, EvtListener<E> listener) {
        UUID uuid = UUID.randomUUID();
        entries.put(uuid, new ListenerWrapper<>(evt, listener));
        return uuid;
    }

    public boolean off(UUID uuid) {
        return entries.remove(uuid) != null;
    }

    public void emit(T eventObject) {
        for (ListenerWrapper<T> entry : entries.values()) {
            entry.onEvent(eventObject);
        }
    }

    public interface EvtListener<I> {
        void onEvent(I event);
    }

    @SuppressWarnings("unchecked, rawtypes")
    @RequiredArgsConstructor
    private static final class ListenerWrapper<I> implements EvtListener<I> {

        private final Class<? extends I> evtTypeSuper;
        private final EvtListener thatListener;

        @Override
        public void onEvent(I event) {
            if(evtTypeSuper.isAssignableFrom(event.getClass())) {
                thatListener.onEvent(event);
            }
        }
    }

}
