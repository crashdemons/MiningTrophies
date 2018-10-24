/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.crashdemons.miningtrophies.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Defines a mining-trophy drop event that occurs when a player mines a block and receives a trophy.
 * @author crash
 */
public class BlockDropTrophyEvent extends BlockEvent implements Cancellable{
    
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemStack drop;
    private boolean cancelled;
    
    /**
     * Constructs a mining-trophy drop event
     * @param theBlock the block that was mined to receive the trophy
     * @param miningPlayer the player that did the mining
     * @param droppedTrophy the trophy itemstack being dropped
     */
    public BlockDropTrophyEvent(Block theBlock, Player miningPlayer, ItemStack droppedTrophy) {
        super(theBlock);
        this.player=miningPlayer;
        this.drop=droppedTrophy;
    }
    
    /**
     * The trophy itemstack being dropped
     * @return 
     */
    public ItemStack getDrop(){
        return drop;
    }
    
    /**
     * The player that mine the block, causing the trophy event
     * @return 
     */
    public Player getPlayer(){
        return player;
    }
    
    /**
     * Set whether the event should be cancelled.
     * @param state the state to change the cancellation to.
     */
    @Override
    public void setCancelled(boolean state){
        cancelled=state;
    }
    
    /**
     * Check whether the event is cancelled.
     * @return the cancellation state of the event.
     */
    @Override
    public boolean isCancelled(){
        return cancelled;
    }
    
    /**
     * Get a list of handlers for the event type
     * @return the list of handlers
     */
    @Override
    public HandlerList getHandlers(){
        return handlers;
    }
    
    /**
     * Get a list of handlers for the event type
     * @return the list of handlers
     */
    public static HandlerList getHandlerList(){
        return handlers;
    }
}
