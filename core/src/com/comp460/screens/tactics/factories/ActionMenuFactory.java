package com.comp460.screens.tactics.factories;

import com.badlogic.ashley.core.Entity;
import com.comp460.screens.tactics.TacticsScreen;
import com.comp460.screens.tactics.components.cursor.MovementPathComponent;
import com.comp460.screens.tactics.components.map.MapPositionComponent;
import com.comp460.screens.tactics.components.unit.PossibleAttacksComponent;
import com.comp460.screens.tactics.components.unit.QueuedMoveComponent;
import com.comp460.screens.tactics.systems.game.MoveActionSystem;

import java.util.List;

/**
 * Created by matth on 3/22/2017.
 */
public class ActionMenuFactory {

    public static QueuedMoveComponent makeActionMenu(Entity entity, TacticsScreen screen) {
        QueuedMoveComponent moveComponent = new QueuedMoveComponent();

        if (entity.getComponent(MovementPathComponent.class) != null) {
            moveComponent.actions.add(MoveActionSystem.Action.WAIT);
            moveComponent.actions.add(MoveActionSystem.Action.WAIT);
            moveComponent.actions.add(MoveActionSystem.Action.WAIT);
            moveComponent.actions.add(MoveActionSystem.Action.WAIT);
        }
        if (entity.getComponent(PossibleAttacksComponent.class) != null) {
            moveComponent.actions.add(MoveActionSystem.Action.ATTACK);
        }
        return moveComponent;
    }
}
