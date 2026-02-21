package com.example.workflow_todo.task;

public enum Priority {
    LOW(1), MED(2), HIGH(3);

    private final int rank;
    Priority(int rank){ this.rank = rank; }
    public int getRank(){ return rank; }
}
