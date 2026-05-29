package com.erneto.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class ItemHelper {

    public static void createGuiItem(SimpleInventory inv, int slot, Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
        inv.setStack(slot, stack);
    }

    public static void createGuiItem(SimpleInventory inv, int slot, Item item, String name, List<String> lore) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
        if (!lore.isEmpty()) {
            stack.set(DataComponentTypes.LORE, new net.minecraft.component.type.LoreComponent(
                    lore.stream().map(s -> (Text) Text.literal(s)).toList()
            ));
        }
        inv.setStack(slot, stack);
    }

    public static void sendCommand(MinecraftClient client, String command) {
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendChatCommand(command);
        }
    }

    public static void copyToClipboard(MinecraftClient client, String text) {
        client.keyboard.setClipboard(text);
    }
}