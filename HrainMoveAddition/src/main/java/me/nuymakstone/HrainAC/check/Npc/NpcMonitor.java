package me.nuymakstone.HrainAC.check.Npc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import me.nuymakstone.HrainAC.HrainMoveAddition;
import me.nuymakstone.HrainAC.util.MathHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NpcMonitor implements Listener {
    protected static ConcurrentHashMap<UUID,Integer> idMap = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<UUID,Object> instanceMap = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<UUID,Location> currentLocation = new ConcurrentHashMap<>();

    public static Class<?> craftWorld = null;
    public static Class<?> worldServer = null;
    public static Class<?> craftServer = null;
    public static Class<?> interactManager = null;
    public static Class<?> entityPlayer = null;
    public static Class<?> minecraftServer = null;
    public static Class<?> gameProfile = null;
    public static Class<?> world = null;
    public static Class<?> craftPlayer = null;
    public static Class<?> packet = null;
    public static Class<?> packet_A = null;
    public static Class<?> packet_B = null;
    public static Class<?> info = null;
    public static Class<?> entityHuman = null;

    public static void load() {
        try {
            craftWorld = Class.forName("org.bukkit.craftbukkit.1.8.R3.CraftWorld");
            worldServer = Class.forName("net.minecraft.server.1.8.R3.WorldServer");
            craftServer = Class.forName("org.bukkit.craftbukkit.1.8.R3.CraftServer");
            interactManager = Class.forName("net.minecraft.server.1.8.R3.PlayerInteractManager");
            entityPlayer = Class.forName("net.minecraft.server.1.8.R3.EntityPlayer");
            minecraftServer = Class.forName("net.minecraft.server.1.8.R3.MinecraftServer");
            gameProfile = Class.forName("com.mojang.authlib.GameProfile");
            world = Class.forName("net.minecraft.server.1.8.R3.World");
            entityHuman = Class.forName("net.minecraft.server.1.8.R3.EntityHuman");
            craftPlayer = Class.forName("org.bukkit.craftbukkit.1.8.R3.entity.CraftPlayer");
            packet = Class.forName("net.minecraft.server.1.8.R3.Packet");
            packet_A = Class.forName("net.minecraft.server.1.8.R3.PacketPlayOutPlayerInfo");
            packet_B = Class.forName("net.minecraft.server.1.8.R3.PacketPlayOutNamedEntitySpawn");
            info = Class.forName("net.minecraft.server.1.8.R3.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Location npcLoaction(Player target) {
        Location loc = target.getLocation().clone();
        loc.setPitch(0.0F);
        Location eyeLoc = target.getEyeLocation().clone();
        eyeLoc.setPitch(0.0F);
        Vector eye = eyeLoc.getDirection().clone();
        eye.normalize();
        eye.multiply(-3.23);
        loc.add(eye);
        return loc.add(0, 3.25, 0);
    }

    public static Vector npcEye(Player target) {
        Location loc = target.getLocation().clone();
        Vector eye = target.getEyeLocation().clone().getDirection().clone();
        eye.normalize();
        eye.multiply(2.98);
        return eye;
    }

    public void spawnNPC(Location location, Player target, String name) throws Exception {
        currentLocation.put(target.getUniqueId(), location);
        Object INSTANCE_craftWorld = craftWorld.cast(location.getWorld());
        Object INSTANCE_worldServer = craftWorld.getMethod("getHandle").invoke(INSTANCE_craftWorld);
        Object INSTANCE_craftServer = craftServer.cast(Bukkit.getServer());
        Object INSTANCE_interactManager = interactManager.getConstructor(world).newInstance(INSTANCE_worldServer);
        UUID ids = UUID.randomUUID();
        Object profile = gameProfile
                .getConstructor(UUID.class, String.class)
                .newInstance(ids, name);
        Object INSTANCE_entityPlayer = entityPlayer.getConstructor(minecraftServer, worldServer, gameProfile, interactManager)
                .newInstance(craftServer.getMethod("getServer")
                        .invoke(INSTANCE_craftServer), INSTANCE_worldServer, profile, INSTANCE_interactManager);
        entityPlayer.getMethod("setPosition", double.class, double.class, double.class)
                .invoke(INSTANCE_entityPlayer, location.getX(), location.getY(), location.getZ());
        entityPlayer.getField("yaw").set(INSTANCE_entityPlayer, 0.0F);
        entityPlayer.getField("pitch").set(INSTANCE_entityPlayer, 0.0F);
        instanceMap.put(target.getUniqueId(), entityPlayer);
        idMap.put(target.getUniqueId(), (Integer) entityPlayer.getMethod("getId").invoke(INSTANCE_entityPlayer));
        Object INSTANCE_TARGET_craftPlayer = craftPlayer.cast(target);
        Object handle = craftPlayer.getMethod("getHandle").invoke(INSTANCE_TARGET_craftPlayer);
        Object connection = handle.getClass().getField("playerConnection").get(handle);
        Method sendPacket = connection.getClass().getMethod("sendPacket", packet);
        Object instance = Array.newInstance(entityPlayer, 1);
        Array.set(instance, 0, INSTANCE_entityPlayer);
        sendPacket.invoke(connection, packet_A.getConstructor(info, instance.getClass())
                .newInstance(info.getField("ADD_PLAYER").get(null), instance));
        sendPacket.invoke(connection, packet_B.getConstructor(entityHuman).newInstance(INSTANCE_entityPlayer));

        int id = idMap.get(target.getUniqueId());
        {
            PacketContainer container = HrainMoveAddition.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
            container.getIntegers().write(0, id);
            container.getIntegers().write(1, 0);
            ItemStack stick = new ItemStack(Material.IRON_SWORD);
            stick.addEnchantment(Enchantment.DAMAGE_ALL, 1);
            container.getItemModifier().write(0, stick);
            HrainMoveAddition.protocolManager.sendServerPacket(target, container);
        }
        {
            PacketContainer container = HrainMoveAddition.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
            container.getIntegers().write(0, id);
            container.getIntegers().write(1, 2);
            ItemStack stick = new ItemStack(Material.LEATHER_BOOTS);
            stick.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1);
            LeatherArmorMeta meta = (LeatherArmorMeta) stick.getItemMeta();
            meta.setColor(Color.RED);
            stick.setItemMeta(meta);
            container.getItemModifier().write(0, stick);
            HrainMoveAddition.protocolManager.sendServerPacket(target, container);
        }
        {
            PacketContainer container = HrainMoveAddition.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
            container.getIntegers().write(0, id);
            container.getIntegers().write(1, 3);
            ItemStack stick = new ItemStack(Material.LEATHER_LEGGINGS);
            stick.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1);
            LeatherArmorMeta meta = (LeatherArmorMeta) stick.getItemMeta();
            meta.setColor(Color.RED);
            stick.setItemMeta(meta);
            container.getItemModifier().write(0, stick);
            HrainMoveAddition.protocolManager.sendServerPacket(target, container);
        }
        {
            PacketContainer container = HrainMoveAddition.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
            container.getIntegers().write(0, id);
            container.getIntegers().write(1, 4);
            ItemStack stick = new ItemStack(Material.LEATHER_CHESTPLATE);
            stick.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1);
            LeatherArmorMeta meta = (LeatherArmorMeta) stick.getItemMeta();
            meta.setColor(Color.RED);
            stick.setItemMeta(meta);
            container.getItemModifier().write(0, stick);
            HrainMoveAddition.protocolManager.sendServerPacket(target, container);
        }
        {
            PacketContainer container = HrainMoveAddition.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
            container.getIntegers().write(0, id);
            container.getIntegers().write(1, 5);
            ItemStack stick = new ItemStack(Material.LEATHER_HELMET);
            stick.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1);
            LeatherArmorMeta meta = (LeatherArmorMeta) stick.getItemMeta();
            meta.setColor(Color.RED);
            stick.setItemMeta(meta);
            container.getItemModifier().write(0, stick);
            HrainMoveAddition.protocolManager.sendServerPacket(target, container);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player  player = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(HrainMoveAddition.instance, () -> {
            try {
                if (Bukkit.getPlayerExact(player.getName()) != null) {
                    spawnNPC(npcLoaction(player), player, "");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 20L);
    }

    public List<Player> onlinePlayers() {
        List<Player> onlines = new ArrayList<>();
        Bukkit.getWorlds().forEach(x -> x.getPlayers().forEach(onlines::add));
        return onlines;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (NpcMonitor.instanceMap.containsKey(e.getPlayer().getUniqueId())) {
            Location current = NpcMonitor.npcLoaction(e.getPlayer());
            Location previous = NpcMonitor.currentLocation.get(e.getPlayer().getUniqueId());
            int id = NpcMonitor.idMap.get(e.getPlayer().getUniqueId());
            Object instance = NpcMonitor.instanceMap.get(e.getPlayer().getUniqueId());
            PacketContainer container = HrainMoveAddition.protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
            container.getIntegers().write(0, id);
            container.getIntegers().write(1, MathHelper.floor(current.getX() * 32.0D));
            container.getIntegers().write(2, MathHelper.floor(current.getY() * 32.0D));
            container.getIntegers().write(3, MathHelper.floor(current.getZ() * 32.0D));
            Vector eye = e.getPlayer().getLocation().getDirection();
            double rotY_Pitch = Math.toDegrees(-Math.asin(eye.getY()));
            double xz = Math.cos(Math.toRadians(rotY_Pitch));
            double rotX_Yaw = Math.toDegrees(Math.asin(eye.getX() / -xz));
            container.getBytes().write(0,  ((byte)(int)(rotX_Yaw * 256.0F / 360.0F)));
            container.getBytes().write(1, ((byte)(int)(rotY_Pitch * 256.0F / 360.0F)));
            container.getBooleans().write(0, true);
            try {
                HrainMoveAddition.protocolManager.sendServerPacket(e.getPlayer(), container);
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            }
            if (new Random().nextInt(10) == 4) {
                PacketContainer container_2 = HrainMoveAddition.protocolManager.createPacket(PacketType.Play.Server.ANIMATION);
                container_2.getIntegers().write(0, id);
                container_2.getIntegers().write(1, 0);
                try {
                    HrainMoveAddition.protocolManager.sendServerPacket(e.getPlayer(), container_2);
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
            NpcMonitor.currentLocation.replace(e.getPlayer().getUniqueId(), current);
        }
    }
}
