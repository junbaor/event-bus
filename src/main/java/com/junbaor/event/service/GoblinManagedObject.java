package com.junbaor.event.service;

import com.junbaor.event.util.AnnotationUtils;
import com.junbaor.event.util.StopWatch;
import com.junbaor.event.util.StringUtils;
import com.junbaor.event.function.Disposable;
import com.junbaor.event.function.Initializable;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

abstract public class GoblinManagedObject
        implements PlatformManagedObject, Initializable, Disposable {

    protected final Logger logger;

    private final AtomicReference<StopWatch> stopWatchReference = new AtomicReference<>();
    private final ObjectName objectName;
    private final boolean registerMBean;
    private final AtomicBoolean initialized = new AtomicBoolean();
    private final AtomicBoolean disposed = new AtomicBoolean();

    protected GoblinManagedObject() {
        logger = initializeLogger();
        initializeStopWatch();
        GoblinManagedBean annotation = AnnotationUtils.getAnnotation(getClass(), GoblinManagedBean.class);
        if (annotation != null) {
            String type = annotation.type();
            if (type.isEmpty()) type = "goblin";
            String name = annotation.name();
            if (name.isEmpty()) name = getClass().getSimpleName();
            if (name.isEmpty()) name = "unnamed";
            objectName = ObjectNameGenerator.generate(type, name);
        } else {
            objectName = null;
        }
        registerMBean = (objectName != null && annotation.register());
        if (registerMBean) {
            try {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                server.registerMBean(this, objectName);
            } catch (InstanceAlreadyExistsException ex) {
                logger.trace("MBean '{}' already registered, ignore.", objectName);
            } catch (Exception ignore) {
            }
        }
    }

    private Logger initializeLogger() {
        GoblinManagedLogger annotation = AnnotationUtils.getAnnotation(getClass(), GoblinManagedLogger.class);
        String name = null;
        if (annotation != null) {
            name = annotation.name();
        }
        if (StringUtils.isBlank(name)) {
            return LoggerFactory.getLogger(getClass());
        } else {
            return LoggerFactory.getLogger(name.trim());
        }
    }

    private void initializeStopWatch() {
        GoblinManagedStopWatch annotation = AnnotationUtils.getAnnotation(getClass(), GoblinManagedStopWatch.class);
        if (annotation == null) {
            return;
        }
        StopWatch stopWatch = annotation.autoStart() ? new StopWatch(true) : new StopWatch(false);
        stopWatchReference.set(stopWatch);
    }

    @Nullable
    public StopWatch getStopWatch() {
        return stopWatchReference.get();
    }

    @Override
    public ObjectName getObjectName() {
        return objectName;
    }

    @Override
    public void initialize() {
        if (initialized.compareAndSet(false, true)) {
            initializeBean();
        }
    }

    @Override
    public void dispose() {
        if (disposed.compareAndSet(false, true)) {
            StopWatch stopWatch = stopWatchReference.get();
            if (stopWatch != null) {
                stopWatch.stop();
            }
            if (registerMBean) {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                if (server.isRegistered(objectName)) {
                    try {
                        server.unregisterMBean(objectName);
                    } catch (InstanceNotFoundException e) {
                        logger.trace("MBean '{}' not found, ignore.", objectName);
                    } catch (Exception ignore) {
                    }
                }
            }
            disposeBean();
        }
    }

    protected void initializeBean() {
    }

    protected void disposeBean() {
    }
}
