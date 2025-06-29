package marius.server.data;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "lynis")
public class Lynis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;

    @Column(nullable = false)
    private String auditor;

    @Column(nullable = false)
    private String ip ;

    @Column(name = "list_id_skipped_test", nullable = true)
    private String listIdSkippedTest;

    @Column(nullable = false)
    private Boolean loaded=false; // indicate if the list of skippable test is loaded on the local machine true means that che list loaded false otherwise

    public Lynis() {}
    public Lynis( String auditor, String ip, Boolean loaded) {
        this.auditor = auditor; this.ip = ip; this.loaded = loaded;
    }
    public Lynis( String auditor, String ip, String listIdSkippedTest, Boolean loaded ) {
        this.auditor = auditor; this.ip = ip; this.listIdSkippedTest = listIdSkippedTest; this.loaded = loaded;
    }
    public int getId() {
        return id;
    }

    public String getListIdSkippedTest() {
        return listIdSkippedTest;
    }

    public void setListIdSkippedTest(String listIdSkippedTest) {
        this.listIdSkippedTest = listIdSkippedTest;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }
}
