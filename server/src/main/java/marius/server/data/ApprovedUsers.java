package marius.server.data;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

@DynamicInsert
@Data
@Entity
@Table(name = "approvedusers")
public class ApprovedUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="email" , unique = true, nullable = false)
    private String email;

    @Column
    private LocalDateTime data;

    public ApprovedUsers() {}
    public ApprovedUsers( String email ) {
        this.email = email;
        //this.data = LocalDateTime.now();
    }
    public Integer getId(){ return this.id; }
    public String getEmail(){ return this.email; }
    public LocalDateTime getData(){ return this.data; }
    public void setId(Integer id){ this.id = id; }
    public void setEmail(String email){ this.email = email; }
    public void setData(LocalDateTime data){ this.data = data; }
}
