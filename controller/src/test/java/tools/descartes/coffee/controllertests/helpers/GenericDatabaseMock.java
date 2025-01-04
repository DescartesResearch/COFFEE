package tools.descartes.coffee.controllertests.helpers;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;

import java.util.List;

public class GenericDatabaseMock<T> implements GenericDatabaseService<T> {

    private int findCounter;
    private int addCounter;
    private int countCounter;

    public GenericDatabaseMock() {
        findCounter = 0;
        addCounter = 0;
        countCounter = 0;
    }

    public int getFindCounter() {
        return findCounter;
    }

    public int getAddCounter() {
        return addCounter;
    }

    public int getCountCounter() {
        return countCounter;
    }

    @Override
    public List<T> findAll() {
        findCounter++;
        return null;
    }

    @Override
    public void add(T entry) {
        addCounter++;
    }

    @Override
    public Long count() {
        countCounter++;
        return null;
    }


}
