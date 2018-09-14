/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.crashdemons.miningtrophies;

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
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author crash
 */
public class MiningTrophies extends JavaPlugin implements Listener{

    private final Random rand = new Random();
    
    @Override
    public void onEnable(){
        getLogger().info("Enabling...");
        saveDefaultConfig();
        reloadConfig();
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
    
    
    
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event){
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
        Double droproll = rand.nextDouble();
        Double droprate = getConfig().getDouble( type.getDropConfigName() );
        if(droprate==0.0) return;
        if(player.hasPermission("miningtrophies.alwaysrewarded")) droproll=0.00;//this player always gets good rolls.
        if(droproll >= (droprate*fortunerate)) return;//bad roll
        
        
        
        ItemStack item = type.createDrop();
        Location location = block.getLocation();
        location.getWorld().dropItemNaturally(location, item);
        
    }
    
}
