package me.zort.containr;

public class OneElementContainer extends StaticContainer {

    public OneElementContainer(Element element) {
        super(1, 1);
        appendElement(element);
    }

}
