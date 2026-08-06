package com.erneto.client.gui;

import com.erneto.client.util.ItemHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainMenu {

    private record SlotAction(String cmd, String label) {
    }

    private static Map<Integer, SlotAction> buildActions(String user) {
        Map<Integer, SlotAction> actions = new HashMap<>();
        actions.put(10, new SlotAction("co l a:chat u:" + user, "chat"));
        actions.put(11, new SlotAction("co l a:command u:" + user, "commands"));
        actions.put(12, new SlotAction("history " + user, "history"));
        actions.put(13, new SlotAction("dupeip " + user, "dupeip"));
        actions.put(14, new SlotAction("staffmenu " + user, "staff menu"));
        actions.put(15, new SlotAction("co l a:+item include:spawner t:1h u:" + user, "spawner log"));
        actions.put(16, new SlotAction("ec " + user, "enderchest"));
        actions.put(19, new SlotAction("punish " + user, "punish"));
        actions.put(20, new SlotAction("invsee " + user, "invsee"));
        actions.put(21, new SlotAction("balance " + user, "money"));
        actions.put(25, new SlotAction("tp " + user, "teleport"));
        return actions;
    }

    public static void open(MinecraftClient client, String targetUser) {
        if (client.player == null) return;

        SimpleInventory inv = new SimpleInventory(27);
        Map<Integer, SlotAction> actions = buildActions(targetUser);

        for (int i = 0; i < 27; i++) {
            ItemHelper.createGuiItem(inv, i, Items.GRAY_STAINED_GLASS_PANE, "§8 ");
        }

        ItemHelper.createGuiItem(inv, 10, Items.PAPER,
                "§b» §fchat", List.of("§7co l a:chat", "§8u:" + targetUser));
        ItemHelper.createGuiItem(inv, 11, Items.COMMAND_BLOCK_MINECART,
                "§b» §fcommands", List.of("§7co l a:command", "§8u:" + targetUser));
        ItemHelper.createGuiItem(inv, 12, Items.BOOK,
                "§b» §fhistory", List.of("§7history " + targetUser));
        ItemHelper.createGuiItem(inv, 13, Items.MAP,
                "§b» §fdupeip", List.of("§7dupeip " + targetUser));
        ItemHelper.createGuiItem(inv, 14, Items.NETHERITE_HOE,
                "§b» §fstaff menu", List.of("§7staffmenu " + targetUser));
        ItemHelper.createGuiItem(inv, 15, Items.SPAWNER,
                "§b» §fspawner log", List.of("§7co l a:+item include:spawner", "§7t:1h u:" + targetUser));
        ItemHelper.createGuiItem(inv, 16, Items.ENDER_CHEST,
                "§b» §fenderchest", List.of("§7ec " + targetUser));

        ItemHelper.createGuiItem(inv, 19, Items.IRON_SWORD,
                "§c» §fpunish", List.of("§7punish " + targetUser, "§8click to execute"));
        ItemHelper.createGuiItem(inv, 20, Items.CHEST,
                "§b» §finvsee", List.of("§7invsee " + targetUser, "§8click to execute"));
        ItemHelper.createGuiItem(inv, 21, Items.GOLD_INGOT,
                "§b» §fmoney", List.of("§7balance " + targetUser, "§8click to execute"));

        ItemHelper.createGuiItem(inv, 25, Items.ENDER_PEARL,
                "§a» §fteleport", List.of("§7tp " + targetUser, "§8click to execute"));

        client.player.playSound(SoundEvents.BLOCK_VAULT_ACTIVATE, 0.5f, 2.0f);

        client.setScreen(new GenericContainerScreen(
                new GenericContainerScreenHandler(
                        ScreenHandlerType.GENERIC_9X3, 0,
                        client.player.getInventory(), inv, 3),
                client.player.getInventory(),
                Text.literal("§8[dmc] §f" + targetUser)
        ) {
            @Override
            protected void onMouseClick(Slot slot, int id, int btn, SlotActionType act) {
                if (slot == null || !slot.hasStack()) return;
                if (slot.getStack().isOf(Items.GRAY_STAINED_GLASS_PANE)) return;

                SlotAction action = actions.get(slot.getIndex());
                if (action == null) return;

                assert client != null;
                assert client.player != null;

                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, 1.2f);

                if (btn == 1) {
                    ItemHelper.copyToClipboard(client, action.cmd());
                    client.inGameHud.setOverlayMessage(
                            Text.literal("§7Copiado: §f/" + action.cmd()), false);
                    return;
                }

                ItemHelper.sendCommand(client, action.cmd());
                client.inGameHud.setOverlayMessage(
                        Text.literal("§b→ §f" + action.label() + " §7» §e" + targetUser), false);
                this.close();
            }
        });
    }
}