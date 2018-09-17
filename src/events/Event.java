package events;

import java.util.ArrayList;

public abstract class Event {
    private String startTime;
    private String endTime;

    private int startLine;
    private int endLine;

    private int bufferTime;

    public Event(String startTime, String endTime, int startLine, int endLine, int bufferTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.startLine = startLine;
        this.endLine = endLine;
        this.bufferTime = bufferTime;
    }

    public void updateEnd(String newEndTime, int newEndLine) {
        endTime = newEndTime;
        endLine = newEndLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public String toString() {
        return  "[line " + startLine + " to " + endLine + ", time " + startTime + " to " + endTime + "]";
    }

    public boolean isFinished(int currentLine, ArrayList<String> lineValues) {
        if ((currentLine - endLine) >= bufferTime) {
            return true;
        } else {
            return false;
        }
    }
}
