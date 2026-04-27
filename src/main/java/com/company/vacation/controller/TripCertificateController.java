package com.company.vacation.controller;

import com.company.vacation.dto.certificate.TripCertificateResponse;
import com.company.vacation.service.TripCertificatePdfService;
import com.company.vacation.service.TripCertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/certificate")
@RequiredArgsConstructor
public class TripCertificateController {

    private final TripCertificateService tripCertificateService;
    private final TripCertificatePdfService tripCertificatePdfService;

    @GetMapping
    public TripCertificateResponse getCertificate(@PathVariable Long tripId) {
        return tripCertificateService.getCertificate(tripId);
    }

    @GetMapping(value = "/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getCertificateHtml(@PathVariable Long tripId) {
        return ResponseEntity.ok(tripCertificateService.renderCertificateHtml(tripId));
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getCertificatePdf(@PathVariable Long tripId) {
        byte[] pdf = tripCertificatePdfService.generatePdf(tripId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename("trip-certificate-%d.pdf".formatted(tripId))
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
