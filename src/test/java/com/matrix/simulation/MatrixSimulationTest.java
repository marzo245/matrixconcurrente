package com.matrix.simulation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios para la simulación de matriz
 */
public class MatrixSimulationTest {

  private MatrixSimulation simulation;

  @BeforeEach
  void setUp() {
    simulation = new MatrixSimulation();
  }

  @Test
  void testSimulationInitialization() {
    assertNotNull(simulation);
    assertTrue(simulation.isSimulationRunning());
    assertEquals(8, simulation.getMatrixSize());
  }

  @Test
  void testPositionCreation() {
    Position pos = new Position(3, 4);
    assertEquals(3, pos.getX());
    assertEquals(4, pos.getY());
  }

  @Test
  void testPositionEquality() {
    Position pos1 = new Position(2, 3);
    Position pos2 = new Position(2, 3);
    Position pos3 = new Position(3, 2);

    assertEquals(pos1, pos2);
    assertNotEquals(pos1, pos3);
  }

  @Test
  void testManhattanDistance() {
    Position pos1 = new Position(1, 1);
    Position pos2 = new Position(4, 5);

    assertEquals(7, pos1.manhattanDistance(pos2));
  }

  @Test
  void testPositionMove() {
    Position original = new Position(2, 3);
    Position moved = original.move(1, -1);

    assertEquals(3, moved.getX());
    assertEquals(2, moved.getY());

    // Verificar que la posición original no cambió (inmutabilidad)
    assertEquals(2, original.getX());
    assertEquals(3, original.getY());
  }

  @Test
  void testSimulationObserver() {
    TestObserver observer = new TestObserver();
    simulation.addObserver(observer);

    // Detener la simulación debería notificar al observer
    simulation.stopSimulation();

    assertFalse(simulation.isSimulationRunning());
    assertTrue(observer.wasNotified());
  }

  // Clase helper para testing del Observer Pattern
  private static class TestObserver implements SimulationObserver {

    private boolean notified = false;

    @Override
    public void onSimulationEvent(String message) {
      notified = true;
    }

    public boolean wasNotified() {
      return notified;
    }
  }
}
