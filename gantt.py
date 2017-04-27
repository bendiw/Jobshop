import numpy as np
import matplotlib.pyplot as plt
from itertools import cycle
from random import random as ra
import sys

numJobs = int(sys.argv[1])
numMachs = int(sys.argv[2])
sched = sys.argv[3].split(',')
start = sys.argv[4].split(',')
end = sys.argv[5].split(',')

schedule = []
startTime = []
endTime = []

for i in range(numMachs):
    sc = []
    st = []
    et = []
    for j in range(numJobs):
        sc.append(sched[i*numJobs+j])
        st.append(start[i*numJobs+j])
        et.append(end[i*numJobs+j])
    schedule.append(sc)
    startTime.append(st)
    endTime.append(et)
    


start={}
finish={}
task_counter = [0]*len(schedule[0])
label = []
color = []
machine = []
for job in range(len(schedule[0])):
    start[job] = []
    finish[job] = []
    l = "Job" + str(job)
    label.append(l)
    r = ra()
    b = ra()
    g = ra()
    color.append((r,g,b))

    
for machnr in range(len(schedule)):
    machine.append(machnr)
    for tasknr in range(len(schedule[0])):
        job = int(schedule[machnr][tasknr])
        start_time = int(startTime[machnr][tasknr])
        end_time = int(endTime[machnr][tasknr])
        start[job].append(start_time)
        finish[job].append(end_time)
        task_counter[job] += 1

for i in range(len(schedule[0])):
    plt.hlines(machine, start[i], finish[i], colors=color[i], label=label[i], lw = 17)

plt.legend()
plt.margins(0.1)
plt.ylabel("Machine")
plt.show()

#schedule = [[0,1,2],[1,2,0],[0,1,2]]
#startTime = [[0,5,12],[12,20,25],[30,35,42]]
#process = [[5,6,9],[2,7,4],[3,6,3]]
#gantt(schedule, startTime, process)

