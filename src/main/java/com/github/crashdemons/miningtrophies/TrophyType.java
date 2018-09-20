/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.crashdemons.miningtrophies;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author crash
 */
public enum TrophyType {
    DIAMOND_ORE("Perfect Diamond",Material.DIAMOND),
    EMERALD_ORE("Perfect Emerald",Material.EMERALD),
    REDSTONE_ORE("Sparking Redstone",Material.REDSTONE),
    LAPIS_ORE("Marbled Lapis",Material.LAPIS_LAZULI),
    NETHER_QUARTZ_ORE("Rose-Quartz",Material.QUARTZ),
    CLAY("Pure Clay",Material.CLAY_BALL),
    GLOWSTONE("Burning Glowstone",Material.GLOWSTONE_DUST,Enchantment.FIRE_ASPECT),
    TURTLE_EGG("Scute of Shame",Material.SCUTE,"You know what you did.",Enchantment.WATER_WORKER),
    GLASS("What-a-pane",Material.GLASS_PANE,"For the experienced griefer.");
    
    
    private String dropName;
    private String dropLore;
    private Material dropMaterial;
    private Enchantment dropEnchantment;
    
    
    TrophyType(String displayName, Material mat){
        this(displayName,mat,Enchantment.LOOT_BONUS_BLOCKS);
    }
    TrophyType(String displayName,Material mat,Enchantment ench){
         this(displayName,mat,"",ench);
    }
    TrophyType(String displayName,Material mat,String lore){
         this(displayName,mat,lore,Enchantment.LOOT_BONUS_BLOCKS);
    }
    TrophyType(String displayName,Material mat,String lore,Enchantment ench){
        dropName=displayName;
        dropMaterial=mat;
        dropEnchantment=ench;
        dropLore=lore;
    }
    
    
    public Material getBlockMaterial(){
        return Material.valueOf(name().toUpperCase());
    }
    public String getBlockName(){
        String spaced = name().replace("_", " ").toLowerCase();
        return camelCase(spaced);
    }
    public String getShortName(){
        return name().replace("_", "").toLowerCase();
    }
    public String getDropConfigName(){
        return getShortName()+"droprate";
    }
    
    
    public static TrophyType get(Material mat){
        TrophyType type;
        try{
            type = valueOf(mat.name().toUpperCase());
        }catch(Exception e){
            type = null;
        }
        return type;
    }
    
    public ItemStack createDrop(){
        ItemStack stack = new ItemStack(dropMaterial,1);
        stack.addUnsafeEnchantment(dropEnchantment, 1);
        ItemMeta meta = stack.getItemMeta();
        
        meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + dropName);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RESET+""+ChatColor.ITALIC+dropLore);
        lore.add("");
        lore.add(ChatColor.RESET+getBlockName()+" "+ChatColor.BLUE+""+ChatColor.ITALIC+"Mining Trophy");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        //meta.addEnchant(dropEnchantment, 1, true);
        
        stack.setItemMeta(meta);
        return stack;
    }
    private static String camelCase(String str)
    {
        StringBuilder builder = new StringBuilder(str);
        // Flag to keep track if last visited character is a 
        // white space or not
        boolean isLastSpace = true;

        // Iterate String from beginning to end.
        for(int i = 0; i < builder.length(); i++)
        {
                char ch = builder.charAt(i);

                if(isLastSpace && ch >= 'a' && ch <='z')
                {
                        // Character need to be converted to uppercase
                        builder.setCharAt(i, (char)(ch + ('A' - 'a') ));
                        isLastSpace = false;
                }else if (ch != ' ')
                        isLastSpace = false;
                else
                        isLastSpace = true;
        }

        return builder.toString();
    }
}
