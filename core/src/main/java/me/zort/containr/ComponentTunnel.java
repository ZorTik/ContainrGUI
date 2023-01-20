package me.zort.containr;

public interface ComponentTunnel {

    void send(ContainerComponent container);
    void send(Element element);

}
