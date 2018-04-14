package sample;

import java.util.*;

public class CpuSimulate {
    private ArrayList<CpuProcess> processes;
    private int contextSwitchTime;


    public CpuSimulate(ArrayList<CpuProcess> processes) {
        this.processes = processes;
        contextSwitchTime = 0;
    }


    public CpuSchedule simulateFCFS() {

        ArrivalComparator arrivalComparator = new ArrivalComparator();


        return makeSchedule(false, arrivalComparator,1);
    }

    public CpuSchedule simulateSJF(boolean isPreemptive) {

        SJFComparator sjfComparator = new SJFComparator();
        return makeSchedule(isPreemptive,sjfComparator,1);
    }


    public CpuSchedule simulatePriority(boolean isPreemptive) {


        PriorityComparator priorityComparator = new PriorityComparator();
        return makeSchedule(isPreemptive, priorityComparator,1);

    }

    public CpuSchedule simulateRoundRobin(int quantumTime) {
        ArrivalComparator arrivalComparator = new ArrivalComparator();
        CpuSchedule cpuSchedule = new CpuSchedule();
        ArrayList<CpuProcess> temp = makeCopy();
        Collections.sort(temp, arrivalComparator);

        Queue<CpuProcess> readyQueue = new LinkedList<>();
        int currentTime = temp.get(0).getArrivalTime();
        readyQueue.add(temp.get(0));
        temp.remove(0);
        int counter = 0;
        while (readyQueue.size() > 0||temp.size()>0) {


            CpuProcess process = readyQueue.peek();
            if(process == null){

                while (temp.size()>0){
                    CpuProcess processtemp = temp.get(0);
                    if(processtemp.getArrivalTime()<=currentTime){
                        readyQueue.add(processtemp);
                        temp.remove(processtemp);

                    }
                    else {
                        break;
                    }
                }
                if(readyQueue.size()==0) currentTime++;  // if no process arrived increase the time
                continue;
            }

            if (process.getCpuBurstTime() == 0) {
                    readyQueue.remove();
                    counter = 0;

                }
                else {
                    cpuSchedule.startTimes.add(currentTime);
                    cpuSchedule.processIDs.add(process.getProcessId());
                    cpuSchedule.finishTimes.add(currentTime+1);
                    process.setCpuBurstTime(process.getCpuBurstTime()-1);
                    currentTime++;
                    if(counter == quantumTime-1  ) {

                        //add the process already arrived to the queue
                        while (temp.size()>0){
                            CpuProcess processtemp = temp.get(0);
                            if(processtemp.getArrivalTime()<=currentTime){
                                readyQueue.add(processtemp);
                                temp.remove(processtemp);

                            }
                            else {
                                break;
                            }
                        }

                        counter = 0;
                        // add the current process to the queue
                        readyQueue.add(readyQueue.poll());

                    }
                    else {
                        counter++;
                    }

            }




        }
        return cpuSchedule;
    }

    private CpuSchedule makeSchedule(boolean isPreemptive, Comparator<CpuProcess> cpuProcessComparator,
                                     int quantumTime) {
        CpuSchedule cpuSchedule = new CpuSchedule();
        ArrayList<CpuProcess> temp = makeCopy();
        int currentTime = 0;

        Collections.sort(temp, cpuProcessComparator);

        while (temp.size() > 0) {
            boolean noProcessArrived = true;
            for (int i = 0; i < temp.size(); i++) {
                CpuProcess process = temp.get(i);

                if (process.getCpuBurstTime() == 0) {
                    temp.remove(process);

                    i--;
                }
                else if (process.getArrivalTime() <= currentTime) {
                    noProcessArrived = false;
                    cpuSchedule.startTimes.add(currentTime);
                    cpuSchedule.processIDs.add(process.getProcessId());
                    cpuSchedule.finishTimes.add(currentTime+1);
                    process.setCpuBurstTime(process.getCpuBurstTime() - 1);
                    currentTime++;
                    if (isPreemptive) {



                        if(cpuProcessComparator.getClass().getSimpleName().equals("SJFComparator"))
                            Collections.sort(temp,cpuProcessComparator);



                        break;
                    }
                    else {
                        while(process.getCpuBurstTime()!=0) {
                            cpuSchedule.startTimes.add(currentTime);
                            cpuSchedule.processIDs.add(process.getProcessId());
                            cpuSchedule.finishTimes.add(currentTime+1);
                            process.setCpuBurstTime(process.getCpuBurstTime() - 1);
                            currentTime++;



                        }
                        temp.remove(process);
                        i--;
                    }

                }
            }
            if(noProcessArrived) currentTime++;

        }
        return cpuSchedule;
    }
    private ArrayList<CpuProcess> makeCopy() {
        ArrayList<CpuProcess> temp = new ArrayList<>();
        for (CpuProcess process : processes) {
            try {
                temp.add((CpuProcess) process.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return temp;
    }


    private class ArrivalComparator implements Comparator<CpuProcess> {
        @Override
        public int compare(CpuProcess o1, CpuProcess o2) {
            if (o1.getArrivalTime() == o2.getArrivalTime()) {
                return o1.getProcessId() - o2.getProcessId();
            }
            return (int) (o1.getArrivalTime() - o2.getArrivalTime());
        }
    }

    private class SJFComparator implements Comparator<CpuProcess> {
        @Override
        public int compare(CpuProcess o1, CpuProcess o2) {
            if (o1.getCpuBurstTime() == o2.getCpuBurstTime()) {
                return o1.getProcessId() - o2.getProcessId();
            }
            return (int)(o1.getCpuBurstTime() - o2.getCpuBurstTime());
        }
    }
    private class PriorityComparator implements Comparator<CpuProcess> {
        @Override
        public int compare(CpuProcess o1, CpuProcess o2) {
            if (o1.getPriority() == o2.getPriority()) {
                return o1.getProcessId() - o2.getProcessId();
            }
            return o1.getPriority() - o2.getPriority();
        }
    }



    public class CpuSchedule {
        ArrayList<Integer> startTimes;
        ArrayList<Integer> processIDs;
        ArrayList<Integer> finishTimes;

        public CpuSchedule() {
            startTimes = new ArrayList<>();
            processIDs = new ArrayList<>();
            finishTimes = new ArrayList<>();
        }
    }


}


