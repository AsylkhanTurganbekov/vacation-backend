package com.company.vacation.service;

public interface TripCertificatePdfService {

    byte[] generatePdf(Long tripId);
}
