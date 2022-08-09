package dansplugins.factionsystem.events;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TerritoryEnterEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player p;
    private final Chunk chunk;

    public TerritoryEnterEvent(Player p,Chunk chunk) {
        this.p = p;
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

    public Chunk getChunk(){
        return chunk;
    }

    public Player getPlayer() {
        return p;
    }

}
