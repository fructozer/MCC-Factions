package dansplugins.factionsystem.events;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TerritoryLeaveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Chunk chunk;
    public TerritoryLeaveEvent(Player player, Chunk chunk) {
        this.player = player;
        this.chunk = chunk;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }
    public Chunk getChunk(){
        return chunk;
    }
}
