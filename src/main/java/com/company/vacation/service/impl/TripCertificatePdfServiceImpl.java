package com.company.vacation.service.impl;

import com.company.vacation.exception.BusinessException;
import com.company.vacation.service.TripCertificatePdfService;
import com.company.vacation.service.TripCertificateService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripCertificatePdfServiceImpl implements TripCertificatePdfService {

    private static final String[] PDF_SERIF_FONT_PATHS = {
            "/usr/share/fonts/dejavu/DejaVuSerif.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf"
    };
    private static final String[] PDF_SANS_FONT_PATHS = {
            "/usr/share/fonts/dejavu/DejaVuSans.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
    };

    private final TripCertificateService tripCertificateService;

    @Override
    public byte[] generatePdf(Long tripId) {
        String html = tripCertificateService.renderCertificatePdfHtml(tripId);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            File serifFont = findFirstExistingFont(PDF_SERIF_FONT_PATHS);
            if (serifFont != null) {
                builder.useFont(serifFont, "DejaVu Serif");
            }
            File sansFont = findFirstExistingFont(PDF_SANS_FONT_PATHS);
            if (sansFont != null) {
                builder.useFont(sansFont, "DejaVu Sans");
            }
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new BusinessException("Failed to generate trip certificate PDF");
        }
    }

    private File findFirstExistingFont(String[] paths) {
        for (String path : paths) {
            File font = new File(path);
            if (font.exists()) {
                return font;
            }
        }
        return null;
    }
}
