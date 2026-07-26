package com.GoingHot.create_hot.infrastructure.ponder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import com.GoingHot.create_hot.ExampleMod;

public class ModPonderIndex implements PonderPlugin {

    @Override
    public String getModId() {
        return "create_hot";
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper helper) {
        // Привязываем 3D-сцену напрямую через компилируемый метод хелпера
        helper.addStoryBoard(
            ExampleMod.STEAM_INTERFACE.get().asItem(),
            "steam_interface_basic",
            ModPonderScenes::steamInterfaceBasic
        );
    }
}
