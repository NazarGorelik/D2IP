package org.example.model;

import java.util.Objects;

public class Pair {
    public int id1;
    public int id2;

    public Pair(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) return false;
        Pair p = (Pair) o;
        return id1 == p.id1 && id2 == p.id2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }
}
