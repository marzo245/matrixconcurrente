package com.matrix.simulation;

import java.util.Objects;

/**
 * Clase immutable para representar una posición en la matriz (Value Object Pattern)
 */
public final class Position {

  private final int x;
  private final int y;

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  /**
   * Calcula la distancia Manhattan entre esta posición y otra
   */
  public int manhattanDistance(Position other) {
    return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
  }

  /**
   * Crea una nueva posición con un desplazamiento
   */
  public Position move(int deltaX, int deltaY) {
    return new Position(x + deltaX, y + deltaY);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Position position = (Position) obj;
    return x == position.x && y == position.y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public String toString() {
    return String.format("Position(%d, %d)", x, y);
  }
}
