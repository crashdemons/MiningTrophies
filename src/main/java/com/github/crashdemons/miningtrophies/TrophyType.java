/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/ .
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
 * An enum defining each supported trophy type, with all necessary information to create it.
 * 
 * Note: generally each enum entry should correspond to a Material type of a block that can be broken.
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
    GLASS("What-a-pane",Material.GLASS_PANE,"For the experienced griefer."),
    COAL_ORE("Fuming Coal",Material.COAL),
    SEA_LANTERN("Shimmering Shard",Material.PRISMARINE_SHARD,"Flickers with a strange energy.")
    ;
    
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
    
    /**
     * Get the material of the block that would drop this trophy
     * @return the block material
     */
    public Material getBlockMaterial(){
        return Material.valueOf(name().toUpperCase());
    }
    
    /**
     * Get the human-readable name of the block that drops this trophy
     * @return the block name
     */
    public String getBlockName(){
        String spaced = name().replace("_", " ").toLowerCase();
        return camelCase(spaced);
    }
    
    /**
     * get a shortened name name (lowercase, without underscores) of this enum value.
     * @return the shortened name
     */
    public String getShortName(){
        return name().replace("_", "").toLowerCase();
    }
    
    /**
     * get the config entry key that corresponds to the droprate for this trophy
     * @return the config key name
     */
    public String getDropConfigName(){
        return getShortName()+"droprate";
    }
    
    /**
     * Get the trophytype corresponding to a provided block material
     * @param mat the material of a block to drop a trophy when mined
     * @return The type of trophy for that block material, or null if none was found.
     */
    public static TrophyType get(Material mat){
        TrophyType type;
        try{
            type = valueOf(mat.name().toUpperCase());
        }catch(Exception e){
            type = null;
        }
        return type;
    }
    
    /**
     * Creates a new itemstack of this trophy type
     * @return the itemstack
     */
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
