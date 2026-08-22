package com.ga170212.chk_datapack_link.client.compat;

import com.ga170212.chk_datapack_link.client.gui.ChannelInputScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ChannelInputScreen::new;
    }
}
