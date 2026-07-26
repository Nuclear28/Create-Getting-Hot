package com.GoingHot.create_hot.infrastructure.ponder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

public class ModPonderScenes {
    public static void steamInterfaceBasic(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("steam_interface", "Использование парового интерфейса");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
    }
}
