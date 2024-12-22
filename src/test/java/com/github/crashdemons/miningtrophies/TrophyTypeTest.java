/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/ .
 */
package com.github.crashdemons.miningtrophies;

import org.bukkit.Material;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author crashdemons (crashenator at gmail.com)
 */
public class TrophyTypeTest {
    
    public TrophyTypeTest() {
    }

    @Test
    public void testGet() {
        //TODO: fix tests - can't run these because enchantment enums now invoke the bukkit server registry.
        /*
        Material exp,act;
        exp = TrophyType.resolveOreVariant(Material.GOLD_ORE);
        act = Material.GOLD_ORE;
        assertEquals(exp,act);
        exp = TrophyType.resolveOreVariant(Material.DEEPSLATE_GOLD_ORE);
        act = Material.GOLD_ORE;
        assertEquals(exp,act);
        exp = TrophyType.resolveOreVariant(Material.NETHER_GOLD_ORE);
        act = Material.NETHER_GOLD_ORE;
        assertEquals(exp,act);
        exp = TrophyType.resolveOreVariant(Material.NETHER_QUARTZ_ORE);
        act = Material.NETHER_QUARTZ_ORE;
        assertEquals(exp,act);*/
    }

    
}
