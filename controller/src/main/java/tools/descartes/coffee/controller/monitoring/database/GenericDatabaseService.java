package tools.descartes.coffee.controller.monitoring.database;

import java.util.List;

public interface GenericDatabaseService<T> {
    List<T> findAll();

    void add(T entry);

    Long count();
}
