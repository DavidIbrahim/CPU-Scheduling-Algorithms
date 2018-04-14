package sample;

public class CpuProcess {
    private int processId;
    private int cpuBurstTime;
    private int arrivalTime;

    private int priority;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        CpuProcess temp = new CpuProcess(processId,cpuBurstTime,arrivalTime,priority);
        return temp;
    }

    public CpuProcess(int processId) {
        this.processId = processId;
        this.cpuBurstTime = 0;
        priority =0;
        arrivalTime=0;
    }

    public CpuProcess(int processId, int cpuBurstTime, int arrivalTime, int priority) {
        this.processId = processId;
        this.cpuBurstTime = cpuBurstTime;
        this.arrivalTime = arrivalTime;
        this.priority = priority;
    }

    public int getProcessId() {
        return processId;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setCpuBurstTime(int cpuBurstTime) {
        this.cpuBurstTime = cpuBurstTime;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getCpuBurstTime() {
        return cpuBurstTime;
    }

    public int getPriority() {
        return priority;
    }
}
