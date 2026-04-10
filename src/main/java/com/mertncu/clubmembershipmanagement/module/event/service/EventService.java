package com.mertncu.clubmembershipmanagement.module.event.service;

import com.mertncu.clubmembershipmanagement.module.event.dao.EventDAO;
import com.mertncu.clubmembershipmanagement.module.event.dao.EventRegistrationDAO;
import com.mertncu.clubmembershipmanagement.module.event.model.Event;
import com.mertncu.clubmembershipmanagement.module.event.model.EventRegistration;

import java.util.Optional;

public class EventService {
    private final EventDAO eventDAO;
    private final EventRegistrationDAO registrationDAO;

    public EventService() {
        this.eventDAO = new EventDAO();
        this.registrationDAO = new EventRegistrationDAO();
    }

    public boolean registerForEvent(int userId, int eventId) {
        Optional<Event> eventOpt = eventDAO.findById(eventId);
        if (eventOpt.isEmpty()) {
            System.err.println("Event not found.");
            return false;
        }

        Event e = eventOpt.get();
        if (registrationDAO.isUserRegisteredForEvent(userId, eventId)) {
            System.err.println("User is already registered for this event.");
            return false;
        }

        int currentAttendees = registrationDAO.getRegistrationCountForEvent(eventId);
        if (currentAttendees >= e.getQuota()) {
            System.err.println("Event quota is full.");
            return false;
        }

        EventRegistration reg = new EventRegistration(eventId, userId);
        return registrationDAO.save(reg) != null;
    }

    public boolean cancelRegistration(int userId, int eventId) {
        return registrationDAO.deleteByUserAndEvent(userId, eventId);
    }
}
