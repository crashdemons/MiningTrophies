/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/ .
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
    /**
     * Constructs a simulated blockbreak event
     * @param block the block to hypothetically break
     * @param player the player doing the breaking
     */
    public SimulatedBlockBreakEvent(Block block, Player player) {
        super(block, player);
    }
}
