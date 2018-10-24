/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.crashdemons.miningtrophies.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * An event used by the plugin to detect if a hypothetical BlockBreak would be cancelled, potentially from protection plugins.
 * 
 * Generally, this should not be used unless entirely necessary.
 * @author crash
 */
public class SimulatedBlockBreakEvent extends BlockBreakEvent {
    public SimulatedBlockBreakEvent(Block block, Player player) {
        super(block, player);
    }
}
