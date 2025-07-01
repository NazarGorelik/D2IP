package org.example.model;

import java.util.Objects;

public class Pair {
    public int id1;
    public int id2;

    public Pair(int a, int b) {
        if (a <= b) {
            this.id1 = a;
            this.id2 = b;
        } else {
            this.id1 = b;
            this.id2 = a;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pair)) return false;
        Pair other = (Pair) obj;
        return id1 == other.id1 && id2 == other.id2;
    }

    @Override
    public int hashCode() {
        return 31 * id1 + id2;
    }

    @Override
    public String toString() {
        return "(" + id1 + ", " + id2 + ")";
    }
}
