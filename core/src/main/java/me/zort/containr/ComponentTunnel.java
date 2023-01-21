package me.zort.containr;

public interface ComponentTunnel {

    void send(ContainerComponent container);
    void send(Element element);
    String getId();

    default void send(Component component) {
        if(component instanceof Element) {
            send((Element) component);
        } else if(component instanceof ContainerComponent) {
            send((ContainerComponent) component);
        }
    }

}
