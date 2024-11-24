package be.uliege.speam.team03.MDTools.models;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer order_id;

    private Integer user_id;
    private Timestamp order_date;

    public Orders(){}

    public Orders(Integer userId, Timestamp date){
        this.user_id = userId;
        this.order_date = date;
    }

    public Integer getId(){
        return this.order_id;
    }
    public Integer getUserId(){
        return this.user_id;
    }
    public Timestamp getDate(){
        return this.order_date;
    }
}
