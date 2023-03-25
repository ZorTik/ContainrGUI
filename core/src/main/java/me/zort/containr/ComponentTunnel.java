package me.zort.containr;

/**
 * A {@link ComponentTunnel} is used primarly as bridge
 * between {@link ComponentSource} and it's destination.
 *
 * @author ZorTik
 */
public interface ComponentTunnel {

    void send(ContainerComponent container);
    void send(Element element);
    void clear();
    String getId();

    default void send(Component component) {
        if(component instanceof Element) {
            send((Element) component);
        } else if(component instanceof ContainerComponent) {
            send((ContainerComponent) component);
        }
    }

}
