package be.uliege.speam.team03.MDTools.models;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer user_id;

    private String username;
    private String email;
    private String password;
    private Timestamp created_at;
    private Timestamp updated_at;
    private String role_name;
    private String job_position;
    private String workplace;

    public Users(){}

    public Users(String username, String email, String pw, Timestamp creation, Timestamp update,String role, String job, String workplace){
        this.username = username;
        this.email = email;
        this.password = pw;
        this.created_at = creation;
        this.updated_at = update;
        this.role_name = role;
        this.job_position = job;
        this.workplace = workplace;
    }
    
    public Integer getId(){
        return this.user_id;
    }
    public String getUsername(){
        return this.username;
    }
    public String getEmail(){
        return this.email;
    }
    public String getPw(){
        return this.password;
    }
    public Timestamp getCreationDate(){
        return this.created_at;
    }
    public Timestamp getUpdateDate(){
        return this.updated_at;
    }
    public String getRole(){
        return this.role_name;
    }
    public String getJob(){
        return this.job_position;
    }
    public String getWorkplace(){
        return this.workplace;
    }
}
