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
 *
 * @author crash
 */
public class SimulatedBlockBreakEvent extends BlockBreakEvent {
    public SimulatedBlockBreakEvent(Block block, Player player) {
        super(block, player);
    }
}
