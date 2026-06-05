package com.erneto.client;

import com.erneto.client.config.Alert;
import com.erneto.client.gui.ConfigScreen;
import com.erneto.client.gui.MainMenu;
import com.erneto.client.hud.HudRenderer;
import com.erneto.client.service.PHandler;
import com.erneto.client.service.PLogger;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class DMCUtilsClient implements ClientModInitializer {

    private Alert config;
    private PLogger logger;
    private HudRenderer hud;
    private KeyBinding configKey;

    @Override
    public void onInitializeClient() {
        config = new Alert();
        //Pass the full config so PLogger can read credentials dynamically
        logger = new PLogger(config.getLogFile(), config);
        hud = new HudRenderer(config);

        hud.register();
        registerKeybind();
        registerMessageEvents();
        registerEntityEvent();
        registerCommands();
    }

    private void registerKeybind() {
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dmcutils.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                KeyBinding.Category.create(Identifier.of("dmcutils", "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.wasPressed()) {
                ConfigScreen screen = new ConfigScreen(client.currentScreen, config);
                screen.setLogger(logger);
                client.setScreen(screen);
            }
        });
    }

    private void registerMessageEvents() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String content = message.getString();
            if (content.contains("USUARIO SANCIONADO")) {
                PHandler.PData data = PHandler.parse(content);
                if (data != null && data.staff().equalsIgnoreCase("erneto13")) {
                    logger.log(data);
                    hud.setLastSanction(data.user() + " §7→ " + data.type());
                }
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String content = message.getString().toLowerCase();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            for (String word : config.getAlertWords()) {
                if (content.contains(word.toLowerCase())) {
                    mc.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(), 2.0f, 1.0f);
                    mc.inGameHud.setOverlayMessage(
                            Text.literal("§6§l[!] §eAlerta: §f" + word), false);
                    break;
                }
            }
        });
    }

    private void registerEntityEvent() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()
                    && entity instanceof PlayerEntity target
                    && hand == Hand.MAIN_HAND
                    && player.isSneaking()) {
                MinecraftClient.getInstance().execute(() ->
                        MainMenu.open(MinecraftClient.getInstance(), target.getName().getString())
                );
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

    private void registerCommands() {
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
                                ctx.getSource().sendFeedback(
                                        Text.literal("§eWatchlist §7(" + config.getAlertWords().size() + "): §f"
                                                + config.getAlertWords()));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("remove")
                            .executes(ctx -> {
                                if (config.getAlertWords().isEmpty()) {
                                    ctx.getSource().sendFeedback(Text.literal("§cNo hay palabras."));
                                    return 1;
                                }
                                String removed = config.getAlertWords()
                                        .remove(config.getAlertWords().size() - 1);
                                config.save();
                                ctx.getSource().sendFeedback(Text.literal("§cEliminado: §f" + removed));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("clear")
                            .executes(ctx -> {
                                int count = config.getAlertWords().size();
                                config.getAlertWords().clear();
                                config.save();
                                ctx.getSource().sendFeedback(
                                        Text.literal("§cEliminadas " + count + " palabras."));
                                return 1;
                            })));

            dispatcher.register(ClientCommandManager.literal("dmctoggle")
                    .executes(ctx -> {
                        boolean newState = !config.isAutoTpEnabled();
                        config.setAutoTpEnabled(newState);
                        config.save();
                        String status = newState ? "§aACTIVADO" : "§cDESACTIVADO";
                        ctx.getSource().sendFeedback(
                                Text.literal("§8[dmc] §fAuto-Report TP: " + status));
                        return 1;
                    }));

            dispatcher.register(ClientCommandManager.literal("dmcconfig")
                    .executes(ctx -> {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        mc.execute(() -> {
                            ConfigScreen screen = new ConfigScreen(null, config);
                            screen.setLogger(logger);
                            mc.setScreen(screen);
                        });
                        return 1;
                    }));
        });
    }
}
