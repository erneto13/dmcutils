package com.erneto.client;

import com.erneto.client.config.Alert;
import com.erneto.client.gui.MainMenu;
import com.erneto.client.service.PHandler;
import com.erneto.client.service.PLogger;
import com.erneto.client.service.RHandler;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class DMCUtilsClient implements ClientModInitializer {
    private Alert config;
    private PLogger logger;


    @Override
    public void onInitializeClient() {
        config = new Alert();
        logger = new PLogger();

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String content = message.getString();
            if (content.contains("USUARIO SANCIONADO")) {
                PHandler.PData data = PHandler.parse(content);
                if (data != null && data.staff().equalsIgnoreCase("erneto13")) {
                    logger.log(data);
                }
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String content = message.getString();
            if (content.contains("USUARIO SANCIONADO")) {
                PHandler.PData data = PHandler.parse(content);
                if (data != null) logger.log(data);
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String content = message.getString().toLowerCase();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            for (String word : config.getAlertWords()) {
                if (content.contains(word.toLowerCase())) {
                    mc.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(), 2.0f, 1.0f);
                    mc.inGameHud.setOverlayMessage(Text.literal("§6§l(!) §eWord detected: §f" + word), false);
                    break;
                }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("dmcalert")
                    .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("word", StringArgumentType.string())
                                    .executes(ctx -> {
                                        String w = StringArgumentType.getString(ctx, "word").toLowerCase();
                                        if (!config.getAlertWords().contains(w)) {
                                            config.getAlertWords().add(w);
                                            config.save();
                                            ctx.getSource().sendFeedback(Text.literal("§aTracking: §f" + w));
                                        }
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("list")
                            .executes(ctx -> {
                                ctx.getSource().sendFeedback(Text.literal("§eWatchlist: §f" + config.getAlertWords()));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("remove")
                            .executes(ctx -> {
                                if (config.getAlertWords().isEmpty()) {
                                    ctx.getSource().sendFeedback(Text.literal("§cNo words to remove."));
                                    return 1;
                                }
                                String removed = config.getAlertWords().remove(config.getAlertWords().size() - 1);
                                config.save();
                                ctx.getSource().sendFeedback(Text.literal("§cRemoved: §f" + removed));
                                return 1;
                            })));

            dispatcher.register(ClientCommandManager.literal("dmctoggle")
                    .executes(context -> {
                        boolean newState = !config.isAutoTpEnabled();
                        config.setAutoTpEnabled(newState);
                        config.save();

                        String status = newState ? "§aENABLED" : "§cDISABLED";
                        context.getSource().sendFeedback(Text.literal("§6§l[DMC] §fAuto-Report TP is now: " + status));
                        return 1;
                    }));
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && entity instanceof PlayerEntity target && hand == Hand.MAIN_HAND && player.isSneaking()) {
                MinecraftClient.getInstance().execute(() -> MainMenu.open(MinecraftClient.getInstance(), target.getName().getString()));
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }
}