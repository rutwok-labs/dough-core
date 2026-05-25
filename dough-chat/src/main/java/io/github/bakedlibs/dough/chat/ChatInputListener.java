package io.github.bakedlibs.dough.chat;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

class ChatInputListener implements Listener {

    private Plugin plugin;
    protected Map<UUID, Set<ChatInputHandler>> handlers;

    protected ChatInputListener(Plugin plugin) {
        this.plugin = plugin;
        this.handlers = new ConcurrentHashMap<>();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        registerPaperChatEvent(plugin);
    }

    public void addCallback(UUID uuid, ChatInputHandler input) {
        Set<ChatInputHandler> callbacks = handlers.computeIfAbsent(uuid, id -> ConcurrentHashMap.newKeySet());
        callbacks.add(input);
    }

    public boolean removeCallback(UUID uuid, ChatInputHandler input) {
        Set<ChatInputHandler> callbacks = handlers.get(uuid);

        if (callbacks == null) {
            return false;
        }

        boolean removed = callbacks.remove(input);

        if (callbacks.isEmpty()) {
            handlers.remove(uuid);
        }

        return removed;
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        if (e.getPlugin() == plugin) {
            ChatInput.listeners.remove(plugin);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        handlers.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        handlers.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        checkInput(e, e.getPlayer(), e.getMessage());
    }

    @EventHandler
    public void onComamnd(PlayerCommandPreprocessEvent e) {
        checkInput(e, e.getPlayer(), e.getMessage());
    }

    private void checkInput(Cancellable e, Player p, String msg) {
        Set<ChatInputHandler> callbacks = handlers.get(p.getUniqueId());

        if (callbacks != null) {
            Iterator<ChatInputHandler> iterator = callbacks.iterator();

            while (iterator.hasNext()) {
                ChatInputHandler handler = iterator.next();

                if (handler.test(msg)) {
                    iterator.remove();
                    plugin.getServer().getScheduler().runTask(plugin, () -> handler.onChat(p, msg));

                    e.setCancelled(true);
                    return;
                }
            }

            if (callbacks.isEmpty())
                handlers.remove(p.getUniqueId());
        }
    }

    @SuppressWarnings("unchecked")
    private void registerPaperChatEvent(Plugin plugin) {
        if (!PaperLib.isPaper()) {
            return;
        }

        try {
            Class<? extends Event> eventClass = (Class<? extends Event>) Class.forName("io.papermc.paper.event.player.AsyncChatEvent").asSubclass(Event.class);
            EventExecutor executor = (listener, event) -> {
                try {
                    Player player = (Player) eventClass.getMethod("getPlayer").invoke(event);
                    Component message = (Component) eventClass.getMethod("message").invoke(event);
                    String plainMessage = PlainTextComponentSerializer.plainText().serialize(message);
                    checkInput((Cancellable) event, player, plainMessage);
                } catch (ReflectiveOperationException x) {
                    throw new EventException(x);
                }
            };

            plugin.getServer().getPluginManager().registerEvent(eventClass, this, EventPriority.LOWEST, executor, plugin);
        } catch (ClassNotFoundException ignored) {
            // Spigot and older Paper versions use AsyncPlayerChatEvent.
        }
    }

}
