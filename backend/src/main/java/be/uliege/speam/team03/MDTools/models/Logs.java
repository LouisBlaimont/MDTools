package be.uliege.speam.team03.MDTools.models;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="logs")
public class Logs {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer log_id;

    private Integer user_id;
    private String action;
    private Timestamp timestamp;

    public Logs(){}

    public Logs(Integer user_id, String action, Timestamp timestamp){
        this.user_id = user_id;
        this.action = action;
        this.timestamp = timestamp;
    }

    Integer getId(){
        return this.log_id;
    }

    Integer getUserId(){
        return this.user_id;
    }

    String getAction(){
        return this.action;
    }

    Timestamp getTimestamp(){
        return this.timestamp;
    }

}
