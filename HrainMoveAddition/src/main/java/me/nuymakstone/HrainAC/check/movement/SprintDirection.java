package me.nuymakstone.HrainAC.check.movement;

import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.check.MovementCheck;
import me.nuymakstone.HrainAC.event.MoveEvent;
import me.nuymakstone.HrainAC.util.Direction;
import me.nuymakstone.HrainAC.util.MathPlus;
import me.nuymakstone.HrainAC.wrap.entity.WrappedEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class SprintDirection extends MovementCheck {
    private Map<UUID, Long> lastSprintTickMap;
    private Set<UUID> collisionHorizontalSet;

    public SprintDirection() {
        super("sprintdirection", true, 5, 5, 0.999, 5000, "%player% failed sprint direction, VL %vl%", null);
        lastSprintTickMap = new HashMap<>();
        collisionHorizontalSet = new HashSet<>();
    }

    @Override
    protected void check(MoveEvent e) {
        HrainACPlayer pp = e.getHrainACPlayer();

        boolean collisionHorizontal = collidingHorizontally(e);
        org.bukkit.util.Vector moveHoriz = e.getTo().toVector().subtract(e.getFrom().toVector()).setY(0);

        if(!pp.isSprinting())
            lastSprintTickMap.put(pp.getUuid(), pp.getCurrentTick());

        Set<Material> collidedMats = WrappedEntity.getWrappedEntity(e.getPlayer()).getCollisionBox(e.getFrom().toVector()).getMaterials(pp.getWorld());
        if(pp.isSwimming() || e.isTeleportAccept() || e.hasAcceptedKnockback() ||
                (collisionHorizontal && !collisionHorizontalSet.contains(pp.getUuid())) ||
                pp.getCurrentTick() - lastSprintTickMap.getOrDefault(pp.getUuid(), pp.getCurrentTick()) < 2 ||
                moveHoriz.lengthSquared() < 0.04 || collidedMats.contains(Material.LADDER) ||
                collidedMats.contains(Material.VINE)) {
            return;
        }

        float yaw = e.getTo().getYaw();
        Vector prevVelocity = pp.getVelocity().clone();

        List<Block> activeBlocks = e.getActiveBlocks();
        for(Block b : activeBlocks) {
            if(b.getType() == Material.SOUL_SAND) {
                prevVelocity.multiply(0.4); //which would you prefer: iterative multiplication or using Math.pow()?
            }
            if(b.getType() == Material.WEB) {
                prevVelocity.multiply(0);
                break; //Any number times 0 is 0; no reason to continue looping.
            }
        }

        if(e.hasHitSlowdown()) {
            prevVelocity.multiply(0.6);
        }
        double dX = e.getTo().getX() - e.getFrom().getX();
        double dZ = e.getTo().getZ() - e.getFrom().getZ();
        float friction = e.getFriction();
        dX /= friction;
        dZ /= friction;
        if(e.isJump()) {
            float yawRadians = yaw * 0.017453292F;
            dX += (MathPlus.sin(yawRadians) * 0.2F);
            dZ -= (MathPlus.cos(yawRadians) * 0.2F);
        }

        //Div by 1.7948708571637845???? What the hell are these numbers?

        dX -= prevVelocity.getX();
        dZ -= prevVelocity.getZ();

        Vector moveForce = new Vector(dX, 0, dZ);
        Vector yawVec = MathPlus.getDirection(yaw, 0);

        if(MathPlus.angle(yawVec, moveForce) > Math.PI / 4 + 0.3) { //0.3 is arbitrary. Prevents falses due to silly stuff in game
            punishAndTryRubberband(pp, e);
        }
        else {
            reward(pp);
        }

        if(collisionHorizontal)
            collisionHorizontalSet.add(pp.getUuid());
        else
            collisionHorizontalSet.remove(pp.getUuid());
    }

    private boolean collidingHorizontally(MoveEvent e) {
        for(Direction dir : e.getBoxSidesTouchingBlocks()) {
            if(dir == Direction.EAST || dir == Direction.NORTH || dir == Direction.SOUTH || dir == Direction.WEST)
                return true;
        }
        return false;
    }

    @Override
    public void removeData(Player p) {
        lastSprintTickMap.remove(p.getUniqueId());
        collisionHorizontalSet.remove(p.getUniqueId());
    }
}

