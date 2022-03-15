package gr.gousiosg.javacg.stat;

import java.util.Objects;

public class GousiousCall {
    private String leftSide;
    private String middleSide;
    private String rightSide;

    public GousiousCall(String leftSide, String middleSide, String rightSide) {
        this.leftSide = leftSide;
        this.middleSide = middleSide;
        this.rightSide = rightSide;
    }

    public String getLeftSide() {
        return leftSide;
    }

    public void setLeftSide(String leftSide) {
        this.leftSide = leftSide;
    }

    public String getMiddleSide() {
        return middleSide;
    }

    public void setMiddleSide(String middleSide) {
        this.middleSide = middleSide;
    }

    public String getRightSide() {
        return rightSide;
    }

    public void setRightSide(String rightSide) {
        this.rightSide = rightSide;
    }

    @Override
    public String toString() {
        return leftSide+" "+middleSide+rightSide; //yes the space is important
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GousiousCall that = (GousiousCall) o;
        return Objects.equals(leftSide, that.leftSide) && Objects.equals(middleSide, that.middleSide) && Objects.equals(rightSide, that.rightSide);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftSide, middleSide, rightSide);
    }
}
