package me.nuymakstone.HrainAC.check.movement.position;

import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.check.MovementCheck;
import me.nuymakstone.HrainAC.event.MoveEvent;
import me.nuymakstone.HrainAC.util.AABB;
import me.nuymakstone.HrainAC.util.Placeholder;
import me.nuymakstone.HrainAC.util.ServerUtils;
import me.nuymakstone.HrainAC.wrap.block.WrappedBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.Openable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NoClip extends MovementCheck {
    private static final double VERTICAL_EPSILON = 0.01;
    private static final Material[] exemptedMats = {Material.WEB, Material.CHEST, Material.ANVIL, Material.PISTON_MOVING_PIECE};

    public NoClip() {
        super("noclip", false, 0, 10, 0.995, 5000, "%player% failed no-clip. Moved into %block%. VL: %vl%", null);
    }

    @Override
    protected void check(MoveEvent e) {
        AABB aabb = AABB.playerCollisionBox.clone();
        aabb.expand(0, -VERTICAL_EPSILON, 0);
        aabb.translate(e.getTo().toVector());

        HrainACPlayer pp = e.getHrainACPlayer();

        Block b = blockCollided(aabb, pp, exemptedMats);

        if(b != null) {
            punishAndTryRubberband(pp, e, new Placeholder("block", b.getType()));
        } else {
            reward(pp);
        }
    }

    private Block blockCollided(AABB aabb, HrainACPlayer pp, Material... exemptedMats) {
        Set<Material> exempt = new HashSet<>(Arrays.asList(exemptedMats));
        Set<Location> ignored = pp.getIgnoredBlockCollisions();

        Vector min = aabb.getMin();
        Vector max = aabb.getMax();

        for (int x = (int)Math.floor(min.getX()); x < (int)Math.ceil(max.getX()); x++) {
            for (int y = (int)Math.floor(min.getY()); y < (int)Math.ceil(max.getY()); y++) {
                for (int z = (int)Math.floor(min.getZ()); z < (int)Math.ceil(max.getZ()); z++) {
                    Block block = ServerUtils.getBlockAsync(new Location(pp.getPlayer().getWorld(), x, y, z));

                    if(block == null || exempt.contains(block.getType()) ||
                            block.getState().getData() instanceof Openable || ignored.contains(block.getLocation()))
                        continue;

                    AABB[] blockBoxes = WrappedBlock.getWrappedBlock(block, pp.getClientVersion()).getCollisionBoxes();

                    for(AABB box : blockBoxes) {
                        if(aabb.isColliding(box)) {
                            return block;
                        }
                    }

                }
            }
        }

        return null;
    }
}
