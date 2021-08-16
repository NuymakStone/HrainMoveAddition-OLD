package me.nuymakstone.HrainAC.check.movement.position;

import me.nuymakstone.HrainAC.check.MovementCheck;
import me.nuymakstone.HrainAC.event.MoveEvent;
import me.nuymakstone.HrainAC.util.MathPlus;

public class ECheck extends MovementCheck {

    public ECheck() {
        super("echeck", "%player% 没能绕过 E, VL: %vl%");
    }

    @Override
    protected void check(MoveEvent e) {
        float gcd = MathPlus.gcdRational((float)e.getTo().getY(), (float)e.getFrom().getY());
        if(String.valueOf(gcd).contains("E")) {
            e.getPlayer().setBanned(true);
            e.getPlayer().kickPlayer("This is the sort of stuff that happens when you get your hack-pack from Walmart.");
        }
    }
}
