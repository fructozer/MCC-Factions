package dansplugins.factionsystem.commands;

import dansplugins.factionsystem.ChunkManager;
import dansplugins.factionsystem.DynmapManager;
import dansplugins.factionsystem.data.PersistentData;
import dansplugins.factionsystem.objects.Faction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCommand {

    public boolean claim(CommandSender sender) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("mf.claim")) {
            player.sendMessage(ChatColor.RED + "Sorry! You need the following permission to use this command: 'mf.claim'");
            return false;
        }

        Faction playersFaction = PersistentData.getInstance().getPlayersFaction(player.getUniqueId());

        if (playersFaction == null) {
            player.sendMessage(ChatColor.RED + "You must be in a faction to use this command.");
            return false;
        }

        // TODO: add officer/owner check

        // if at demesne limit
        if (!(ChunkManager.getInstance().getChunksClaimedByFaction(playersFaction.getName(), PersistentData.getInstance().getClaimedChunks()) < playersFaction.getCumulativePowerLevel())) {
            player.sendMessage(ChatColor.RED + "You have reached your demesne limit! Invite more players to increase this.");
            return false;
        }

        ChunkManager.getInstance().addChunkAtPlayerLocation(player);
        DynmapManager.updateClaims();
        return true;
    }

}
