package org.example;

import java.util.Objects;

public class MatchPair {
    private final int id1;
    private final int id2;

    public MatchPair(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public int getId1() {
        return id1;
    }

    public int getId2() {
        return id2;
    }

    @Override
    public String toString() {
        return "(" + id1 + ", " + id2 + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchPair)) return false;
        MatchPair that = (MatchPair) o;
        return id1 == that.id1 && id2 == that.id2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }
}
