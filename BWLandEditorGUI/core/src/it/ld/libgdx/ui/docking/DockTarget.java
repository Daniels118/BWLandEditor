package it.ld.libgdx.ui.docking;

public class DockTarget {
    public final DockContainer container;

    public DockTarget(DockContainer container) {
        this.container = container;
    }

    public boolean isValid() {
        return container != null;
    }
}
