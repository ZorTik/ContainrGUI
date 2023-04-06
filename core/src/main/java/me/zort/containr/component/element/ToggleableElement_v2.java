package me.zort.containr.component.element;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval
@Deprecated
public abstract class ToggleableElement_v2 extends SwitchableElement<Boolean> {

    public ToggleableElement_v2() {
        super(Lists.newArrayList(true, false));
    }

}
