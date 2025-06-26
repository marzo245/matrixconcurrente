package com.matrix.simulation;

/**
 * Observer Pattern - Interface para observar eventos de la simulación
 */
public interface SimulationObserver {
  void onSimulationEvent(String message);
}
