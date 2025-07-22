package com.matrix.simulation.patterns;

import com.matrix.simulation.Position;
import com.matrix.simulation.entities.EntityType;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Estrategia de movimiento manhattana - se mueve hacia el objetivo por la distancia más corta
 */
public class ManhattanMovementStrategy implements MovementStrategy {

  @Override
  public Position calculateNextMove(
    Position currentPos,
    Position targetPos,
    EntityType[][] matrix,
    List<Position> agentPositions
  ) {
    // 1. Intentar encontrar el camino más corto usando BFS
    Position bfsMove = bfsNextMove(
      currentPos,
      targetPos,
      matrix,
      agentPositions
    );
    if (bfsMove != null) {
      return bfsMove;
    }
    // Si no hay camino, usar la lógica anterior (movimiento directo o alternativas)
    int deltaX = Integer.compare(targetPos.getX(), currentPos.getX());
    int deltaY = Integer.compare(targetPos.getY(), currentPos.getY());
    Position directMove = currentPos.move(deltaX, deltaY);
    if (
      isValidMove(directMove, matrix) &&
      !isNearAgent(directMove, agentPositions)
    ) {
      return directMove;
    }
    Position moveX = currentPos.move(deltaX, 0);
    Position moveY = currentPos.move(0, deltaY);
    boolean canMoveX =
      isValidMove(moveX, matrix) && !isNearAgent(moveX, agentPositions);
    boolean canMoveY =
      isValidMove(moveY, matrix) && !isNearAgent(moveY, agentPositions);
    if (canMoveX && canMoveY) {
      int distanceIfMoveX = moveX.manhattanDistance(targetPos);
      int distanceIfMoveY = moveY.manhattanDistance(targetPos);
      return distanceIfMoveX <= distanceIfMoveY ? moveX : moveY;
    } else if (canMoveX) {
      return moveX;
    } else if (canMoveY) {
      return moveY;
    }
    Position[] alternativeMoves = {
      currentPos.move(1, 0),
      currentPos.move(-1, 0),
      currentPos.move(0, 1),
      currentPos.move(0, -1),
    };
    Position bestAlternative = currentPos;
    int bestScore = Integer.MIN_VALUE;
    for (Position alternative : alternativeMoves) {
      if (isValidMove(alternative, matrix)) {
        int distance = alternative.manhattanDistance(targetPos);
        int agentPenalty = isNearAgent(alternative, agentPositions) ? -100 : 0;
        int score = -distance + agentPenalty;
        if (score > bestScore) {
          bestScore = score;
          bestAlternative = alternative;
        }
      }
    }
    if (bestAlternative.equals(currentPos)) {
      for (Position alternative : alternativeMoves) {
        if (isValidMove(alternative, matrix)) {
          return alternative;
        }
      }
    }
    return bestAlternative;
  }

  // BFS para encontrar el siguiente paso hacia el objetivo evitando obstáculos y agentes
  private Position bfsNextMove(
    Position start,
    Position goal,
    EntityType[][] matrix,
    List<Position> agentPositions
  ) {
    int rows = matrix.length;
    int cols = matrix[0].length;
    boolean[][] visited = new boolean[rows][cols];
    Queue<List<Position>> queue = new LinkedList<>();
    HashSet<Position> agentSet = new HashSet<>(agentPositions);
    List<Position> initial = new LinkedList<>();
    initial.add(start);
    queue.add(initial);
    visited[start.getX()][start.getY()] = true;
    int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
    while (!queue.isEmpty()) {
      List<Position> path = queue.poll();
      Position current = path.get(path.size() - 1);
      if (current.equals(goal)) {
        // El primer paso después del actual
        if (path.size() >= 2) {
          return path.get(1);
        } else {
          return start; // Ya está en el objetivo
        }
      }
      for (int[] dir : directions) {
        int nx = current.getX() + dir[0];
        int ny = current.getY() + dir[1];
        if (nx >= 0 && nx < rows && ny >= 0 && ny < cols && !visited[nx][ny]) {
          Position next = new Position(nx, ny);
          if (
            matrix[nx][ny] != EntityType.OBSTACLE &&
            matrix[nx][ny] != EntityType.AGENT &&
            !agentSet.contains(next)
          ) {
            visited[nx][ny] = true;
            List<Position> newPath = new LinkedList<>(path);
            newPath.add(next);
            queue.add(newPath);
          }
        }
      }
    }
    return null; // No hay camino
  }

  private boolean isValidMove(Position pos, EntityType[][] matrix) {
    int x = pos.getX();
    int y = pos.getY();
    if (x < 0 || x >= matrix.length || y < 0 || y >= matrix[0].length) {
      return false;
    }
    EntityType cellType = matrix[x][y];
    return cellType != EntityType.OBSTACLE && cellType != EntityType.AGENT;
  }

  // Penaliza si la posición está adyacente a un agente
  private boolean isNearAgent(Position pos, List<Position> agentPositions) {
    for (Position agent : agentPositions) {
      if (pos.manhattanDistance(agent) == 1) {
        return true;
      }
    }
    return false;
  }
}
