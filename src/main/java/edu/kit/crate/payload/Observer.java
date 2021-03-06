package edu.kit.crate.payload;

/**
 * Interface for the observer pattern used with the deletion of entities.
 */
public interface Observer {
  void update(String entityId);
}
