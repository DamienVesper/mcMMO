package com.gmail.nossr50.skills.excavation;

import com.gmail.nossr50.MMOTestEnvironment;
import com.gmail.nossr50.api.exceptions.InvalidSkillException;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.datatypes.treasure.ExcavationTreasure;
import com.gmail.nossr50.util.skills.RankUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ExcavationTest extends MMOTestEnvironment {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ExcavationTest.class.getName());


    @BeforeEach
    void setUp() throws InvalidSkillException {
        mockBaseEnvironment(logger);
        when(rankConfig.getSubSkillUnlockLevel(SubSkillType.EXCAVATION_ARCHAEOLOGY, 1)).thenReturn(1);
        when(rankConfig.getSubSkillUnlockLevel(SubSkillType.EXCAVATION_GIGA_DRILL_BREAKER, 1)).thenReturn(1);

        // wire advanced config

        when(RankUtils.getRankUnlockLevel(SubSkillType.EXCAVATION_ARCHAEOLOGY, 1)).thenReturn(1); // needed?
        when(RankUtils.getRankUnlockLevel(SubSkillType.EXCAVATION_GIGA_DRILL_BREAKER, 1)).thenReturn(1); // needed?
        when(RankUtils.hasReachedRank(eq(1), any(Player.class), eq(SubSkillType.EXCAVATION_ARCHAEOLOGY))).thenReturn(true);
        when(RankUtils.hasReachedRank(eq(1), any(Player.class), eq(SubSkillType.EXCAVATION_GIGA_DRILL_BREAKER))).thenReturn(true);

        // setup player and player related mocks after everything else
        this.player = Mockito.mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerUUID);

        // wire inventory
        this.playerInventory = Mockito.mock(PlayerInventory.class);
        this.itemInMainHand = new ItemStack(Material.DIAMOND_SHOVEL);
        when(player.getInventory()).thenReturn(playerInventory);
        when(playerInventory.getItemInMainHand()).thenReturn(itemInMainHand);

        // Set up spy for Excavation Manager

    }

    @AfterEach
    void tearDown() {
        cleanupBaseEnvironment();
    }

    @Test
    void excavationShouldHaveTreasureDrops() {
        mmoPlayer.modifySkill(PrimarySkillType.EXCAVATION, 1000);

        // Wire block
        BlockState blockState = Mockito.mock(BlockState.class);
        BlockData blockData = Mockito.mock(BlockData.class);
        Block block = Mockito.mock(Block.class);
        when(blockState.getBlockData()).thenReturn(blockData);
        when(blockState.getType()).thenReturn(Material.SAND);
        when(blockData.getMaterial()).thenReturn(Material.SAND);
        when(blockState.getBlock()).thenReturn(block);
        when(blockState.getBlock().getDrops(any())).thenReturn(null);

        ExcavationManager excavationManager = Mockito.spy(new ExcavationManager(mmoPlayer));
        doReturn(getGuaranteedTreasureDrops()).when(excavationManager).getTreasures(blockState);
        excavationManager.excavationBlockCheck(blockState);

        // verify ExcavationManager.processExcavationBonusesOnBlock was called
        verify(excavationManager, atLeastOnce()).processExcavationBonusesOnBlock(any(BlockState.class), any(ExcavationTreasure.class), any(Location.class));
    }

    @Test
    void excavationShouldNotDropTreasure() {
        mmoPlayer.modifySkill(PrimarySkillType.EXCAVATION, 1000);

        // Wire block
        BlockState blockState = Mockito.mock(BlockState.class);
        BlockData blockData = Mockito.mock(BlockData.class);
        Block block = Mockito.mock(Block.class);
        when(blockState.getBlockData()).thenReturn(blockData);
        when(blockState.getType()).thenReturn(Material.SAND);
        when(blockData.getMaterial()).thenReturn(Material.SAND);
        when(blockState.getBlock()).thenReturn(block);
        when(blockState.getBlock().getDrops(any())).thenReturn(null);

        ExcavationManager excavationManager = Mockito.spy(new ExcavationManager(mmoPlayer));
        doReturn(getImpossibleTreasureDrops()).when(excavationManager).getTreasures(blockState);
        excavationManager.excavationBlockCheck(blockState);

        // verify ExcavationManager.processExcavationBonusesOnBlock was called
        verify(excavationManager, never()).processExcavationBonusesOnBlock(any(BlockState.class), any(ExcavationTreasure.class), any(Location.class));
    }

    private List<ExcavationTreasure> getGuaranteedTreasureDrops() {
        List<ExcavationTreasure> treasures = new ArrayList<>();;
        treasures.add(new ExcavationTreasure(new ItemStack(Material.CAKE), 1, 100, 1));
        return treasures;
    }

    private List<ExcavationTreasure> getImpossibleTreasureDrops() {
        List<ExcavationTreasure> treasures = new ArrayList<>();;
        treasures.add(new ExcavationTreasure(new ItemStack(Material.CAKE), 1, 0, 1));
        return treasures;
    }
}
