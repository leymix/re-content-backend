package com.recontent.backend.membership.entity;

import com.recontent.backend.common.enums.DurationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "membership_plans")
public class MembershipPlanEntity {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_type", nullable = false, length = 30)
    private DurationType durationType;

    @Column(nullable = false)
    private boolean active;

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public DurationType getDurationType() { return durationType; }
    public void setDurationType(DurationType durationType) { this.durationType = durationType; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
