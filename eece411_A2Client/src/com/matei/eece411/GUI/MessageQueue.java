package com.matei.eece411.GUI;

import java.util.LinkedList;

/* a synchronized queue */
public class MessageQueue {

    /* the actual queue */
    private LinkedList<String>  _queue ;
    
    /* the constructor - it simply creates the LinkedList where the queue elements are stored */
    public MessageQueue() {
        _queue = new LinkedList<String>();
    }
    
    /* gets the first element of the queue or blocks if the queue is empty */
    public synchronized String dequeue() throws InterruptedException {
        while (_queue.isEmpty()) {
            wait();
        }
        return (String)_queue.removeFirst();
    }
    
    /* add a new element to the queue */
    public synchronized void enqueue(String m) {
        _queue.addLast(m);
        notify();
    }
}
