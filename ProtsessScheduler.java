import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Scope(value = "singleton")
public class ProtsessScheduler extends AbstractScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtsessScheduler.class);

    private static final String INIT_TASK_ENABLED = "";
    private static final String INIT_TASK_START_DELAY = "";
    private static final String TASK_FAILURE_DELAY = "";
    private static final String TASK_RETRY_COUNT = "";

    private static final int INIT_TASK_START_DELAY_DEFAULT = 20000;
    private static final int TASK_FAILURE_DELAY_DEFAULT = 60000;
    private static final int TASK_RETRY_COUNT_DEFAULT = 5;

    private int taskStartDelay;
    private int taskFailureDelay;
    private int taskRetryCount;

    private AtomicInteger counter = new AtomicInteger(0);

    private ScheduledExecutorService scheduler;

    @Autowired
    public ProtsessScheduler() {
        startScheduler();
    }

    @Override
    public void startScheduler() {
        LOGGER.info("startScheduler(): scheduler initializing...");
        initTaskStartDelay();
        initTaskFailureDelay();
        initTaskRetryCount();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        if (isSchedulerInitTaskEnabled()) {
            scheduler.schedule(new ProtsessInitTask(), taskStartDelay, TimeUnit.MILLISECONDS);
        }
        scheduler.scheduleAtFixedRate(new MonitorTask(), taskStartDelay, 100000, TimeUnit.MILLISECONDS);
        LOGGER.info("scheduler initialized");
    }

    public void addTask(UUID uuid) {
        LOGGER.info("addTask(): uuid={}", uuid);
        counter.incrementAndGet();
        scheduler.execute(new ProtsessTask(uuid));
    }

    private void addTaskWithDelay(UUID uuid, int delay) {
        LOGGER.info("addTaskWithDelay(): uuid={}, delay={}", uuid, delay);
        counter.incrementAndGet();
        scheduler.schedule(new ProtsessTask(uuid), delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public ExecutorService getScheduler() {
        return scheduler;
    }

    private boolean isSchedulerInitTaskEnabled() {
        try {
            String initTaskEnabled = environment.getProperty(INIT_TASK_ENABLED);
            if (initTaskEnabled != null) {
                return Boolean.parseBoolean(initTaskEnabled);
            } else {
                LOGGER.warn("Taotlus scheduler property '{}' is not set. Init task is not enabled by default.", INIT_TASK_ENABLED);
                return false;
            }
        } catch (Exception e) {
            LOGGER.info("Taotlus scheduler property '{}' is not set. Init task is not enabled by default.", INIT_TASK_ENABLED);
            return false;
        }
    }

    private void initTaskStartDelay() {
        try {
            taskStartDelay = Integer.parseInt(environment.getProperty(INIT_TASK_START_DELAY));
            if (taskStartDelay < 0) {
                LOGGER.warn("Property '{}' has bad value={}. Use default={}.", INIT_TASK_START_DELAY, taskStartDelay,
                            INIT_TASK_START_DELAY_DEFAULT);
                taskStartDelay = INIT_TASK_START_DELAY_DEFAULT;
            }
        } catch (Exception e) {
            LOGGER.warn("Property '{}' is not set. Use default={}.", INIT_TASK_START_DELAY, INIT_TASK_START_DELAY_DEFAULT);
            taskStartDelay = INIT_TASK_START_DELAY_DEFAULT;
        }
    }

    private void initTaskFailureDelay() {
        try {
            taskFailureDelay = Integer.parseInt(environment.getProperty(TASK_FAILURE_DELAY));
            if (taskFailureDelay < 0) {
                LOGGER
                    .warn("Property '{}' has bad value={}. Use default={}.", TASK_FAILURE_DELAY, taskFailureDelay, TASK_FAILURE_DELAY_DEFAULT);
                taskFailureDelay = TASK_FAILURE_DELAY_DEFAULT;
            }
        } catch (Exception e) {
            LOGGER.warn("Property '{}' is not set. Use the default={}.", TASK_FAILURE_DELAY, TASK_FAILURE_DELAY_DEFAULT);
            taskFailureDelay = TASK_FAILURE_DELAY_DEFAULT;
        }
    }

    private void initTaskRetryCount() {
        try {
            taskRetryCount = Integer.parseInt(environment.getProperty(TASK_RETRY_COUNT));
            if (taskRetryCount < 0) {
                LOGGER.warn("Property '{}' has bad value={}. Use default={}.", TASK_RETRY_COUNT, taskRetryCount, TASK_RETRY_COUNT_DEFAULT);
                taskRetryCount = TASK_RETRY_COUNT_DEFAULT;
            }
        } catch (Exception e) {
            LOGGER.warn("Property '{}' is not set. Use the default={}.", TASK_RETRY_COUNT, TASK_RETRY_COUNT_DEFAULT);
            taskRetryCount = TASK_RETRY_COUNT_DEFAULT;
        }
    }

    private class ProtsessInitTask implements Runnable {

        @Override
        public void run() {
            try {
                // INITIALISING TASK
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private class ProtsessTask implements Runnable {

        @Override
        public void run() {
            try {
				// FILL THE TASK HERE
            } catch (Exception e) {
                // WHAT ELSE HAPPENS
				addTaskWithDelay(...);
            } finally {
                counter.decrementAndGet();
            }
        }
    }

    private class MonitorTask implements Runnable {

        @Override
        public void run() {
            LOGGER.info("monitorTask(): counter={}", counter.get());
        }
    }

}

