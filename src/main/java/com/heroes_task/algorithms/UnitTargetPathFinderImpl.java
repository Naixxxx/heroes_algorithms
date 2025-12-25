package com.heroes_task.algorithms;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    private static final int FIELD_WIDTH = 27;
    private static final int FIELD_HEIGHT = 21;

    // 8 направлений (включая диагонали)
    private static final int[] DIR_X = {-1, -1, -1,  0, 0,  1, 1, 1};
    private static final int[] DIR_Y = {-1,  0,  1, -1, 1, -1, 0, 1};

    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        if (attackUnit == null || targetUnit == null) return new ArrayList<>();

        final int startX = attackUnit.getxCoordinate();
        final int startY = attackUnit.getyCoordinate();
        final int goalX = targetUnit.getxCoordinate();
        final int goalY = targetUnit.getyCoordinate();

        if (!inBounds(startX, startY) || !inBounds(goalX, goalY)) return new ArrayList<>();
        if (startX == goalX && startY == goalY) {
            List<Edge> sameCell = new ArrayList<>();
            sameCell.add(new Edge(startX, startY));
            return sameCell;
        }

        // blocked[x][y] == true означает препятствие
        boolean[][] blocked = new boolean[FIELD_WIDTH][FIELD_HEIGHT];
        if (existingUnitList != null) {
            for (Unit u : existingUnitList) {
                if (u == null || !u.isAlive()) continue;
                // старт и цель не считаем препятствиями
                if (u == attackUnit || u == targetUnit) continue;

                int x = u.getxCoordinate();
                int y = u.getyCoordinate();
                if (inBounds(x, y)) blocked[x][y] = true;
            }
        }

        // gScore[x][y] — стоимость пути от старта до клетки
        int[][] gScore = new int[FIELD_WIDTH][FIELD_HEIGHT];
        for (int[] col : gScore) Arrays.fill(col, Integer.MAX_VALUE);

        // parent[x][y] — предыдущая клетка на кратчайшем пути
        Edge[][] parent = new Edge[FIELD_WIDTH][FIELD_HEIGHT];

        boolean[][] closed = new boolean[FIELD_WIDTH][FIELD_HEIGHT];

        PriorityQueue<SearchNode> openSet = new PriorityQueue<>();
        gScore[startX][startY] = 0;
        openSet.add(new SearchNode(startX, startY, chebyshevDistance(startX, startY, goalX, goalY)));

        while (!openSet.isEmpty()) {
            SearchNode current = openSet.poll();
            int cx = current.x;
            int cy = current.y;

            if (closed[cx][cy]) continue;
            closed[cx][cy] = true;

            if (cx == goalX && cy == goalY) break;

            for (int i = 0; i < 8; i++) {
                int nx = cx + DIR_X[i];
                int ny = cy + DIR_Y[i];

                if (!inBounds(nx, ny)) continue;
                if (blocked[nx][ny]) continue;
                if (closed[nx][ny]) continue;

                int tentativeG = gScore[cx][cy] + 1;
                if (tentativeG < gScore[nx][ny]) {
                    gScore[nx][ny] = tentativeG;
                    parent[nx][ny] = new Edge(cx, cy);

                    int fScore = tentativeG + chebyshevDistance(nx, ny, goalX, goalY);
                    openSet.add(new SearchNode(nx, ny, fScore));
                }
            }
        }

        return restorePath(parent, startX, startY, goalX, goalY);
    }

    private static List<Edge> restorePath(Edge[][] parent, int startX, int startY, int goalX, int goalY) {
        List<Edge> path = new ArrayList<>();
        int x = goalX, y = goalY;

        while (!(x == startX && y == startY)) {
            path.add(new Edge(x, y));
            Edge p = parent[x][y];
            if (p == null) return new ArrayList<>(); // пути нет
            x = p.getX();
            y = p.getY();
        }

        path.add(new Edge(startX, startY));
        Collections.reverse(path);
        return path;
    }

    // Для 8-направленного движения с одинаковой ценой шага подходит эвристика Чебышёва
    private static int chebyshevDistance(int x, int y, int tx, int ty) {
        return Math.max(Math.abs(tx - x), Math.abs(ty - y));
    }

    private static boolean inBounds(int x, int y) {
        return x >= 0 && x < FIELD_WIDTH && y >= 0 && y < FIELD_HEIGHT;
    }

    private static class SearchNode implements Comparable<SearchNode> {
        final int x;
        final int y;
        final int f;

        SearchNode(int x, int y, int f) {
            this.x = x;
            this.y = y;
            this.f = f;
        }

        @Override
        public int compareTo(SearchNode other) {
            return Integer.compare(this.f, other.f);
        }
    }
}