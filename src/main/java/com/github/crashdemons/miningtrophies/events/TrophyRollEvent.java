/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/ .
 */
package com.github.crashdemons.miningtrophies.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

/**
 * Event created by MiningTrophies (0.7.2+) to indicate that a trophy dropchance roll has occurred and the success/failure has been determined.
 * This event allows third-party plugin authors to analyze and modify drop chance success with all factors considered by MiningTrophies available.
 * If the success of this event is set to false, no trophy will be dropped. If it is set to true, a trophy will be dropped.
 * @author crashdemons (crashenator at gmail.com)
 */
public class TrophyRollEvent extends BlockEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Entity miner;
    private final Block target;

    private final boolean minerAlwaysRewarded;
    private final double fortuneModifier;
    
    private final double originalDropRoll;
    private final double effectiveDropRoll;
    private final double originalDropRate;
    private final double effectiveDropRate;
    private boolean dropSuccess;

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
    public TrophyRollEvent(Entity miner, Block target, boolean minerAlwaysRewarded, double fortuneModifier, double originalDropRoll, double effectiveDropRoll, double originalDropRate, double effectiveDropRate, boolean dropSuccess) {
        super(target);
        this.fortuneModifier=fortuneModifier;
        this.originalDropRate=originalDropRate;
        this.effectiveDropRate=effectiveDropRate;
        this.dropSuccess=dropSuccess;
        this.effectiveDropRoll=effectiveDropRoll;
        this.originalDropRoll=originalDropRoll;
        this.minerAlwaysRewarded=minerAlwaysRewarded;
        
        this.miner=miner;
        this.target=target;
    }
    
    /**
     * Gets the fortune modifier (multiplier) that was applied to the effective droprate. This is generally 1 (no effect) or greater.
     * @return the fortune modifier
     */
    public double getFortuneModifier(){
        return fortuneModifier;
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
