package com.company.vacation.service.impl;

import com.company.vacation.exception.BusinessException;
import com.company.vacation.service.TripCertificatePdfService;
import com.company.vacation.service.TripCertificateService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.File;
import java.io.ByteArrayOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripCertificatePdfServiceImpl implements TripCertificatePdfService {

    private static final File PDF_SERIF_FONT = new File("/usr/share/fonts/dejavu/DejaVuSerif.ttf");
    private static final File PDF_SANS_FONT = new File("/usr/share/fonts/dejavu/DejaVuSans.ttf");

    private final TripCertificateService tripCertificateService;

    @Override
    public byte[] generatePdf(Long tripId) {
        String html = tripCertificateService.renderCertificatePdfHtml(tripId);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            if (PDF_SERIF_FONT.exists()) {
                builder.useFont(PDF_SERIF_FONT, "DejaVu Serif");
            }
            if (PDF_SANS_FONT.exists()) {
                builder.useFont(PDF_SANS_FONT, "DejaVu Sans");
            }
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new BusinessException("Failed to generate trip certificate PDF");
        }
    }
}
