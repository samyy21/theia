package com.paytm.pgplus.biz.taskengine.workflow;

import com.paytm.pgplus.biz.taskengine.task.AbstractTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TaskedWorkflow {

    private final List<Set<AbstractTask>> tasks;

    public TaskedWorkflow() {
        tasks = new ArrayList<>();
    }

    protected void addTask(int position, AbstractTask task) {
        addTask(position, task, tasks);
    }

    public List<Set<AbstractTask>> getTasks() {
        List<Set<AbstractTask>> newTasks = new ArrayList<>();
        int index = -1;
        for (Set<AbstractTask> tasks : tasks) {
            index++;
            for (AbstractTask task : tasks) {
                addTask(index, task, newTasks);
            }
        }
        return newTasks;

    }

    private void addTask(int position, AbstractTask task, List<Set<AbstractTask>> allTasks) {
        if (allTasks.size() == position) {
            Set<AbstractTask> newTasks = Collections.newSetFromMap(new ConcurrentHashMap<>());
            allTasks.add(position, newTasks);
        }
        Set<AbstractTask> tasks = allTasks.get(position);
        tasks.add(task);
    }

}
