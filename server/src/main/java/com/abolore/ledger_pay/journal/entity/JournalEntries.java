package com.abolore.ledger_pay.journal.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "journal_entries")
public class JournalEntries {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    @Column(name = "source_type", nullable = false, length = 40)
    private String sourceType;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "source_event_id", nullable = false)
    private UUID sourceEventId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static class Builder {
        private String reference;
        private String sourceType;
        private UUID sourceId;
        private UUID sourceEventId;

        public Builder() {
        }

        public JournalEntries build() {
            return new JournalEntries(this);
        }

        public Builder setReference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder setSourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public Builder setSourceId(UUID sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder setSourceEventId(UUID sourceEventId) {
            this.sourceEventId = sourceEventId;
            return this;
        }

    }

    private JournalEntries(Builder builder) {
        this.reference = builder.reference;
        this.sourceType = builder.sourceType;
        this.sourceId = builder.sourceId;
        this.sourceEventId = builder.sourceEventId;
        this.createdAt = Instant.now();
    }

    protected JournalEntries() {
    }

    public UUID getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public String getSourceType() {
        return sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getSourceEventId() {
        return sourceEventId;
    }

}