package com.matrix.simulation.patterns;

import com.matrix.simulation.Position;
import com.matrix.simulation.entities.EntityType;

/**
 * Estrategia de movimiento manhattana - se mueve hacia el objetivo por la distancia más corta
 */
public class ManhattanMovementStrategy implements MovementStrategy {

  @Override
  public Position calculateNextMove(
    Position currentPos,
    Position targetPos,
    EntityType[][] matrix
  ) {
    int deltaX = Integer.compare(targetPos.getX(), currentPos.getX());
    int deltaY = Integer.compare(targetPos.getY(), currentPos.getY());

    Position newPos = currentPos.move(deltaX, deltaY);

    // Verificar si el movimiento es válido
    if (isValidMove(newPos, matrix)) {
      return newPos;
    }

    // Si no puede moverse directamente, intentar moverse solo en X o Y
    Position moveX = currentPos.move(deltaX, 0);
    if (isValidMove(moveX, matrix)) {
      return moveX;
    }

    Position moveY = currentPos.move(0, deltaY);
    if (isValidMove(moveY, matrix)) {
      return moveY;
    }

    // Si no puede moverse, quedarse en la misma posición
    return currentPos;
  }

  private boolean isValidMove(Position pos, EntityType[][] matrix) {
    int x = pos.getX();
    int y = pos.getY();

    // Verificar límites
    if (x < 0 || x >= matrix.length || y < 0 || y >= matrix[0].length) {
      return false;
    }

    // Verificar que no sea obstáculo
    return matrix[x][y] != EntityType.OBSTACLE;
  }
}
