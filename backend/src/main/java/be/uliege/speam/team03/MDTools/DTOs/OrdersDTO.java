package be.uliege.speam.team03.MDTools.DTOs;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrdersDTO {
    private Long id;
    private String name;
    private Boolean isExported;
    private Timestamp creationDate;
    private Timestamp exportDate;
}
