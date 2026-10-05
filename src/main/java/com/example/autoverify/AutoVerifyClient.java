package com.example.autoverify;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoVerifyClient implements ClientModInitializer {

    private static final Pattern VERIFY = Pattern.compile("/verify\\s+([A-Za-z0-9]+)");

    private String lastCode = "";
    private long lastSent = 0;

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            handle(message.getString());
        });
    }

    private void handle(String text) {
        Matcher m = VERIFY.matcher(text);
        if (!m.find()) return;

        String code = m.group(1);
        long now = System.currentTimeMillis();

        if (code.equals(lastCode) && now - lastSent < 10_000) return;
        lastCode = code;
        lastSent = now;

        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.player != null && client.player.networkHandler != null) {
                client.player.networkHandler.sendChatCommand("verify " + code);
            }
        });
    }
}
