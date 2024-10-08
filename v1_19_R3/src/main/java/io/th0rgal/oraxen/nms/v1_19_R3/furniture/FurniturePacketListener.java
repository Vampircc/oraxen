package io.th0rgal.oraxen.nms.v1_19_R3.furniture;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.OraxenFurniture;
import io.th0rgal.oraxen.api.events.OraxenNativeMechanicsRegisteredEvent;
import io.th0rgal.oraxen.api.events.furniture.OraxenFurnitureInteractEvent;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.IFurniturePacketManager;
import io.th0rgal.oraxen.utils.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class FurniturePacketListener implements Listener {

    @EventHandler
    public void onFurnitureFactory(OraxenNativeMechanicsRegisteredEvent event) {
        double r = FurnitureFactory.get().simulationRadius;
        for (Player player : Bukkit.getOnlinePlayers()) for (Entity baseEntity : player.getNearbyEntities(r,r,r)) {
            FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(baseEntity);
            IFurniturePacketManager packetManager = FurnitureFactory.instance.packetManager();
            if (mechanic == null) continue;

            packetManager.sendFurnitureEntityPacket(baseEntity, mechanic, player);
            packetManager.sendInteractionEntityPacket(baseEntity, mechanic, player);
            packetManager.sendBarrierHitboxPacket(baseEntity, mechanic, player);
        }
    }

    @EventHandler
    public void onFurnitureAdded(EntityAddToWorldEvent event) {
        Entity baseEntity = event.getEntity();
        FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(baseEntity);
        if (mechanic == null) return;
        IFurniturePacketManager packetManager = FurnitureFactory.get().packetManager();

        for (Player player : baseEntity.getWorld().getNearbyPlayers(baseEntity.getLocation(), FurnitureFactory.get().simulationRadius)) {
            packetManager.sendFurnitureEntityPacket(baseEntity, mechanic, player);
            packetManager.sendInteractionEntityPacket(baseEntity, mechanic, player);
            packetManager.sendBarrierHitboxPacket(baseEntity, mechanic, player);
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFurnitureRemoval(EntityRemoveFromWorldEvent event) {
        Entity baseEntity = event.getEntity();
        FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(baseEntity);
        if (mechanic == null) return;
        IFurniturePacketManager packetManager = FurnitureFactory.get().packetManager();

        packetManager.removeFurnitureEntityPacket(baseEntity, mechanic);
        packetManager.removeInteractionHitboxPacket(baseEntity, mechanic);
        packetManager.removeBarrierHitboxPacket(baseEntity, mechanic);
    }

    @EventHandler
    public void onPlayerChunkLoad(PlayerChunkLoadEvent event) {
        Player player = event.getPlayer();
        IFurniturePacketManager packetManager = FurnitureFactory.instance.packetManager();
        for (Entity baseEntity : event.getChunk().getEntities()) {
            FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(baseEntity);
            if (mechanic == null) return;
            packetManager.sendFurnitureEntityPacket(baseEntity, mechanic, player);
            packetManager.sendInteractionEntityPacket(baseEntity, mechanic, player);
            packetManager.sendBarrierHitboxPacket(baseEntity, mechanic, player);
        }
    }

    @EventHandler
    public void onPlayerChunkUnload(PlayerChunkUnloadEvent event) {
        Player player = event.getPlayer();
        IFurniturePacketManager packetManager = FurnitureFactory.instance.packetManager();
        if (packetManager != null) for (Entity baseEntity : event.getChunk().getEntities()) {
            FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(baseEntity);
            if (mechanic == null) return;
            packetManager.removeFurnitureEntityPacket(baseEntity, mechanic, player);
            packetManager.removeInteractionHitboxPacket(baseEntity, mechanic, player);
            packetManager.removeBarrierHitboxPacket(baseEntity, mechanic, player);
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        IFurniturePacketManager packetManager = FurnitureFactory.instance.packetManager();

        for (Entity baseEntity : event.getFrom().getEntities()) {
            FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(baseEntity);
            if (mechanic == null) return;
            packetManager.removeFurnitureEntityPacket(baseEntity, mechanic, player);
            packetManager.removeInteractionHitboxPacket(baseEntity, mechanic, player);
            packetManager.removeBarrierHitboxPacket(baseEntity, mechanic, player);
        }
    }

    @EventHandler
    public void onUseUnknownEntity(PlayerUseUnknownEntityEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        ItemStack itemInHand = hand == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
        IFurniturePacketManager packetManager = FurnitureFactory.instance.packetManager();

        Entity baseEntity = packetManager.baseEntityFromHitbox(event.getEntityId());
        if (baseEntity == null) return;
        FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(baseEntity);
        if (mechanic == null) return;

        if (event.isAttack()) OraxenFurniture.remove(baseEntity, player);
        else EventUtils.callEvent(new OraxenFurnitureInteractEvent(mechanic, baseEntity, player, itemInHand, hand));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamageBarrierHitbox(BlockDamageEvent event) {
        Location location = event.getBlock().getLocation();
        FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(location);
        if (mechanic == null) return;
        OraxenFurniture.remove(location, null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreativePlayerBreakBarrierHitbox(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(location);
        if (mechanic == null) return;
        OraxenFurniture.remove(location, null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractBarrierHitbox(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        Location interactionPoint = event.getInteractionPoint();
        FurnitureMechanic mechanic = Optional.ofNullable(OraxenFurniture.getFurnitureMechanic(clickedBlock))
                .orElse(OraxenFurniture.getFurnitureMechanic(interactionPoint));
        if (mechanic == null) return;

        Entity baseEntity = Optional.ofNullable(mechanic.baseEntity(clickedBlock)).orElse(mechanic.baseEntity(interactionPoint));
        if (baseEntity == null) return;

        EventUtils.callEvent(new OraxenFurnitureInteractEvent(mechanic, baseEntity, player, event.getItem(), event.getHand(), interactionPoint));
        // Resend the hitbox as client removes the "ghost block"
        Bukkit.getScheduler().runTaskLater(OraxenPlugin.get(), () ->
                FurnitureFactory.instance.packetManager().sendBarrierHitboxPacket(baseEntity, mechanic, player), 1L);
    }
}
