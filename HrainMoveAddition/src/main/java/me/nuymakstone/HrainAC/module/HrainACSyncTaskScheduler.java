/*
 * This file is part of HrainMoveAddition Anticheat.
 * Copyright (C) 2018 HrainMoveAddition Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.nuymakstone.HrainAC.module;

import me.nuymakstone.HrainAC.HrainMoveAddition;

import java.util.ArrayList;
import java.util.List;

public class HrainACSyncTaskScheduler implements Runnable {

    private long currentTick;
    private final HrainMoveAddition HrainAC;
    private List<HrainACTask> tasks;
    private static int HrainACTaskInstances;

    public HrainACSyncTaskScheduler(HrainMoveAddition HrainAC) {
        this.HrainAC = HrainAC;
        this.tasks = new ArrayList<>();
    }

    @Override
    public void run() {
        for(HrainACTask HrainACTask : tasks) {
            if(currentTick % HrainACTask.interval == 0) {
                HrainACTask.task.run();
            }
        }
        currentTick++;
    }

    //Don't harass me for reinventing the wheel; I'm trying to make my life easier.
    public int addRepeatingTask(Runnable task, int interval) {
        HrainACTask HrainACTask = new HrainACTask(task, interval);
        tasks.add(HrainACTask);
        return HrainACTask.id;
    }

    public void cancelTask(int id) {
        for(int i = 0; i < tasks.size(); i++) {
            HrainACTask task = tasks.get(i);
            if(id == task.id) {
                tasks.remove(i);
                break;
            }
        }
    }

    private class HrainACTask {

        private Runnable task;
        private int interval;
        private int id;

        private HrainACTask(Runnable task, int interval) {
            this.task = task;
            this.interval = interval;
            this.id = HrainACTaskInstances;
            HrainACTaskInstances++;
        }
    }
}
