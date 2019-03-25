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
import java.util.Random;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.inventory.ItemStack;
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
    
        @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("MiningTrophies")) {
            return false;
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
    

    
    
    @EventHandler(priority = EventPriority.LOWEST)
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
        
        boolean addenchants=getConfig().getBoolean("addenchants");
        boolean addeffects=getConfig().getBoolean("addeffects");
        boolean addlore=getConfig().getBoolean("addlore");
        
        ItemStack item = type.createDrop(addenchants,addeffects,addlore);
        
        BlockDropTrophyEvent trophyEvent = new BlockDropTrophyEvent(block,player,item);
        getServer().getPluginManager().callEvent(trophyEvent);
        if (trophyEvent.isCancelled()){//another plugin caught and cancelled the trophy event - don't drop.
            //getLogger().info("Trophy event cancelled.");
            return;
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
