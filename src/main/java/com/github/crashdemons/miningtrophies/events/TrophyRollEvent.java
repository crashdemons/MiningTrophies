/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/ .
 */
package com.github.crashdemons.miningtrophies.events;

import com.github.crashdemons.miningtrophies.modifiers.DropRateModifier;
import com.github.crashdemons.miningtrophies.modifiers.DropRateModifierType;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.plugin.Plugin;

/**
 * Event created by MiningTrophies (0.7.2+) to indicate that a trophy dropchance roll has occurred and the success/failure has been determined.
 * This event allows third-party plugin authors to analyze and modify drop chance success with all factors considered by MiningTrophies available.
 * If the success of this event is set to false, no trophy will be dropped. If it is set to true, a trophy will be dropped.
 * @author crashdemons (crashenator at gmail.com)
 */
public class TrophyRollEvent extends BlockEvent {
    private static final HandlerList handlers = new HandlerList();
    
    private final LinkedHashMap<String, DropRateModifier> modifiers = new LinkedHashMap<>();

    private final Entity miner;
    private final Block target;
    private final Material material;

    private final boolean minerAlwaysRewarded;
    //private final double fortuneModifier;
    
    private final double originalDropRoll;
    private double effectiveDropRoll;
    private final double originalDropRate;
    private double effectiveDropRate;
    
    private boolean dropSuccess;

    /**
     * Creates the Trophy dropchance event for MiningTrophies.
     * @param miner the Entity mining the block
     * @param target the Block being mined
     * @param minerAlwaysRewarded whether the miner has the always-rewarded permission
     * @param originalDropRoll the randomized PRNG double droproll value inclusively between 0 to 1.
     * @param originalDropRate the configured droprate of the target as a fraction (0.01 = 1%)
     */
    public TrophyRollEvent(Entity miner, Block target, boolean minerAlwaysRewarded, double originalDropRoll, double originalDropRate) {
        super(target);
        this.originalDropRate=originalDropRate;
        this.effectiveDropRate=100;
        this.dropSuccess=false;
        this.effectiveDropRoll=100;
        this.originalDropRoll=originalDropRoll;
        this.minerAlwaysRewarded=minerAlwaysRewarded;
        
        this.miner=miner;
        this.target=target;
        this.material=block.getType();
    }
    
    
    /**
     * Creates the Trophy dropchance event for MiningTrophies.
     * @param miner the Entity mining the block
     * @param target the Block being mined
     * @param minerAlwaysRewarded whether the miner has the always-rewarded permission
     * @param originalDropRoll the randomized PRNG double droproll value inclusively between 0 to 1.
     * @param effectiveDropRoll the modified droproll value after permission logic was applied (alwaysrewarded sets to 0)
     * @param originalDropRate the configured droprate of the target as a fraction (0.01 = 1%)
     * @param effectiveDropRate the effective droprate of the target as a fraction (0.01 = 1%), as modified by fortune.
     * @param dropSuccess whether the droproll was determined to be initially a successful roll.
     */
    public TrophyRollEvent(Entity miner, Block target, boolean minerAlwaysRewarded, double originalDropRoll, double effectiveDropRoll, double originalDropRate, double effectiveDropRate, boolean dropSuccess) {
        super(target);
        this.originalDropRate=originalDropRate;
        this.effectiveDropRate=effectiveDropRate;
        this.dropSuccess=dropSuccess;
        this.effectiveDropRoll=effectiveDropRoll;
        this.originalDropRoll=originalDropRoll;
        this.minerAlwaysRewarded=minerAlwaysRewarded;
        
        this.miner=miner;
        this.target=target;
        this.material=block.getType();
    }
    
    /**
     * Creates the Trophy dropchance event for MiningTrophies.
     * @param miner the Entity mining the block
     * @param target the Block being mined
     * @param minerAlwaysRewarded whether the miner has the always-rewarded permission
     * @param originalDropRoll the randomized PRNG double droproll value inclusively between 0 to 1.
     * @param fortuneModifier the fractional probability modifier (greater than or equal to 1.0) of fortune, as applied by MiningTrophies to the effective droprate.
     * @param effectiveDropRoll the modified droproll value after permission logic was applied (alwaysrewarded sets to 0)
     * @param originalDropRate the configured droprate of the target as a fraction (0.01 = 1%)
     * @param effectiveDropRate the effective droprate of the target as a fraction (0.01 = 1%), as modified by fortune.
     * @param dropSuccess whether the droproll was determined to be initially a successful roll.
     */
    @Deprecated
    public TrophyRollEvent(Entity miner, Block target, boolean minerAlwaysRewarded, double fortuneModifier, double originalDropRoll, double effectiveDropRoll, double originalDropRate, double effectiveDropRate, boolean dropSuccess) {
        super(target);
        this.originalDropRate=originalDropRate;
        this.effectiveDropRate=effectiveDropRate;
        this.dropSuccess=dropSuccess;
        this.effectiveDropRoll=effectiveDropRoll;
        this.originalDropRoll=originalDropRoll;
        this.minerAlwaysRewarded=minerAlwaysRewarded;
        
        setModifier("fortune", new DropRateModifier(DropRateModifierType.MULTIPLY, fortuneModifier));
        
        this.miner=miner;
        this.target=target;
        this.material=block.getType();
    }
    
    /**
     * Gets the fortune modifier (multiplier) that was applied to the effective droprate. This is generally 1 (no effect) or greater.
     * @return the fortune modifier
     */
    @Deprecated
    public double getFortuneModifier(){
        DropRateModifier modifier = getModifier("fortune");
        if (modifier == null) {
            return 1;
        }
        return modifier.getMultiplierValue();
    }
    

    /**
     * Get the Miner's entity that may have done the mining.
     * @return the entity of the miner, or null if the miner was a mob.
     */
    public Entity getMiner() {
        return miner;
    }

    /**
     * Get the Target's block that may have been mined
     * @return the entity of the target
     */
    public Block getTarget() {
        return target;
    }
    public Material getTargetMaterial() {
        return material;
    }

    /**
     * Gets whether the miner was configured to always be rewarded this type of target.
     * If this is true, the effective droproll may have been set to 0 to force success.
     * @return Whether the miner was configured to always be rewarded
     */
    public boolean getMinerAlwaysRewarded() {
        return minerAlwaysRewarded;
    }

    /**
     * Gets the original PRNG-generated random value of the drop roll, uniform between 0 and 1 inclusively.
     * When this value is lower than the droprate value by chance, the roll is considered successful.
     * @return the drop roll value in the range [0,1]
     */
    public double getOriginalDropRoll() {
        return originalDropRoll;
    }
    
    /**
     * Gets the effective drop roll value after modification by MiningTrophies.
     * The droproll will normally be reflected by the original random droproll, except if the miner always is rewarded, then this may be 0.
     * If this is below the droprate, the roll would have been determined to be a success.
     * @return the effective drop roll.
     * @see #getOriginalDropRoll
     */
    public double getEffectiveDropRoll() {
        return effectiveDropRoll;
    }

    /**
     * Gets the configured droprate for the target as a fractional probability, unmodified.
     * @return the droprate
     */
    public double getOriginalDropRate() {
        return originalDropRate;
    }
    /**
     * Gets the configured droprate for the target as a fractional probability, after modification by fortune.
     * @return the droprate
     */
    public double getEffectiveDropRate() {
        return effectiveDropRate;
    }

    /**
     * Whether the effective drop roll was determined to be a success.
     * @return the success of the drop roll
     */
    public boolean getDropSuccess() {
        return dropSuccess;
    }

    /**
     * Sets whether the drop roll should be considered a success.
     * @param value whether the trophy drop should succeed or fail.
     */
    public void setDropSuccess(boolean value) {
        dropSuccess = value;
    }

    /**
     * Whether the effective drop roll was determined to be a success.
     * Alias of getDropSuccess
     * @see #getDropSuccess() 
     * @return the success of the drop roll
     */
    public boolean succeeded() {
        return getDropSuccess();
    }
    
    ////////////////////////////////////////////
    

    /**
     * Gets the list of modifiers to the effective droprate. This map will be in
     * order that the modifiers are applied.
     *
     * @since 5.2.16-SNAPSHOT
     * @return map containing the droprate modifiers by name.
     */
    public Map<String, DropRateModifier> getModifiers() {
        return modifiers;
    }

    /**
     * Re-apply the current effective droproll and effective droprate values to
     * make a new determination of the trophy drop's success. Modifiers are not
     * considered by this method, only the two effective values. Note: if
     * killerAlwaysBetrophys is enabled, the effective droproll will be set to 0.
     * 
     * This is equivalent to recalculateSuccess(false).
     *
     * @since 5.2.16-SNAPSHOT
     */
    public void applyDropRate() {
        if (minerAlwaysRewarded) {
            effectiveDropRoll = 0;
        }
        this.setDropSuccess(effectiveDropRoll < effectiveDropRate);
    }

    /**
     * Re-apply all droprate modifiers to the original droprate and recalculate
     * the effective droprate. This method will discard the current effective
     * droprate, if you want to retain the original values, you should copy them
     * before calling this method. Success is not updated by this method.
     *
     * @since 5.2.16-SNAPSHOT
     */
    public void applyModifiers() {
        effectiveDropRate = originalDropRate;
        for (DropRateModifier modifier : modifiers.values()) {
            effectiveDropRate = modifier.apply(effectiveDropRate);
        }
    }

    /**
     * Re-apply all factors (droprate modifiers then effective values) to
     * determine suuccess. This method will discard the current effective
     * droprate, if you want to retain the original values, you should copy them
     * before calling this method.
     * 
     * This is equivalent to recalculateSuccess(true) or applyModifiers();applyDropRate();
     *
     * @since 5.2.16-SNAPSHOT
     */
    public void recalculateSuccess() {
        applyModifiers();
        applyDropRate();
    }
    /**
     * Re-apply all relevant factors (droprate modifiers then effective values) to
     * determine suuccess. This method will discard the current effective
     * droprate (if set), if you want to retain the original values, you should copy them
     * before calling this method.
     * 
     * When the applyModifiers is true, this is equivalent to recalculateSuccess() or applyModifiers();applyDropRate().
     * When the applyModifiers is false, this is equivalent to applyDropRate().
     * 
     * @param applyModifiers If this is set, the current effective droprate will be discarded and recalculated from modifiers.
     *
     * @since 5.2.17-SNAPSHOT
     */
    public void recalculateSuccess(boolean applyModifiers) {
        if(applyModifiers) applyModifiers();
        applyDropRate();
    }
    

    /**
     * Retrieve the value of a modifier of the effective droprate. Note: this
     * value does not impact calculations or success and is for you to use as a
     * courtesy to other plugins at this point. This method can retrieve both
     * internal and custom plugin modifiers (if the prefix is included).
     *
     * @since 5.2.16-SNAPSHOT
     * @param modifierName the name of the modifier
     * @return the value of the modifier, or null if it is not present.
     */
    public DropRateModifier getModifier(final String modifierName) {
        return modifiers.get(modifierName);
    }

    /**
     * Sets a note about an internal modifier of the effective droprate. Note:
     * this value does not impact calculations or success unless applyModifiers+applyDropRate or recalculateSuccess 
     * is called Note: new modifies are generally applied AFTER other
     * modifiers<br>
     *
     * @deprecated using this method to modify existing modifiers should be
     * avoided - use setCustomModifier to note new ones.
     * @since 5.2.16-SNAPSHOT
     * @param modifierName the name of the modifier to set.
     * @param value the value of the modifier to set
     */
    public void setModifier(final String modifierName, final DropRateModifier value) {
        modifiers.put(modifierName, value);
    }

    /**
     * Replaces notes about an internal modifiers of the effective droprate.
     * Note: this value does not impact calculations or success unless
     * applyModifiers+applyDropRate or recalculateSuccess is called Note: new modifies are generally applied AFTER
     * other modifiers; this method will overwrite existing modifiers.<br>
     *
     * @deprecated using this method to modify existing modifiers should be
     * avoided - use setCustomModifier to note new ones.
     * @since 5.2.16-SNAPSHOT
     * @param entries the modifiers to set
     */
    public void setModifiers(final Map<String, DropRateModifier> entries) {
        modifiers.clear();
        modifiers.putAll(entries);
    }

    /**
     * Constructs the internal name of a custom droprate modifier, provided the
     * name of the plugin and modifier.
     *
     * @since 5.2.16-SNAPSHOT
     * @param pluginName The name of the plugin that added the modifier
     * @param modifierName The name of the modifier
     * @return the internal name of the modifier;
     */
    public static String getCustomModifierName(final String pluginName, final String modifierName) {
        return pluginName + ":" + modifierName;
    }

    /**
     * Add or change a note about your custom modifier to the trophy-roll event.
     * Note: this value does not impact calculations unless applyModifiers+applyDropRate or recalculateSuccess is called.<br>
     * Note: the name of the modifier will be prepended with "PluginName:"
     * depending on your plugin's name.<br>
     * Note: new modifies are generally applied AFTER other modifiers<br>
     *
     * @since 5.2.16-SNAPSHOT
     * @param yourPlugin the plugin adding the modifier
     * @param modifierName the name of the modifier, excluding any prefix
     * @param modifierValue the value of the modifier
     */
    public void setCustomModifier(final Plugin yourPlugin, final String modifierName, final DropRateModifier modifierValue) {
        String customModifierName = getCustomModifierName(yourPlugin.getName(), modifierName);
        modifiers.put(customModifierName, modifierValue);
    }
    /**
     * Add or change a note about your custom modifier to the trophy-roll event.
     * Note: this value does not impact calculations unless applyModifiers+applyDropRate or recalculateSuccess is called.<br>
     * Note: the name of the modifier will be prepended with "PluginName:"
     * depending on your plugin's name.<br>
     * Note: new modifies are generally applied AFTER other modifiers<br>
     *
     * @since 5.2.17-SNAPSHOT
     * @param yourPlugin the plugin adding the modifier
     * @param modifierName the name of the modifier, excluding any prefix
     * @param modifierValue the value of the modifier
     * @param recalculateSuccess whether to force recalculation of success by applying this modifier. Note: this will erase any changes to the effective droprate.
     */
    public void setCustomModifier(final Plugin yourPlugin, final String modifierName, final DropRateModifier modifierValue, boolean recalculateSuccess) {
        String customModifierName = getCustomModifierName(yourPlugin.getName(), modifierName);
        modifiers.put(customModifierName, modifierValue);
        if(recalculateSuccess) recalculateSuccess();
    }

    /**
     * Gets a custom (plugin-added) modifier to the trophy-roll event. Note: this
     * value does not impact calculations or success and is for you to use as a
     * courtesy to other plugins at this point. Note: the name of the modifier
     * will be prepended with "PluginName:" depending on your plugin's name.
     *
     * @since 5.2.16-SNAPSHOT
     * @param yourPlugin the plugin which added the modifier
     * @param modifierName the name of the modifier, excluding any prefix
     * @return the value of the modifier, or the null if it is not found.
     */
    public DropRateModifier getCustomModifier(final Plugin yourPlugin, final String modifierName) {
        return getCustomModifier(yourPlugin.getName(), modifierName);
    }

    /**
     * Gets a custom (plugin-added) modifier to the trophy-roll event. Note: this
     * value does not impact calculations or success and is for you to use as a
     * courtesy to other plugins at this point. Note: the name of the modifier
     * will be prepended with "PluginName:" depending on your plugin's name.
     *
     * @since 5.2.16-SNAPSHOT
     * @param yourPluginName the plugin name which added the modifier
     * @param modifierName the name of the modifier, excluding any prefix
     * @return the value of the modifier, or the null if it is not found.
     */
    public DropRateModifier getCustomModifier(final String yourPluginName, final String modifierName) {
        String customModifierName = getCustomModifierName(yourPluginName, modifierName);
        return getModifier(customModifierName);
    }


    /**
     * Sets the effective droproll value for the event. Note: this value will
     * not impact the success value or calculations unless applyDropRate() or
     * recalculateSuccess() is called.
     *
     * @since 5.2.16-SNAPSHOT
     * @param effectiveRoll the value between 0.0 and 1.0 to use as the drop
     * roll.
     */
    public void setEffectiveDropRoll(final double effectiveRoll) {
        this.effectiveDropRoll = effectiveRoll;
    }

    /**
     * Sets the effective droprate value for the event.
     * Note: this value will be overwritten by applyModifiers and recalculateSuccess methods and does not impact the success of the event unless you run applyDropRate().
     * Since other plugins are likely to overwrite this change, you are strongly recommended to use a Modifier instead (which is not likely to be erased) to get the result you want.
     *
     * @deprecated this value is very likely to be overwritten by other plugins (or applyModifiers/recalculateSuccess) - use setCustomModifier instead.
     * @since 5.2.16-SNAPSHOT
     * @param effectiveRate the effective droprate/fractional-chance value to set (0.0-1.0 inclusive)
     */
    public void setEffectiveDropRate(final double effectiveRate) {
        this.effectiveDropRate = effectiveRate;
    }
    
    ////////////////////////////////////////////
    

    /**
     * Get a list of handlers for the event.
     *
     * @return a list of handlers for the event
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Get a list of handlers for the event.
     *
     * @return a list of handlers for the event
     */
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
