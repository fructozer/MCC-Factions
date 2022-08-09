package dansplugins.factionsystem.worldwar;

import dansplugins.factionsystem.MedievalFactions;
import dansplugins.factionsystem.data.PersistentData;
import dansplugins.factionsystem.externalapi.MF_Faction;
import dansplugins.factionsystem.objects.domain.ClaimedChunk;
import dansplugins.factionsystem.objects.domain.Faction;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class WorldUtility {
    private MedievalFactions medievalFactions;
    private PersistentData accessor;
    public static WorldUtility inst;

    public WorldUtility(MedievalFactions medievalFactions, PersistentData accessor){
        this.medievalFactions = medievalFactions;
        this.accessor = accessor;
        inst = this;
    }

    public String getFaction(Chunk chunk){
        ClaimedChunk c = accessor.getChunkDataAccessor().getClaimedChunk(chunk);
        return c!=null?c.getHolder():null;
    }
    public String getFaction(Player p){
        MF_Faction fac =  medievalFactions.getAPI().getFaction(p);
        return fac!=null?fac.getName():null;
    }
    public boolean isSafe(Player p,Chunk c){
        String facP = getFaction(p);
        String facC = getFaction(c);
        if (facC==null||facP==null||facP.equals(facC)) return true;
        return medievalFactions.getAPI().getFaction(facC).isAlly(facP);
    }
}
