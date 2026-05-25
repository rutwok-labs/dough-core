package io.github.bakedlibs.dough.common;

import javax.annotation.Nonnull;

import io.papermc.lib.PaperLib;
import org.bukkit.ChatColor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * This class contains some utilities related to {@link ChatColor}.
 * 
 * @author TheBusyBiscuit
 *
 */
public final class ChatColors {

    private ChatColors() {}

    /**
     * This is just a simple shortcut for:
     * <code>ChatColor.translateAlternateColorCodes('&amp;', input)</code>
     * 
     * @param input
     *            The String that should be colored
     * 
     * @return The colored String
     */
    public static @Nonnull String color(@Nonnull String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    /**
     * This method colors the given String in alternating Colors.
     * <code>ChatColors.alternating("Hello World", ChatColor.YELLOW, ChatColor.RED)</code>
     * will yield a String where each letter is yellow or red (in alternating patterns).
     * 
     * @param text
     *            The String that should be colored
     * @param colors
     *            The Colors that should be applied
     * 
     * @return The colored String
     */
    public static @Nonnull String alternating(@Nonnull String text, ChatColor... colors) {
        int i = 0;
        StringBuilder builder = new StringBuilder(text.length() * 3);

        for (char c : text.toCharArray()) {
            builder.append(colors[i % colors.length].toString() + c);
            i++;
        }

        return builder.toString();
    }

    public static @Nonnull Component colorComponent(@Nonnull String input) {
        if (!PaperLib.isPaper()) {
            throw new UnsupportedOperationException("Adventure Components are only supported on Paper.");
        }

        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);
    }

    public static @Nonnull Component alternatingComponent(@Nonnull String text, TextColor... colors) {
        if (!PaperLib.isPaper()) {
            throw new UnsupportedOperationException("Adventure Components are only supported on Paper.");
        }

        Component component = Component.empty();
        int i = 0;

        for (char c : text.toCharArray()) {
            component = component.append(Component.text(c).color(colors[i % colors.length]));
            i++;
        }

        return component;
    }

}
