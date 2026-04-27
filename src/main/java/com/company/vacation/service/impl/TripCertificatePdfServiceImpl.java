package com.company.vacation.service.impl;

import com.company.vacation.exception.BusinessException;
import com.company.vacation.service.TripCertificatePdfService;
import com.company.vacation.service.TripCertificateService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripCertificatePdfServiceImpl implements TripCertificatePdfService {

    private final TripCertificateService tripCertificateService;

    @Override
    public byte[] generatePdf(Long tripId) {
        String html = tripCertificateService.renderCertificateHtml(tripId);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new BusinessException("Failed to generate trip certificate PDF");
        }
    }
}
