package com.heroes_task.algorithms;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.*;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        List<Unit> result = new ArrayList<>();
        if (unitsByRow == null || unitsByRow.isEmpty()) return result;

        // y, которые уже заняты юнитами ближе к атакующему (т.е. прикрывают остальных)
        Set<Integer> frontYs = new HashSet<>();

        if (isLeftArmyTarget) {
            // цель слева (x=0..2), атакуют справа -> идём от фронта: x=2 -> x=1 -> x=0
            for (int i = unitsByRow.size() - 1; i >= 0; i--) {
                addLayer(unitsByRow.get(i), frontYs, result);
            }
        } else {
            // цель справа (x=24..26), атакуют слева -> идём от фронта: x=24 -> x=25 -> x=26
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
            // добавляем только первого (ближайшего к атакующему) для каждого y
            if (frontYs.add(y)) {
                out.add(u);
            }
        }
    }
}