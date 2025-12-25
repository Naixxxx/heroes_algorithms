package com.heroes_task.algorithms;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

public class GeneratePresetImpl implements GeneratePreset {

    private static final int FIELD_WIDTH = 3;      // зона врага слева
    private static final int FIELD_HEIGHT = 21;
    private static final int MAX_PER_TYPE = 11;

    private final Random rng = new Random();

    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        // Не мутируем входной список (иногда это важно для тестов/движка)
        List<Unit> templates = new ArrayList<>(unitList);
        templates.sort(byEfficiency());

        // Готовим уникальные клетки под расстановку
        List<Cell> spawnCells = buildSpawnCells();
        Collections.shuffle(spawnCells, rng);
        Iterator<Cell> cellIt = spawnCells.iterator();

        List<Unit> resultUnits = new ArrayList<>();
        Map<String, Integer> typeSerial = new HashMap<>();

        int remaining = maxPoints;
        int spent = 0;

        for (Unit template : templates) {
            if (remaining <= 0 || !cellIt.hasNext()) break;

            int cost = template.getCost();
            if (cost <= 0) continue; // защита от странных данных

            int affordable = remaining / cost;
            int unitsToCreate = Math.min(MAX_PER_TYPE, affordable);

            for (int i = 0; i < unitsToCreate && cellIt.hasNext(); i++) {
                Cell cell = cellIt.next();

                int serial = typeSerial.merge(template.getUnitType(), 1, Integer::sum);
                String name = template.getName() + " " + serial;

                Unit unit = new Unit(
                        name,
                        template.getUnitType(),
                        template.getHealth(),
                        template.getBaseAttack(),
                        template.getCost(),
                        template.getAttackType(),
                        template.getAttackBonuses(),
                        template.getDefenceBonuses(),
                        cell.x,
                        cell.y
                );

                // КРИТИЧНО: переносим программу поведения, иначе unit.getProgram() будет null
                unit.setProgram(template.getProgram());

                resultUnits.add(unit);

                remaining -= cost;
                spent += cost;
            }
        }

        Army army = new Army();
        army.setUnits(resultUnits);
        army.setPoints(spent);
        return army;
    }

    private Comparator<Unit> byEfficiency() {
        Comparator<Unit> byAttackPerCost =
                Comparator.comparingDouble((Unit u) -> (double) u.getBaseAttack() / u.getCost())
                        .reversed();

        Comparator<Unit> byHealthPerCost =
                Comparator.comparingDouble((Unit u) -> (double) u.getHealth() / u.getCost())
                        .reversed();

        return byAttackPerCost.thenComparing(byHealthPerCost);
    }

    private List<Cell> buildSpawnCells() {
        List<Cell> cells = new ArrayList<>(FIELD_WIDTH * FIELD_HEIGHT);
        for (int x = 0; x < FIELD_WIDTH; x++) {
            for (int y = 0; y < FIELD_HEIGHT; y++) {
                cells.add(new Cell(x, y));
            }
        }
        return cells;
    }

    private static final class Cell {
        private final int x;
        private final int y;

        private Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
