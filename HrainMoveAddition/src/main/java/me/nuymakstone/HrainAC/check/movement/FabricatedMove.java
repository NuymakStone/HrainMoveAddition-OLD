package me.nuymakstone.HrainAC.check.movement;

import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.check.MovementCheck;
import me.nuymakstone.HrainAC.event.MoveEvent;
import me.nuymakstone.HrainAC.util.ServerUtils;
import me.nuymakstone.HrainAC.wrap.packet.WrappedPacket;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.nuymakstone.HrainAC.util.ServerUtils.getProtocolVersion;

public class FabricatedMove extends MovementCheck {

    private final Map<UUID, Integer> flyingTicksMap;

    public FabricatedMove() {
        super("fabricatedmove", true, 0, 2, 0.999, 5000, "%player% failed fabricated move, VL: %vl%", null);
        flyingTicksMap = new HashMap<>();
    }

    @Override
    protected void check(MoveEvent e) {
        HrainACPlayer pp = e.getHrainACPlayer();
        if(getProtocolVersion(e.getPlayer()) <= 47){
        if(pp.getPlayer().isInsideVehicle() || e.isTeleportAccept() || !e.getTo().getWorld().equals(e.getFrom().getWorld())) {
            flyingTicksMap.put(pp.getUuid(), 0);
            return;
        }

        //也忽略tp之后的一些动作来修复误报。
        //使用ping，因为如果你发送多个相同位置的TP，
        //如果玩家静止不动，HrainAC将注册第一个
        //在批处理中接受并忽略后续的。 添加 10 个刻度
        //只是为了安全。 - 海岛童子军
        if(pp.getCurrentTick() - pp.getLastTeleportAcceptTick() > ServerUtils.getPing(e.getPlayer()) / 50 + 10 &&
                pp.getCurrentTick() > 100) {

            WrappedPacket packet = e.getWrappedPacket();
            switch(packet.getType()) {
                case POSITION:
                    //我们可以通过检查将检查扩展到定位数据包
                    //自上次飞行数据包以来的速度。 - 海岛童子军
                    if(!e.hasDeltaPos() && pp.getVelocity().lengthSquared() > 0) {
                        punishAndTryRubberband(e.getHrainACPlayer(), e);
                    } else {
                        reward(e.getHrainACPlayer());
                    }
                    break;
                case LOOK:
                    if(!e.hasDeltaRot()) {
                        punishAndTryRubberband(e.getHrainACPlayer(), e);
                    } else {
                        reward(e.getHrainACPlayer());
                    }
                    break;
                case POSITION_LOOK:
                    if(!e.hasDeltaRot() || (!e.hasDeltaPos() && pp.getVelocity().lengthSquared() > 0)) {
                        punishAndTryRubberband(e.getHrainACPlayer(), e);
                    } else {
                        reward(e.getHrainACPlayer());
                    }
                    break;
            }

            UUID uuid = pp.getUuid();
            int flying = flyingTicksMap.getOrDefault(uuid, 0);

            //moved less than 0.03
            if(e.isUpdatePos() && flying == 0 && e.getTo().distanceSquared(e.getFrom()) < 0.00089) {
                punishAndTryRubberband(e.getHrainACPlayer(), e);
            }

            if(!e.isUpdatePos()) {
                flying++;
            }
            else {
                flying = 0;
            }
            flyingTicksMap.put(uuid, flying);

            if(flying > 20) {
                punishAndTryRubberband(pp, flying - 20, e);
            }
        } }
    }

    @Override
    public void removeData(Player p) {
        flyingTicksMap.remove(p.getUniqueId());
    }
}
