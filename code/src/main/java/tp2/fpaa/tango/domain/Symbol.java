package tp2.fpaa.tango.domain;

public enum Symbol {
    SUN, MOON;

    public Symbol opposite() {
        return this == SUN ? MOON : SUN;
    }
}
