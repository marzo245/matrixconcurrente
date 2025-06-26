package com.matrix.simulation.entities;

/**
 * Tipo de entidad en la matriz
 */
public enum EntityType {
  NEON('N'),
  AGENT('A'),
  TELEPORT('T'),
  OBSTACLE('#'),
  EMPTY('.');

  private final char symbol;

  EntityType(char symbol) {
    this.symbol = symbol;
  }

  public char getSymbol() {
    return symbol;
  }
}
