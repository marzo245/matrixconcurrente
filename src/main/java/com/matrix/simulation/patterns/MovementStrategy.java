package com.matrix.simulation.patterns;

import com.matrix.simulation.Position;
import com.matrix.simulation.entities.EntityType;

/**
 * Strategy Pattern para diferentes algoritmos de movimiento
 */
public interface MovementStrategy {
  Position calculateNextMove(
    Position currentPos,
    Position targetPos,
    EntityType[][] matrix,
    java.util.List<Position> agentPositions
  );
}
