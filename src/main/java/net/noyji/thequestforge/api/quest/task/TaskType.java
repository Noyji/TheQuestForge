package net.noyji.thequestforge.api.quest.task;

import java.util.function.Supplier;

public class TaskType<T extends AbstractTask<?>> {
    private final Supplier<T> factory;

    public TaskType(Supplier<T> factory) {
        this.factory = factory;
    }

    public T createInstance() {
        return factory.get();
    }
}
