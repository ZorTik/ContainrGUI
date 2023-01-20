package me.zort.containr;

public interface ComponentSource {

    boolean enable(ComponentTunnel tunnel);
    void disable();

}
