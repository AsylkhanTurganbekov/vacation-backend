package com.company.vacation.service;

import com.company.vacation.dto.certificate.TripCertificateResponse;

public interface TripCertificateService {

    TripCertificateResponse getCertificate(Long tripId);

    String renderCertificateHtml(Long tripId);

    String renderCertificatePdfHtml(Long tripId);
}
