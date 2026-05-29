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

public class MainMenu {

    public static void open(MinecraftClient client, String targetUser) {
        if (client.player == null) return;

        SimpleInventory inv = new SimpleInventory(27);
        for (int i = 0; i < 27; i++) com.erneto.client.util.ItemHelper.createGuiItem(inv, i, Items.GRAY_STAINED_GLASS_PANE, "§8 ");

        ItemHelper.createGuiItem(inv, 10, Items.PAPER, "§7chat: §f" + targetUser);
        ItemHelper.createGuiItem(inv, 11, Items.COMMAND_BLOCK_MINECART, "§7commands: §f" + targetUser);
        ItemHelper.createGuiItem(inv, 12, Items.BOOK, "§7history: §f" + targetUser);
        ItemHelper.createGuiItem(inv, 13, Items.MAP, "§7dupeip: §f" + targetUser);
        ItemHelper.createGuiItem(inv, 14, Items.NETHERITE_HOE, "§7staff menu: §f" + targetUser);
        ItemHelper.createGuiItem(inv, 15, Items.SPAWNER, "§7co l a:+item include:spawner t:1h " + targetUser);
        ItemHelper.createGuiItem(inv, 16, Items.ENDER_CHEST, "§7ec: §f" + targetUser);

        client.player.playSound(SoundEvents.BLOCK_VAULT_ACTIVATE, 0.5f, 2.0f);

        client.setScreen(new GenericContainerScreen(
                new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, 0, client.player.getInventory(), inv, 3),
                client.player.getInventory(),
                Text.literal("§8dmc: §f" + targetUser)
        ) {
            @Override
            protected void onMouseClick(Slot slot, int id, int btn, SlotActionType act) {
                if (slot == null || !slot.hasStack() || slot.getStack().isOf(Items.GRAY_STAINED_GLASS_PANE)) return;

                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, 1.2f);

                String cmd = switch (slot.getIndex()) {
                    case 10 -> "co l a:chat u:" + targetUser;
                    case 11 -> "co l a:command u:" + targetUser;
                    case 12 -> "history " + targetUser;
                    case 13 -> "dupeip " + targetUser;
                    case 14 -> "staffmenu " + targetUser;
                    case 15 -> "co l a:+item include:spawner t:1h " + targetUser;
                    case 16 -> "ec " + targetUser;
                    default -> null;
                };

                if (cmd != null) {
                    ItemHelper.sendCommand(client, cmd);
                    this.close();
                }
            }
        });
    }
}