package dev.pratyush.stampede;

enum Strategy {
    NAIVE("naive"),
    LEASE("lease"),
    XFETCH("xfetch");

    private final String label;

    Strategy(String label) {
        this.label = label;
    }

    String label() {
        return label;
    }
}
