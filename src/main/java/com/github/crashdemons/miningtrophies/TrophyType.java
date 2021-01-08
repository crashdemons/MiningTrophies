/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/ .
 */
package com.github.crashdemons.miningtrophies;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    SEA_LANTERN("Singing Shard",Material.PRISMARINE_SHARD,"Hums with a strange energy."),
    ICE("Shimmering Water",Material.POTION)
    ;
    
    private String dropName;
    private String dropLore;
    private Material dropMaterial;
    private Enchantment dropEnchantment;
    private static final int TPS=20;
    
    
    private static HashMap<String,TrophyType> loreReference = new HashMap<>();
    
    
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
    
    public String getDropName(){
        return dropName;
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
    
    private static boolean isStainedGlassBlock(Material mat){//note: regular glass is not included and is a fallthrough/default for the glass trophy
        if(mat.isBlock()){
            String matname = mat.name().toUpperCase();
            return matname.contains("STAINED_GLASS") && !matname.contains("PANE");
        }
        return false;
    }
    
    private static boolean isIceVariant(Material mat){//regular ice is excluded
        if(mat.isBlock()){
            if(mat==Material.FROSTED_ICE) return false;//explicitly deny this
            if(mat==Material.BLUE_ICE || mat==Material.PACKED_ICE) return true;
        }
        return false;
    }
    
    
    /**
     * Get the trophytype corresponding to a provided block material
     * @param mat the material of a block to drop a trophy when mined
     * @return The type of trophy for that block material, or null if none was found.
     */
    public static TrophyType get(Material mat){
        if(isStainedGlassBlock(mat)) mat=Material.GLASS;//consider all glass blocks as normal glass blocks.
        if(isIceVariant(mat)) mat=Material.ICE;
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
    public ItemStack createDrop(){ return createDrop(true,true,true); }
    
   public String getIdentifyingLore(){
        return ChatColor.RESET+""+ChatColor.DARK_PURPLE+getBlockName()+" "+ChatColor.BLUE+""+ChatColor.ITALIC+"Mining Trophy";
    }
   
   
   private static void createLoreCache(){
        for(TrophyType type : TrophyType.values()){
            String strippedLore = ChatColor.stripColor(type.getIdentifyingLore());
            loreReference.put(strippedLore,type);
        }
   }

   public static TrophyType identifyTrophyLore(String loreLine){
       loreLine = ChatColor.stripColor(loreLine);
       return loreReference.get(loreLine);
   }
   public static TrophyType identifyTrophyItem(ItemStack stack){
        synchronized(loreReference){
            if(loreReference.isEmpty()) createLoreCache();
        }
        if(!stack.hasItemMeta()){
            return null;
        }
        ItemMeta meta = stack.getItemMeta();
        if(!meta.hasLore()){
            return null;
        }
        List<String> lore = meta.getLore();
        for(String loreLine : lore){
            TrophyType type = identifyTrophyLore(loreLine);
            if(type!=null) return type;
        }
        return null;
    }
    
    public List<String> getLore(){
        ArrayList<String> lore = new ArrayList<>();
        if(!dropLore.isEmpty()) lore.add(ChatColor.RESET+""+ChatColor.DARK_PURPLE+ChatColor.ITALIC+dropLore);
        lore.add(getIdentifyingLore());
        return lore;
    }
    
    /**
     * Creates a new itemstack of this trophy type
     * @param addenchants controls whether to add default enchantments to the trophy item
     * @param addeffects controls whether to add default potion effects to the trophy item
     * @param addlore controls whether to add lore-text to the trophy item
     * @return the itemstack
     */
    public ItemStack createDrop(boolean addenchants, boolean addeffects, boolean addlore){
        ItemStack stack = new ItemStack(dropMaterial,1);
        if(addenchants) stack.addUnsafeEnchantment(dropEnchantment, 1);
        ItemMeta meta = stack.getItemMeta();
        
        meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + dropName);
        if(addlore){
            meta.setLore(getLore());
        }
        if(addenchants) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        if(dropMaterial==Material.POTION){
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            PotionMeta potMeta = (PotionMeta) meta;
            potMeta.setColor(Color.BLUE);
            if(addeffects){
                potMeta.addCustomEffect(new PotionEffect(PotionEffectType.GLOWING,300*TPS,1),true);
                potMeta.addCustomEffect(new PotionEffect(PotionEffectType.SATURATION,3*TPS,10),false);
            }
        }
        
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
