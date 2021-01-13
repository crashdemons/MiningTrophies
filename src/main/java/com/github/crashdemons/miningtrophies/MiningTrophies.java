/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/ .
 */
package com.github.crashdemons.miningtrophies;

import com.github.crashdemons.miningtrophies.events.BlockDropTrophyEvent;
import com.github.crashdemons.miningtrophies.events.SimulatedBlockBreakEvent;
import com.github.crashdemons.miningtrophies.events.TrophyRollEvent;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin class for MiningTrophies
 * @author crash
 */
public class MiningTrophies extends JavaPlugin implements Listener{

    private final Random rand = new Random();
    
    private boolean NCPEnabled = false;
    
    /**
     * Checks whether the plugin has detected NoCheatPlus and will attempt to support it.
     * @return whether nocheatplus was detected
     */
    public boolean isNCPEnabled(){ return NCPEnabled; }
    
    
    @Override
    public void onEnable(){
        getLogger().info("Enabling...");
        saveDefaultConfig();
        reloadConfig();
        
        if (getServer().getPluginManager().getPlugin("NoCheatPlus") != null) {
            NCPEnabled = true;
            getLogger().info("NCP Support Enabled");
        }
        
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Enabled.");
        
    }

    @Override
    public void onDisable(){
        getLogger().info("Disabling...");
        saveConfig();
        getLogger().info("Disabled.");
    }
    
    public ItemStack createTrophyDrop(TrophyType type){
        boolean addenchants=getConfig().getBoolean("addenchants");
        boolean addeffects=getConfig().getBoolean("addeffects");
        boolean addlore=getConfig().getBoolean("addlore");
        return type.createDrop(addenchants,addeffects,addlore);
    }
    
    
        @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("MiningTrophies")) {
            return false;
        }
        // [reload|give] [args]
        if(args.length<1) return false;
        String subcommand = args[0];
        
        switch(subcommand.toLowerCase()){
            case "reload":
                if(!sender.hasPermission("miningtrophies.config.reload")){
                    sender.sendMessage("You do not have permission to use this command.");
                    return true;
                }
                reloadConfig();
                sender.sendMessage("["+label+"] Reloaded config.");
                return true;
            case "give":
                if(args.length<3) return false;//give trophy num [to]
                if(!sender.hasPermission("miningtrophies.give")){
                    sender.sendMessage("You do not have permission to use this command.");
                    return true;
                }
                String targetMessage="";
                CommandSender target = sender;
              
                TrophyType type;
                try{
                    type = TrophyType.valueOf(args[1].toUpperCase());
                }catch(Exception e){
                    sender.sendMessage("Unknown trophy type: "+args[1]);
                    return true;
                }
                
                int amount;
                try{
                    amount = Integer.valueOf(args[2]);
                }catch(NumberFormatException e){
                    sender.sendMessage("Invalid amount: "+args[2]);
                    return true;
                }
                
                if(args.length>=4 && sender.hasPermission("miningtrophies.give.other")){
                    target = Bukkit.getPlayer(args[3]);
                    if(target==null){  sender.sendMessage("Couldn't find player: "+args[3]); return true; }
                    targetMessage = "("+args[3]+") ";
                }
                
                if(!(target instanceof Player)){ sender.sendMessage(targetMessage+"Only players may receive trophy-items."); return true; }
                Player player = (Player) target;
               

                ItemStack item = createTrophyDrop(type);
                item.setAmount(amount);
                InventoryManager.addItem(player, item);
                sender.sendMessage(targetMessage+"Gave "+amount+" "+type.getDropName());
        }
        
        
        if(!sender.hasPermission("miningtrophies.config.reload")){
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if(args.length!=1 || !args[0].equalsIgnoreCase("reload")){
            sender.sendMessage("["+label+"] Usage: /"+label+" reload");
            return true;
        }
        if(args[0].equalsIgnoreCase("reload")){
            reloadConfig();
            sender.sendMessage("["+label+"] Reloaded config.");
            return true;
        }
        return false;
    }
    
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event){
        ItemStack item = event.getItemInHand();
        if(item==null || item.getType().isAir()) return;
        if(!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if(!meta.hasLore()) return;
        
        if(!getConfig().getBoolean("disableplacement")) return;
        
        for(String line : meta.getLore()){
            if(line.contains("Mining Trophy")){
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler(ignoreCancelled=true,priority=EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event){
        if(!getConfig().getBoolean("fixdroppedtrophies")) return;
        Item entity = event.getEntity();
        ItemStack stack = entity.getItemStack();
        TrophyType type = TrophyType.identifyTrophyItem(stack);
        if(type==null) return;
        ItemStack newStack = createTrophyDrop(type);
        if(newStack==null){ getLogger().warning("replacement trophy item was null!"); return; }
        if(newStack.getType().isAir()){getLogger().warning("replacement trophy item was air!"); return; }
        entity.setItemStack(newStack);
        
    }

    @EventHandler(ignoreCancelled=true, priority = EventPriority.LOWEST)
    public void onBlockBreakEvent(BlockBreakEvent event){
        if(event instanceof SimulatedBlockBreakEvent) return;
        Block block = event.getBlock();
        if(block==null) return;
        TrophyType type = TrophyType.get(block.getType());
        if(type==null) return;
        Player player = event.getPlayer();
        if(player==null) return;
        if(event.isCancelled()) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;//players in creative destroy blocks, they don't mine them.
        if(!player.hasPermission("miningtrophies.canberewarded")) return;//can't get rewards
        
        
        ItemStack tool = player.getEquipment().getItemInMainHand();
        double fortunerate=1;
        if (tool != null) {
            if(tool.getType()==Material.SHEARS) return;//shears are not permitted for trophies
            if(tool.getEnchantmentLevel(Enchantment.SILK_TOUCH)>0) return;//silk touch is not permitted to generate rewards
            fortunerate = 1 + (getConfig().getDouble("fortunerate") * tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));
        }
        
        
        //check drop rates
        Double droprollOriginal = rand.nextDouble();
        Double droproll = droprollOriginal;
        Double droprateOriginal = getConfig().getDouble( type.getDropConfigName() );
        Double droprateEffective = droprateOriginal;
        if(droprateEffective==0.0) return;
        boolean playerAlwaysRewarded = player.hasPermission("miningtrophies.alwaysrewarded");
        if(playerAlwaysRewarded) droproll=0.00;//this player always gets good rolls.
        droprateEffective *= fortunerate;
        
        boolean droprollSuccess = droproll < droprateEffective;
        TrophyRollEvent rollEvent = new TrophyRollEvent(player,block,playerAlwaysRewarded,fortunerate,droprollOriginal,droproll,droprateOriginal,droprateEffective,droprollSuccess);
        getServer().getPluginManager().callEvent(rollEvent);
        if(!rollEvent.succeeded()){//bad roll
            //getLogger().info("Roll wasn't lucky");
            return;
        }
        
        
        PluginManager pm = getServer().getPluginManager();
        boolean wasExemptFromNCP = true;
        if (NCPEnabled) {
            wasExemptFromNCP = NCPExemptionManager.isExempted(player, CheckType.BLOCKBREAK_FASTBREAK);
            //getLogger().info("NCP Exemption: "+wasExemptFromNCP);
            if (!wasExemptFromNCP){
                //getLogger().info("NCP Exemption added");
                NCPExemptionManager.exemptPermanently(player, CheckType.BLOCKBREAK_FASTBREAK);
            }
        }
        pm.callEvent(new PlayerAnimationEvent(player));
        pm.callEvent(new BlockDamageEvent(player, block, player.getEquipment().getItemInMainHand(), true));
        SimulatedBlockBreakEvent simulatedbreak = new SimulatedBlockBreakEvent(block, player);
        pm.callEvent(simulatedbreak);
        
        if (NCPEnabled && !wasExemptFromNCP){
            NCPExemptionManager.unexempt(player, CheckType.BLOCKBREAK_FASTBREAK);
            //getLogger().info("NCP Exemption removed");
        }
        
        
        
        
        if(simulatedbreak.isCancelled()){
            //getLogger().info("Simulated block break cancelled.");
            event.setCancelled(true);
            return;
        }
        
        
        ItemStack item = createTrophyDrop(type);
        
        BlockDropTrophyEvent trophyEvent = new BlockDropTrophyEvent(block,player,item);
        getServer().getPluginManager().callEvent(trophyEvent);
        if (trophyEvent.isCancelled()){//another plugin caught and cancelled the trophy event - don't drop.
            //getLogger().info("Trophy event cancelled.");
            return;
        }
        
        
        //broadcast message about the trophy - rouugh code adapted from PH, needs ironing out
        FileConfiguration configFile = getConfig();
        if (configFile.getBoolean("broadcast")) {
            String message = player.getDisplayName()+" found a "+type.getDropName()+".";

            int broadcastRange = configFile.getInt("broadcastrange");
            if (broadcastRange > 0) {
                broadcastRange *= broadcastRange;
                Location location = player.getLocation();
                List<Player> players = player.getWorld().getPlayers();

                for (Player loopPlayer : players) {
                    try{
                        if (location.distanceSquared(loopPlayer.getLocation()) <= broadcastRange) {
                            loopPlayer.sendMessage(message);
                        }
                    }catch(IllegalArgumentException e){
                        //entities are in different worlds
                    }
                }
            } else {
                getServer().broadcastMessage(message);
            }
        }
        
        
        Location location = block.getLocation();
        
        if(NCPEnabled){//NCP seems to disable the block-break without cancelling it, but allows the drops.
            event.setCancelled(true);
            block.setType(Material.AIR);
        }
        //event.setCancelled(true);
        //getLogger().info("Original break cancelled? "+event.isCancelled());
        //block.setType(Material.AIR);
        location.getWorld().dropItemNaturally(location, item);
        
    }
    
}
