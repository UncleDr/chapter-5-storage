package com.camp.bit.todolist.beans;

public enum Priority {
    LOW(-1), MEDIUM(0), HIGH(1);

    public final int intValue;

    Priority(int intValue) {
        this.intValue = intValue;
    }
}
