package de.cloudypanda.dragonEggSaver;

import lombok.Getter;
import lombok.Setter;

public class EggHolder<T> {
    @Getter @Setter private T holder = null;
    @Getter @Setter private boolean active = false;
}
