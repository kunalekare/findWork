package com.skillbridge.backend.invoice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Server-Side GST Tax Invoice Generation Service
 * IMPORTANT: The SAC code and GSTIN are configuration placeholders to be replaced with the
 * registered entity's real values before official production launch.
 */
@Service
public class GstInvoiceService {

    private final String gstin;
    private final String sacCode;
    private final double gstRatePercent;
    private final boolean isProduction;

    public record ServerGstInvoice(
        String invoiceNumber,
        String issueDate,
        String jobId,
        String jobTitle,
        String gstin,
        String sacCode,
        long baseAmountPaise,
        long cgstPaise,
        long sgstPaise,
        long totalAmountPaise,
        String workerName,
        String taskPosterName,
        boolean isPlaceholderMode,
        String complianceDisclaimer
    ) {}

    public GstInvoiceService(
        @Value("${gst.invoice.gstin:GSTIN-NOT-CONFIGURED-PILOT-ONLY}") String gstin,
        @Value("${gst.invoice.sac-code:SAC-NOT-CONFIGURED}") String sacCode,
        @Value("${gst.rate.percent:18.0}") double gstRatePercent,
        @Value("${app.is-production:false}") boolean isProduction
    ) {
        this.gstin = gstin;
        this.sacCode = sacCode;
        this.gstRatePercent = gstRatePercent;
        this.isProduction = isProduction;
    }

    public GstInvoiceService(String gstin, String sacCode, double gstRatePercent) {
        this(gstin, sacCode, gstRatePercent, false);
    }

    public ServerGstInvoice generateInvoice(
        String jobId,
        String jobTitle,
        long totalPaise,
        String workerName,
        String taskPosterName
    ) {
        if (isProduction && (gstin.contains("NOT-CONFIGURED") || sacCode.contains("NOT-CONFIGURED"))) {
            throw new IllegalStateException("GST invoice generation blocked: valid corporate GSTIN and SAC required in production profile");
        }

        // 18% inclusive GST calculation: Base = Total / (1 + Rate/100)
        double rateFactor = 1.0 + (gstRatePercent / 100.0);
        long baseAmountPaise = (long) (totalPaise / rateFactor);
        long totalGstPaise = totalPaise - baseAmountPaise;
        long cgstPaise = totalGstPaise / 2;
        long sgstPaise = totalGstPaise - cgstPaise;

        String invoiceNumber = "SB-INV-" + LocalDate.now().getYear() + "-" + jobId.substring(Math.max(0, jobId.length() - 4)).toUpperCase(Locale.ROOT);
        String issueDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

        return new ServerGstInvoice(
            invoiceNumber,
            issueDate,
            jobId,
            jobTitle,
            gstin,
            sacCode,
            baseAmountPaise,
            cgstPaise,
            sgstPaise,
            totalPaise,
            workerName,
            taskPosterName,
            gstin.contains("NOT-CONFIGURED"), // Test mode placeholder flag
            "PILOT NOTICE: SAC code (" + sacCode + ") and GSTIN (" + gstin + ") are TEST-MODE PLACEHOLDERS. Valid corporate registration required before commercial release."
        );
    }
}
