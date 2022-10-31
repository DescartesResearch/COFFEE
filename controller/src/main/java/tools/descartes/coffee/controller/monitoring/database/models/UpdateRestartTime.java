package tools.descartes.coffee.controller.monitoring.database.models;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import tools.descartes.coffee.controller.procedure.collection.deployment.UpdateContainer;

@Entity
public class UpdateRestartTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** update number to which this entry refers */
    public int updateNo;

    /** time when a container with the old image is shut down */
    public Timestamp shutDownTime;

    /** time when a container with the new image has been started */
    public Timestamp restartTime;

    public long timeToRestart;

    protected UpdateRestartTime() {
    }

    public UpdateRestartTime(Timestamp shutDown, Timestamp restart) {
        this.updateNo = UpdateContainer.getCurrentUpdateCount();
        this.shutDownTime = shutDown;
        this.restartTime = restart;
        timeToRestart = restart.getTime() - shutDown.getTime();
    }

    @Override
    public String toString() {
        return String.format(
                "UpdateRestartTime[id=%d, updateNo=%d, shutDownTime='%s', restartTime='%s']",
                id, updateNo, shutDownTime, restartTime);
    }

    public int getUpdateNo() {
        return updateNo;
    }

    public Timestamp getShutDownTime() {
        return shutDownTime;
    }

    public Timestamp getRestartTime() {
        return restartTime;
    }
}