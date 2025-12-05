package de.cloudypanda.dragonEggSaver;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public final class Texts {
    private Texts() {}

    public static final TextComponent pluginPrefix = Component.text()
            .color(TextColor.color(50, 205, 50))
            .append(Component.text("End-Event | ").color(TextColor.color(50, 205, 50))).build();

    public static final TextComponent cannotMoveEggToInventory = pluginPrefix.append(
            Component.text("Du kannst das Drachenei nur in deinem Inventar behalten!").color(TextColor.color(255, 69, 0))
    );

    public static final TextComponent noEggHolder = pluginPrefix.append(
            Component.text("Aktuell hat niemand das Drachenei!").color(TextColor.color(255, 69, 0))
    );

    public static final TextComponent selfEggHolder = pluginPrefix.append(
            Component.text("Du hast das Drachenei!").color(TextColor.color(50, 205, 50))
    );

    public static final TextComponent eggDroppedDueToMissingSpace = pluginPrefix.append(
            Component.text("Das Drachenei wurde fallengelassen, da du keinen Platz im Inventar hast!")
                    .color(TextColor.color(255, 69, 0))
    );

    public static final TextComponent eggReturnedToHolder = pluginPrefix.append(
            Component.text("Das Drachenei kam zurück zu seinem Besitzer!").color(TextColor.color(50, 205, 50))
    );
}
