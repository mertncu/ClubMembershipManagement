package com.mertncu.clubmembershipmanagement.module.event.model;

import com.mertncu.clubmembershipmanagement.common.base.BaseEntity;
import java.time.LocalDateTime;

/**
 * A time slot in which a trainer is available for private sessions.
 */
public class TrainerAvailability extends BaseEntity {
    private int           trainerId;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
    private boolean       isBooked;   // true when an appointment occupies this slot

    public TrainerAvailability() {}

    public TrainerAvailability(int trainerId, LocalDateTime slotStart, LocalDateTime slotEnd) {
        this.trainerId = trainerId;
        this.slotStart = slotStart;
        this.slotEnd   = slotEnd;
        this.isBooked  = false;
    }

    public int           getTrainerId()            { return trainerId; }
    public void          setTrainerId(int v)        { this.trainerId = v; }
    public LocalDateTime getSlotStart()             { return slotStart; }
    public void          setSlotStart(LocalDateTime v){ this.slotStart = v; }
    public LocalDateTime getSlotEnd()               { return slotEnd; }
    public void          setSlotEnd(LocalDateTime v) { this.slotEnd = v; }
    public boolean       isBooked()                 { return isBooked; }
    public void          setBooked(boolean v)       { this.isBooked = v; }
}
