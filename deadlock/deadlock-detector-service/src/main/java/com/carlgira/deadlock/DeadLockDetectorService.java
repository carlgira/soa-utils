package com.carlgira.deadlock;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeadLockDetectorService {

    @RequestMapping("/deadlock")
    public Boolean deadlockDetector() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] threadIds = bean.findDeadlockedThreads(); // Returns null if no threads are deadlocked.
        return threadIds != null;
    }
}
