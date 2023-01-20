package me.zort.containr.source;

import me.zort.containr.*;
import me.zort.containr.exception.InvalidComponentException;
import reactor.core.publisher.Sinks;

import static reactor.core.publisher.Sinks.many;

public class SinkComponentSource implements ComponentSource {

    private final Sinks.Many<Component> attachedSink;
    private Sinks.EmitFailureHandler emitFailureHandler;
    private boolean completed;

    public <T> SinkComponentSource(Sinks.Many<T> diffSink, ComponentMapper<T> mapper) {
        this(many().multicast().directBestEffort());
        this.emitFailureHandler = Sinks.EmitFailureHandler.FAIL_FAST;
        diffSink.asFlux().subscribe(anyObject -> {
            attachedSink.emitNext(mapper.map(anyObject), emitFailureHandler);
        });
    }

    public SinkComponentSource(Sinks.Many<Component> sink) {
        this.attachedSink = sink;
        this.completed = false;
    }

    public void setEmitFailureHandler(Sinks.EmitFailureHandler emitFailureHandler) {
        this.emitFailureHandler = emitFailureHandler;
    }

    @Override
    public boolean enable(ComponentTunnel tunnel) {
        if(completed)
            return false;

        attachedSink.asFlux().subscribe((component) -> {
            if (component instanceof Element) {
                tunnel.send((Element) component);
            } else if(component instanceof ContainerComponent) {
                tunnel.send((ContainerComponent) component);
            } else {
                throw new InvalidComponentException(component);
            }
        });
        return true;
    }

    @Override
    public void disable() {
        if(!completed && attachedSink.tryEmitComplete().isSuccess())
            completed = true;
    }

    public interface ComponentMapper<T> {
        Component map(T anyObject);
    }
}
