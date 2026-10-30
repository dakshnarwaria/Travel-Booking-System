package com.travel.interfaces;

/**
 * Demonstrates: Interface abstraction and contract specification for terminal rendering.
 * Multiple classes across hierarchies implement this for polymorphism.
 */
public interface Displayable {
    /**
     * Renders the implementing component to the terminal output.
     */
    void render();

    /**
     * Demonstrates interface default method.
     * Provides an optional fallback description for standard output.
     */
    default String getDisplaySummary() {
        return "Displayable Component";
    }
}
