package me.zort.containr;

/**
 * A source class that is used for publishing components to
 * a {@link me.zort.containr.ComponentTunnel}.
 * <p>
 * Also, {@link ComponentSource} is used in {@link Component}
 * for reactively filling the container with elements. So,
 * with this feeature, users are able to fill containers
 * dynamically from various sources.
 *
 * @author ZorTik
 */
public interface ComponentSource {

    boolean enable(ComponentTunnel tunnel);
    void disable(ComponentTunnel tunnel);

}
