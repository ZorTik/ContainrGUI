package me.zort.containr;

import com.google.common.collect.Lists;

public abstract class ToggleableElement_v2 extends SwitchableElement<Boolean> {

    public ToggleableElement_v2() {
        super(Lists.newArrayList(true, false));
    }

}
