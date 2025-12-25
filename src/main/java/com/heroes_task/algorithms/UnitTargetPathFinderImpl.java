package com.heroes_task.algorithms;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;
public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {
    private static final int[] X_DIRECTIONS = {-1, -1, -1,  0, 0,  1, 1, 1};
    private static final int[] Y_DIRECTIONS = {-1,  0,  1, -1, 1, -1, 0, 1};

    private static final int H = 21;
    private static final int W = 27;

    private static final int INF = Integer.MAX_VALUE;
    private static final int NO_PARENT = -1;

    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        if (attackUnit == null || targetUnit == null) return Collections.emptyList();

        final int startX = attackUnit.getxCoordinate();
        final int targetX = targetUnit.getxCoordinate();
        final int startY = attackUnit.getyCoordinate();
        final int targetY = targetUnit.getyCoordinate();

        if (!inside(startX, startY) || !inside(targetX, targetY)) return Collections.emptyList();

        if (startX == targetX && startY == targetY) {
            return List.of(new Edge(startX, startY));
        }

        final int start = idx(startX, startY);
        final int goal = idx(targetX, targetY);

        boolean[] blocked = buildBlocked(existingUnitList, attackUnit, targetUnit);

        blocked[start] = false;
        blocked[goal] = false;

        int[] gScore = new int[W * H];
        Arrays.fill(gScore, INF);

        int[] parent = new int[W * H];
        Arrays.fill(parent, NO_PARENT);

        boolean[] closed = new boolean[W * H];

        PriorityQueue<QItem> open = new PriorityQueue<>(Comparator.comparingInt(a -> a.f));
        gScore[start] = 0;
        open.add(new QItem(start, simplification(startX, startY, targetX, targetY)));

        while (!open.isEmpty()) {
            QItem cur = open.poll();
            int curIdx = cur.cell;

            if (closed[curIdx]) continue;
            closed[curIdx] = true;

            if (curIdx == goal) break;

            int cx = x(curIdx);
            int cy = y(curIdx);

            int baseG = gScore[curIdx];
            if (baseG == INF) continue;

            for (int k = 0; k < 8; k++) {
                int nx = cx + X_DIRECTIONS[k];
                int ny = cy + Y_DIRECTIONS[k];

                if (!inside(nx, ny)) continue;

                int nIdx = idx(nx, ny);
                if (blocked[nIdx] || closed[nIdx]) continue;

                int tentative = baseG + 1;
                if (tentative < gScore[nIdx]) {
                    gScore[nIdx] = tentative;
                    parent[nIdx] = curIdx;

                    int f = tentative + simplification(nx, ny, targetX, targetY);
                    open.add(new QItem(nIdx, f));
                }
            }
        }

        return reconstruct(parent, start, goal);
    }

    private static boolean[] buildBlocked(List<Unit> units, Unit attacker, Unit target) {
        boolean[] blocked = new boolean[W * H];
        if (units == null) return blocked;

        for (Unit u : units) {
            if (u == null || !u.isAlive()) continue;
            if (u == attacker || u == target) continue;

            int x = u.getxCoordinate();
            int y = u.getyCoordinate();
            if (!inside(x, y)) continue;

            blocked[idx(x, y)] = true;
        }
        return blocked;
    }

    private static List<Edge> reconstruct(int[] parent, int start, int goal) {
        if (goal != start && parent[goal] == NO_PARENT) return Collections.emptyList();

        ArrayList<Edge> rev = new ArrayList<>();
        int cur = goal;

        while (cur != start) {
            rev.add(new Edge(x(cur), y(cur)));
            cur = parent[cur];
            if (cur == NO_PARENT) return Collections.emptyList();
        }

        rev.add(new Edge(x(start), y(start)));
        Collections.reverse(rev);
        return rev;
    }

    private static int simplification(int x, int y, int tx, int ty) {
        return Math.max(Math.abs(tx - x), Math.abs(ty - y));
    }

    private static boolean inside(int x, int y) {
        return x >= 0 && x < W && y >= 0 && y < H;
    }

    private static int idx(int x, int y) {
        return y * W + x;
    }

    private static int x(int idx) {
        return idx % W;
    }

    private static int y(int idx) {
        return idx / W;
    }

    private static final class QItem {
        final int cell;
        final int f;

        QItem(int cell, int f) {
            this.cell = cell;
            this.f = f;
        }
    }
}