package me.zort.containr.component.container;

import me.zort.containr.Element;
import me.zort.containr.StaticContainer;

@Deprecated
public class OneElementContainer extends StaticContainer {

    public OneElementContainer(Element element) {
        super(1, 1);
        appendElement(element);
    }

}
