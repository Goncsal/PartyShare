package tqs.backend.tqsbackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "description", nullable = false, length = 4096)
    private String description;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportState state;

    public Report() {
    }

    public Report(Long senderId, String title, String description) {
        this.senderId = senderId;
        this.title = title;
        this.description = description;
        this.state = ReportState.NEW;
    }
    
    public Long getId() { return id; }
    
    public Long getSenderId() { return senderId; }
    
    public String getTitle() { return title; }
    
    public String getDescription() { return description; }
    
    public ReportState getState() { return state; }
    
    public void setId(Long id) { this.id = id; }
    
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    
    public void setTitle(String title) { this.title = title; }
    
    public void setDescription(String description) { this.description = description; }
    
    public void setState(ReportState state) { this.state = state; }
}
