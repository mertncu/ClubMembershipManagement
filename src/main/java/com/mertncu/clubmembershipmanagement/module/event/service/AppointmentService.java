package com.mertncu.clubmembershipmanagement.module.event.service;

import com.mertncu.clubmembershipmanagement.module.event.dao.AppointmentDAO;
import com.mertncu.clubmembershipmanagement.module.event.model.Appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AppointmentService {
    private final AppointmentDAO appointmentDAO;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
    }

    public boolean bookAppointment(int memberId, int trainerId, LocalDateTime date) {
        if (date.isBefore(LocalDateTime.now())) {
            System.err.println("Cannot book an appointment in the past.");
            return false;
        }
        
        // Ensure no colliding exact dates exist for simplicity
        List<Appointment> existing = appointmentDAO.findByTrainerId(trainerId);
        boolean clash = existing.stream()
                .anyMatch(a -> a.getAppointmentDate().equals(date) && "APPROVED".equals(a.getStatus()));
                
        if (clash) {
             System.err.println("Trainer is already booked at this time.");
             return false;
        }

        Appointment appt = new Appointment(memberId, trainerId, date, "PENDING");
        return appointmentDAO.save(appt) != null;
    }

    public boolean updateAppointmentStatus(int appointmentId, String status) {
        Optional<Appointment> opt = appointmentDAO.findById(appointmentId);
        if (opt.isPresent()) {
            Appointment a = opt.get();
            a.setStatus(status);
            return appointmentDAO.update(a);
        }
        return false;
    }
}
