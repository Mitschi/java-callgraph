package gr.gousiosg.javacg.stat;

import java.util.Objects;

public class GousiousCall {
    private String leftSide;
    private String middleSide;
    private String rightSide;

    private Boolean leftSideFQN=false;
    private Boolean rightSideFQN=false;
    private Boolean isOptional=false;

    public GousiousCall(String leftSide, String middleSide, String rightSide) {
        this.leftSide = leftSide;
        this.middleSide = middleSide;
        this.rightSide = rightSide;
    }

    public Boolean getOptional() {
        return isOptional;
    }

    public void setOptional(Boolean optional) {
        isOptional = optional;
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

    public Boolean getLeftSideFQN() {
        return leftSideFQN;
    }

    public void setLeftSideFQN(Boolean leftSideFQN) {
        this.leftSideFQN = leftSideFQN;
    }

    public Boolean getRightSideFQN() {
        return rightSideFQN;
    }

    public void setRightSideFQN(Boolean rightSideFQN) {
        this.rightSideFQN = rightSideFQN;
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
