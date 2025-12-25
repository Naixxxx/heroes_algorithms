package com.heroes_task.algorithms;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.*;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        List<Unit> result = new ArrayList<>();
        if (unitsByRow == null || unitsByRow.isEmpty()) return result;

        Set<Integer> frontYs = new HashSet<>();
        if (isLeftArmyTarget) {
            for (int i = unitsByRow.size() - 1; i >= 0; i--) {
                addLayer(unitsByRow.get(i), frontYs, result);
            }
        } else {
            for (int i = 0; i < unitsByRow.size(); i++) {
                addLayer(unitsByRow.get(i), frontYs, result);
            }
        }

        return result;
    }

    private static void addLayer(List<Unit> layer, Set<Integer> frontYs, List<Unit> out) {
        if (layer == null) return;

        for (Unit u : layer) {
            if (u == null || !u.isAlive()) continue;

            int y = u.getyCoordinate();
            if (frontYs.add(y)) {
                out.add(u);
            }
        }
    }
}