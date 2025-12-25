package com.heroes_task.algorithms;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.*;

public final class SimulateBattleImpl implements SimulateBattle {

    private PrintBattleLog printBattleLog;

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        if (playerArmy == null || computerArmy == null) return;

        List<Unit> playerUnits = playerArmy.getUnits();
        List<Unit> computerUnits = computerArmy.getUnits();

        if (playerUnits == null || computerUnits == null) return;

        while (isAliveCheck(playerUnits) && isAliveCheck(computerUnits)) {
            Deque<Unit> playerQueue = buildSortedRoundQueue(playerUnits);
            Deque<Unit> computerQueue = buildSortedRoundQueue(computerUnits);

            boolean anyAttackThisRound = false;

            while (!playerQueue.isEmpty() || !computerQueue.isEmpty()) {

                anyAttackThisRound |= actOne(playerQueue);
                if (Thread.interrupted()) throw new InterruptedException();

                anyAttackThisRound |= actOne(computerQueue);
                if (Thread.interrupted()) throw new InterruptedException();
            }

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
            return target != null;
        }
        return false;
    }

    private static boolean isAliveCheck(List<Unit> units) {
        for (Unit u : units) {
            if (u != null && u.isAlive()) return true;
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
}