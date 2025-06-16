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

    public Lynis() {}
    public Lynis( String auditor, String ip ) {
        this.auditor = auditor; this.ip = ip;
    }
    public Lynis( String auditor, String ip, String listIdSkippedTest ) {
        this.auditor = auditor; this.ip = ip; this.listIdSkippedTest = listIdSkippedTest;
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
