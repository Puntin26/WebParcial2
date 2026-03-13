package edu.univ.eventos.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "participant_id"})
})
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(optional = false)
    @JoinColumn(name = "participant_id")
    private User participant;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "validation_token", nullable = false, unique = true)
    private String validationToken;

    @Column(name = "qr_payload", nullable = false, length = 1024)
    private String qrPayload;

    @Column(nullable = false)
    private boolean attended = false;

    @Column(name = "attended_at")
    private LocalDateTime attendedAt;

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public User getParticipant() { return participant; }
    public void setParticipant(User participant) { this.participant = participant; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public String getValidationToken() { return validationToken; }
    public void setValidationToken(String validationToken) { this.validationToken = validationToken; }
    public String getQrPayload() { return qrPayload; }
    public void setQrPayload(String qrPayload) { this.qrPayload = qrPayload; }
    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }
    public LocalDateTime getAttendedAt() { return attendedAt; }
    public void setAttendedAt(LocalDateTime attendedAt) { this.attendedAt = attendedAt; }
}
