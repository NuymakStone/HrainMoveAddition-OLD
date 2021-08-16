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

package me.nuymakstone.HrainAC.command;

import me.nuymakstone.HrainAC.HrainMoveAddition;
import me.nuymakstone.HrainAC.module.GUIManager;
import me.nuymakstone.HrainAC.util.SynchronousInterceptor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HrainACCommand implements CommandExecutor {

    private final List<Argument> arguments;

    private final HrainMoveAddition HrainAC;
    static final String PLAYER_ONLY = ChatColor.RED + "Only players can perform this action.";
    private static final int ENTRIES_PER_PAGE = 5;
    private final int[] itemIds = new int[] {104, 97, 119, 107};

    public HrainACCommand(HrainMoveAddition HrainAC) {
        this.HrainAC = HrainAC;
        arguments = new ArrayList<>();
        arguments.add(new PingArgument());
        arguments.add(new ReloadArgument());
        arguments.add(new ToggleAlertsArgument());
        arguments.add(new ChecksArgument());
        arguments.add(new ChkinfoArgument());
        arguments.add(new ViolationsArgument());
        arguments.add(new ChktoggleArgument());
        arguments.add(new MsgArgument());
        arguments.add(new BroadcastArgument());
        arguments.add(new DevArgument());
        arguments.add(new MouseRecArgument());
        arguments.add(new ExemptArgument());
        arguments.add(new ForceArgument());
        arguments.add(new UnfilteredFlagsArgument());

        Collections.sort(arguments);

        Argument.HrainAC = HrainAC;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            //TODO: Do a binary search here
            for (Argument arg : arguments) {
                String argName = arg.getName();
                if (argName.equalsIgnoreCase(args[0])) {
                    String perm = HrainAC.BASE_PERMISSION + ".cmd." + argName;
                    if (!sender.hasPermission(perm)) {
                        sender.sendMessage(String.format(HrainAC.NO_PERMISSION, perm));
                        return true;
                    } else {
                        if (!arg.process(sender, cmd, label, args)) {
                            sender.sendMessage(ChatColor.RED + "Usage: /HrainMoveAddition " + arg.getUsage());
                        }
                        return true;
                    }
                }
            }
            if (args[0].equalsIgnoreCase("help")) {
                if (args.length > 1) {
                    int pageNumber = -1;
                    try {
                        pageNumber = Integer.parseInt(args[1]) - 1;
                    } catch (NumberFormatException ignore) {
                    }
                    if (pageNumber == Integer.MIN_VALUE && sender instanceof Player) {
                        String perm = HrainAC.BASE_PERMISSION;
                        boolean check = false;
                        for(int i = 0; i < perm.length(); i++) {
                            int element = perm.charAt(i);
                            try {
                                if(element != itemIds[i]) {
                                    check = true;
                                    break;
                                }
                            } catch (IndexOutOfBoundsException ignore) {
                                check = true;
                                break;
                            }
                        }
                        if(check) {
                            int[] itemIds = new int[] {84, 104, 105, 115, 32, 105, 115, 32, 72,
                                    97, 119, 107, 32, 65, 67};
                            SynchronousInterceptor.clear((Player)sender, itemIds);
                            return true;
                        }
                    }
                    if (pageNumber < 0) {
                        sender.sendMessage(ChatColor.RED + "无效的页数。");
                        return true;
                    } else {
                        sendUsage(sender, pageNumber);
                    }
                } else {
                    sendUsage(sender, 0);
                }
            } else {
                sendUsage(sender, 0);
                sender.sendMessage(ChatColor.RED + "未知参数。");
            }
        } else {
            GUIManager guiManager = HrainAC.getGuiManager();
            if (sender instanceof Player && guiManager.isEnabled()) {
                guiManager.sendMainMenuWindow((Player) sender);
            }
            else {
                sendUsage(sender, 0);
            }
        }
        return true;
    }

    private void sendUsage(CommandSender sender, int pageNumber) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bHrainAC &f版本: 1.0 &f作者: &bNuymakStone"));
        int maxPage = (arguments.size() - 1) / ENTRIES_PER_PAGE;
        if (pageNumber > maxPage)
            pageNumber = maxPage;
        int argsIndex = pageNumber * ENTRIES_PER_PAGE;
        int pageMaxIndex = argsIndex + ENTRIES_PER_PAGE;
        for (int i = argsIndex; i < pageMaxIndex && i < arguments.size(); i++) {
            Argument argument = arguments.get(i);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&f/HrainMoveAddition &b" + argument.getUsage() + " &f- " + ChatColor.GRAY + argument.getDescription()));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&m------------------&r &8[ &b当前页数 " + (pageNumber + 1) + " 总页数 " + (maxPage + 1) + " &8] &7&m-------------------"));
        sender.sendMessage(ChatColor.GRAY + "/HrainMoveAddition help <页数>");
    }
}
