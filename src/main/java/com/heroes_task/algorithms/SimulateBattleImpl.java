package com.heroes_task.algorithms;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.*;

public final class SimulateBattleImpl implements SimulateBattle {

    // важно для reflection
    private PrintBattleLog printBattleLog;

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        if (playerArmy == null || computerArmy == null) return;

        List<Unit> playerUnits = playerArmy.getUnits();
        List<Unit> computerUnits = computerArmy.getUnits();
        if (playerUnits == null || computerUnits == null) return;

        while (hasAlive(playerUnits) && hasAlive(computerUnits)) {

            // очередь раунда: каждый живой максимум 1 раз
            Deque<Unit> playerQueue = buildSortedRoundQueue(playerUnits);
            Deque<Unit> computerQueue = buildSortedRoundQueue(computerUnits);

            boolean anyAttackThisRound = false;

            // чередование ходов, пока у кого-то ещё есть ходящие в этом раунде
            while (!playerQueue.isEmpty() || !computerQueue.isEmpty()) {

                anyAttackThisRound |= actOne(playerQueue);
                if (Thread.interrupted()) throw new InterruptedException();

                anyAttackThisRound |= actOne(computerQueue);
                if (Thread.interrupted()) throw new InterruptedException();

                // ВАЖНО: бой по требованиям заканчивается, когда армия не имеет живых,
                // но "хвост раунда" другой армии мы уже доигрываем этим циклом,
                // потому что очереди на раунд уже построены.
                // Поэтому тут НЕ нужно break при смерти армии.
            }

            // Если за целый раунд никто не смог атаковать — дальше смысла нет
            if (!anyAttackThisRound) return;
        }
    }

    private boolean actOne(Deque<Unit> queue) {
        while (!queue.isEmpty()) {
            Unit attacker = queue.pollFirst();
            if (attacker == null || !attacker.isAlive()) continue;

            Unit target = tryAttack(attacker);

            if (printBattleLog != null) {
                printBattleLog.printBattleLog(attacker, target);
            }
            return target != null; // был ли успешный удар
        }
        return false;
    }

    private static Deque<Unit> buildSortedRoundQueue(List<Unit> units) {
        List<Unit> alive = new ArrayList<>();
        for (Unit u : units) {
            if (u != null && u.isAlive()) alive.add(u);
        }
        alive.sort((u1, u2) -> {
            int byAtk = Integer.compare(u2.getBaseAttack(), u1.getBaseAttack());
            if (byAtk != 0) return byAtk;
            int byName = String.valueOf(u1.getName()).compareTo(String.valueOf(u2.getName()));
            if (byName != 0) return byName;
            return Integer.compare(System.identityHashCode(u1), System.identityHashCode(u2));
        });
        return new ArrayDeque<>(alive);
    }

    private static Unit tryAttack(Unit attacker) {
        try {
            return attacker.getProgram() == null ? null : attacker.getProgram().attack();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean hasAlive(List<Unit> units) {
        for (Unit u : units) {
            if (u != null && u.isAlive()) return true;
        }
        return false;
    }
}