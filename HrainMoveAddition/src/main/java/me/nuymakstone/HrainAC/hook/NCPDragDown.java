package me.nuymakstone.HrainAC.hook;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.logging.LogManager;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NCPDragDown implements NCPHook
{
    private final CheckType[] type;

    public NCPDragDown() {
        this.type = new CheckType[] { CheckType.MOVING_SURVIVALFLY, CheckType.MOVING_PASSABLE };
        this.hook();
    }

    public boolean onCheckFailure(final CheckType checkType, final Player p, final IViolationInfo vlInfo) {
        return !p.hasPermission("ncpdd.bypass") && vlInfo.willCancel() && this.dragDown(p, checkType);
    }

    private boolean dragDown(final Player p, final CheckType checkType) {
        double MaxY = -1.0;
        float FallDist = -1.0f;
        final boolean debug = p.hasPermission("ncpdd.debug");
        final IPlayerData pData = DataManager.getPlayerData(p);
        if (pData != null) {
            final MovingData mData = (MovingData)pData.getGenericInstance((Class)MovingData.class);
            MaxY = mData.noFallMaxY;
            FallDist = mData.noFallFallDistance;
        }
        if (p.getLocation().getY() <= 0.0) {
            if (MaxY != -1.0 && FallDist != -1.0) {
                final MovingData mData = (MovingData)pData.getGenericInstance((Class)MovingData.class);
                mData.noFallMaxY = MaxY;
                mData.setTeleported(p.getLocation().clone().subtract(0.0, 2.0, 0.0));
                mData.noFallFallDistance = FallDist + 2.0f;
                if (debug) {
                    this.log("Set position to 2 blocks lower!", p);
                }
            }
            if (p.getLocation().getY() <= -70.0) {
                BridgeHealth.setHealth((LivingEntity)p, 0.0);
                BridgeHealth.damage((LivingEntity)p, 1.0);
            }
            return false;
        }
        final Location ploc = p.getLocation();
        final Block bDown = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
        if (PlayerUtil.isOnGround(p, 1.0) || PlayerUtil.isOnGround(p, 0.0)) {
            if (pData != null) {
                final MovingData mData2 = (MovingData)pData.getGenericInstance((Class)MovingData.class);
                if (mData2.hasSetBack() && mData2.getSetBackY() >= p.getLocation().getY()) {
                    mData2.noFallMaxY = MaxY;
                    mData2.noFallFallDistance = FallDist;
                    mData2.setTeleported(p.getLocation());
                    if (debug) {
                        this.log("Set position to current location!", p);
                    }
                    return false;
                }
            }
            return false;
        }
        ploc.setX(p.getLocation().getX());
        ploc.setZ(p.getLocation().getZ());
        ploc.setPitch(p.getLocation().getPitch());
        ploc.setYaw(p.getLocation().getYaw());
        boolean is2 = false;
        if (PlayerUtil.isAir(bDown.getType())) {
            final Block bDown2 = bDown.getRelative(BlockFace.DOWN);
            if (PlayerUtil.isAir(bDown2.getType())) {
                is2 = true;
                ploc.setY((double)bDown2.getLocation().getBlockY());
            }
            else {
                ploc.setY((double)bDown.getLocation().getBlockY());
            }
        }
        if (MaxY != -1.0 && FallDist != -1.0) {
            final MovingData mData3 = (MovingData)pData.getGenericInstance((Class)MovingData.class);
            mData3.noFallMaxY = MaxY;
            mData3.setTeleported(ploc);
            mData3.noFallFallDistance = FallDist + (is2 ? 2.0f : 1.0f);
        }
        if (debug) {
            this.log("Set position to " + (is2 ? "2" : "1") + " block(s) lower!", p);
        }
        return false;
    }

    public void hook() {
        NCPHookManager.addHook(this.type, (NCPHook)this);
    }

    public String getHookName() {
        return "NCPDragDown";
    }

    public String getHookVersion() {
        return "1.1";
    }

    private void log(final String s, final Player p) {
        final LogManager logManager = NCPAPIProvider.getNoCheatPlusAPI().getLogManager();
        final StringBuilder builder = new StringBuilder(300);
        builder.append(this.getHookName());
        builder.append(" [" + ChatColor.YELLOW + p.getName());
        builder.append(ChatColor.WHITE + "] ");
        builder.append(s);
        final String message = builder.toString();
        logManager.info(Streams.NOTIFY_INGAME, message);
    }
}
