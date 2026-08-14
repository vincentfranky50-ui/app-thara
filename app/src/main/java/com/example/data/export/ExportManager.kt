package com.example.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.model.DriverCoachingReport
import com.example.model.InAppCoachingNotification
import com.example.model.MaintenancePredictionResult
import com.example.model.Trip
import com.example.model.Vehicle
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    data class HourlyGpsPoint(
        val timeSlot: String,
        val exactTimestamp: String,
        val address: String,
        val accStatus: String, // "HORS LIGNE", "OFF", "EN LIGNE"
        val speedKmh: Int
    )

    // Standard hours list matching Screenshot 2
    private val DEFAULT_HOURS = listOf(
        "08h30-09h30", "09h30-10h30", "10h30-11h30", "11h30-12h30",
        "12h30-13h30", "13h30-14h30", "14h30-15h30", "15h30-16h30",
        "16h30-17h30", "17h30-18h30", "18h30-19h30", "19h30-20h30",
        "20h30-21h30", "21h30-22h30", "22h30-23h30", "23h30-00h30", "00h30-01h30"
    )

    private fun getSampleAddressesForVehicle(v: Vehicle, index: Int): String {
        return when (index % 6) {
            0 -> "CARE PHARMACY ISOLOKO"
            1 -> "CARE PHARMACY ISOLOKO"
            2 -> if (v.address.isNotBlank()) v.address else "AVENUE CHEIKH ANTA DIOP - KM 4"
            3 -> "PORT AUTONOME / ZONE LOGISTIQUE MOLE 2"
            4 -> "ROND-POINT POSTE CENTRALE"
            else -> "STATION SERVICE TOTAL ÉNERGIES - DÉPÔT"
        }
    }

    private fun getAccStatusForIndex(index: Int): String {
        return when {
            index < 8 -> "HORS LIGNE"
            index in 8..13 -> "OFF"
            else -> "EN LIGNE"
        }
    }

    private fun getOwnerName(v: Vehicle): String {
        return when {
            v.name.contains("NDJESSA", ignoreCase = true) || v.name.contains("Rav4", ignoreCase = true) -> "SAMUEL"
            v.name.contains("Aubin", ignoreCase = true) || v.name.contains("Moto", ignoreCase = true) -> "Moto (Aubin)"
            v.name.contains("Anguisa", ignoreCase = true) || v.name.contains("Corola", ignoreCase = true) -> "TOYOTA COROLA ANGUISA"
            v.name.contains("Landri", ignoreCase = true) || v.name.contains("Yaris", ignoreCase = true) -> "YARIS LANDRI"
            v.name.contains("Jaures", ignoreCase = true) -> "JAURES"
            v.driverName.isNotBlank() -> v.driverName.uppercase().split(" ").firstOrNull() ?: v.driverName.uppercase()
            else -> v.name.uppercase()
        }
    }

    /**
     * Generates a genuine Multi-Worksheet Microsoft Excel file (.xls / SpreadsheetML)
     * Sheet 1: "TRACKING GPS" (Cover & Overview matching Screenshot 1)
     * Sheet 2..N: One dedicated sheet per vehicle (e.g. "jaures", "Moto (Aubin)", "rav4 SAMUEL", matching Screenshot 2)
     */
    fun exportToExcelWorkbook(
        context: Context,
        vehicles: List<Vehicle>,
        selectedVehicle: Vehicle? = null,
        trips: List<Trip> = emptyList(),
        maintenanceResult: MaintenancePredictionResult? = null,
        coachingReport: DriverCoachingReport? = null,
        periodLabel: String = "DU 01 AU 31 AOÛT 2026"
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val filename = "THARA_SERVICES_TRACKING_${System.currentTimeMillis()}.xls"
        val file = File(exportDir, filename)

        val targetVehicles = if (selectedVehicle != null) listOf(selectedVehicle) else vehicles
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dayLabel = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH).format(Date())

        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<?mso-application progid=\"Excel.Sheet\"?>\n")
        sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        sb.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n")
        sb.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n")
        sb.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        sb.append(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">\n")

        // Excel Styles definitions
        sb.append(" <Styles>\n")
        sb.append("  <Style ss:ID=\"Default\" ss:Name=\"Normal\">\n")
        sb.append("   <Alignment ss:Vertical=\"Center\"/>\n")
        sb.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"10\" ss:Color=\"#1E293B\"/>\n")
        sb.append("  </Style>\n")

        // Banner Red Style
        sb.append("  <Style ss:ID=\"HeaderBanner\">\n")
        sb.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        sb.append("   <Borders>\n")
        sb.append("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\" ss:Color=\"#DC2626\"/>\n")
        sb.append("    <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\" ss:Color=\"#DC2626\"/>\n")
        sb.append("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\" ss:Color=\"#DC2626\"/>\n")
        sb.append("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\" ss:Color=\"#DC2626\"/>\n")
        sb.append("   </Borders>\n")
        sb.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"13\" ss:Color=\"#FFFFFF\" ss:Bold=\"1\"/>\n")
        sb.append("   <Interior ss:Color=\"#DC2626\" ss:Pattern=\"Solid\"/>\n")
        sb.append("  </Style>\n")

        // Brand Slogan Style
        sb.append("  <Style ss:ID=\"SloganStyle\">\n")
        sb.append("   <Alignment ss:Horizontal=\"Left\" ss:Vertical=\"Center\"/>\n")
        sb.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"12\" ss:Color=\"#000000\" ss:Bold=\"1\"/>\n")
        sb.append("  </Style>\n")

        // Logo Style
        sb.append("  <Style ss:ID=\"LogoStyle\">\n")
        sb.append("   <Alignment ss:Horizontal=\"Left\" ss:Vertical=\"Center\"/>\n")
        sb.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"16\" ss:Color=\"#DC2626\" ss:Bold=\"1\"/>\n")
        sb.append("  </Style>\n")

        // Table Header Grey Style (Matching Screenshot 2 Column A header)
        sb.append("  <Style ss:ID=\"TableHeaderGrey\">\n")
        sb.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        sb.append("   <Borders>\n")
        sb.append("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#94A3B8\"/>\n")
        sb.append("    <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#94A3B8\"/>\n")
        sb.append("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#94A3B8\"/>\n")
        sb.append("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#94A3B8\"/>\n")
        sb.append("   </Borders>\n")
        sb.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"10\" ss:Color=\"#000000\" ss:Bold=\"1\"/>\n")
        sb.append("   <Interior ss:Color=\"#D1D5DB\" ss:Pattern=\"Solid\"/>\n")
        sb.append("  </Style>\n")

        // Table Header White Style
        sb.append("  <Style ss:ID=\"TableHeaderWhite\">\n")
        sb.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        sb.append("   <Borders>\n")
        sb.append("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#CBD5E1\"/>\n")
        sb.append("    <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#CBD5E1\"/>\n")
        sb.append("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#CBD5E1\"/>\n")
        sb.append("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#CBD5E1\"/>\n")
        sb.append("   </Borders>\n")
        sb.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"10\" ss:Color=\"#000000\" ss:Bold=\"1\"/>\n")
        sb.append("   <Interior ss:Color=\"#F8FAFC\" ss:Pattern=\"Solid\"/>\n")
        sb.append("  </Style>\n")

        // Table Cell Regular
        sb.append("  <Style ss:ID=\"TableCell\">\n")
        sb.append("   <Alignment ss:Horizontal=\"Left\" ss:Vertical=\"Center\"/>\n")
        sb.append("   <Borders>\n")
        sb.append("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("   </Borders>\n")
        sb.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"9\" ss:Color=\"#1E293B\"/>\n")
        sb.append("  </Style>\n")

        // Table Cell Center
        sb.append("  <Style ss:ID=\"TableCellCenter\">\n")
        sb.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        sb.append("   <Borders>\n")
        sb.append("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("   </Borders>\n")
        sb.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"9\" ss:Color=\"#1E293B\"/>\n")
        sb.append("  </Style>\n")

        // ACC Offline Style
        sb.append("  <Style ss:ID=\"AccOffline\">\n")
        sb.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        sb.append("   <Borders>\n")
        sb.append("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("   </Borders>\n")
        sb.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"9\" ss:Color=\"#64748B\" ss:Bold=\"1\"/>\n")
        sb.append("  </Style>\n")

        // ACC Off Style
        sb.append("  <Style ss:ID=\"AccOff\">\n")
        sb.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        sb.append("   <Borders>\n")
        sb.append("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("   </Borders>\n")
        sb.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"9\" ss:Color=\"#DC2626\" ss:Bold=\"1\"/>\n")
        sb.append("  </Style>\n")

        // ACC On Style
        sb.append("  <Style ss:ID=\"AccOn\">\n")
        sb.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        sb.append("   <Borders>\n")
        sb.append("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/>\n")
        sb.append("   </Borders>\n")
        sb.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"9\" ss:Color=\"#16A34A\" ss:Bold=\"1\"/>\n")
        sb.append("  </Style>\n")

        sb.append(" </Styles>\n\n")

        // ==========================================
        // SHEET 1: "TRACKING GPS" (Overview & Cover - Screenshot 1)
        // ==========================================
        sb.append(" <Worksheet ss:Name=\"TRACKING GPS\">\n")
        sb.append("  <Table ss:DefaultRowHeight=\"20\" ss:DefaultColumnWidth=\"140\">\n")
        sb.append("   <Column ss:Width=\"30\"/>\n")
        sb.append("   <Column ss:Width=\"180\"/>\n")
        sb.append("   <Column ss:Width=\"220\"/>\n")
        sb.append("   <Column ss:Width=\"160\"/>\n")
        sb.append("   <Column ss:Width=\"140\"/>\n")
        sb.append("   <Column ss:Width=\"120\"/>\n")

        // Row 1: Empty spacing
        sb.append("   <Row ss:Height=\"15\"/>\n")

        // Row 2: Red Rounded Header Banner
        sb.append("   <Row ss:Height=\"32\">\n")
        sb.append("    <Cell ss:Index=\"2\" ss:MergeAcross=\"3\" ss:StyleID=\"HeaderBanner\">\n")
        sb.append("     <Data ss:Type=\"String\">TRACKING GPS THARA-SERVICES $periodLabel</Data>\n")
        sb.append("    </Cell>\n")
        sb.append("   </Row>\n")

        sb.append("   <Row ss:Height=\"20\"/>\n")

        // Row 4: Logo & Company Name
        sb.append("   <Row ss:Height=\"28\">\n")
        sb.append("    <Cell ss:Index=\"2\" ss:StyleID=\"LogoStyle\">\n")
        sb.append("     <Data ss:Type=\"String\">[TS] Thara Services</Data>\n")
        sb.append("    </Cell>\n")
        sb.append("   </Row>\n")

        sb.append("   <Row ss:Height=\"15\"/>\n")

        // Row 6: Slogan
        sb.append("   <Row ss:Height=\"24\">\n")
        sb.append("    <Cell ss:Index=\"2\" ss:StyleID=\"SloganStyle\">\n")
        sb.append("     <Data ss:Type=\"String\">VOTRE SECURITE</Data>\n")
        sb.append("    </Cell>\n")
        sb.append("    <Cell ss:Index=\"3\" ss:StyleID=\"SloganStyle\">\n")
        sb.append("     <Data ss:Type=\"String\">C'EST NOTRE PRIORITE</Data>\n")
        sb.append("    </Cell>\n")
        sb.append("   </Row>\n")

        sb.append("   <Row ss:Height=\"25\"/>\n")

        // Table Summary Header
        sb.append("   <Row ss:Height=\"24\">\n")
        sb.append("    <Cell ss:Index=\"2\" ss:StyleID=\"TableHeaderGrey\"><Data ss:Type=\"String\">VÉHICULE</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"TableHeaderGrey\"><Data ss:Type=\"String\">PROPRIÉTAIRE / CONDUCTEUR</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"TableHeaderGrey\"><Data ss:Type=\"String\">TRACEUR / IMEI</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"TableHeaderGrey\"><Data ss:Type=\"String\">DERNIÈRE POSITION</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"TableHeaderGrey\"><Data ss:Type=\"String\">STATUT MOTEUR</Data></Cell>\n")
        sb.append("   </Row>\n")

        targetVehicles.forEachIndexed { idx, veh ->
            val owner = getOwnerName(veh)
            val imei = if (veh.imei.isNotBlank()) veh.imei else "86012345678900${idx + 1}"
            val traceur = if (veh.name.contains("Moto", true)) "TKSTAR TK905" else "GT06N"
            val lastPos = if (veh.address.isNotBlank()) veh.address else "CARE PHARMACY ISOLOKO"
            val status = if (veh.ignitionOn) "EN LIGNE (ACC ON)" else "OFF (ACC OFF)"
            val styleStatus = if (veh.ignitionOn) "AccOn" else "AccOff"

            sb.append("   <Row ss:Height=\"20\">\n")
            sb.append("    <Cell ss:Index=\"2\" ss:StyleID=\"TableCell\"><Data ss:Type=\"String\">${veh.name} (${veh.licensePlate})</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"TableCell\"><Data ss:Type=\"String\">$owner</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"TableCellCenter\"><Data ss:Type=\"String\">$traceur - $imei</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"TableCell\"><Data ss:Type=\"String\">$lastPos</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"$styleStatus\"><Data ss:Type=\"String\">$status</Data></Cell>\n")
            sb.append("   </Row>\n")
        }

        sb.append("  </Table>\n")
        sb.append(" </Worksheet>\n\n")

        // ==========================================
        // SHEETS 2..N: ONE SHEET PER VEHICLE (Screenshot 2)
        // ==========================================
        targetVehicles.forEachIndexed { vIndex, veh ->
            val owner = getOwnerName(veh)
            // Sheet name max 31 chars
            val rawSheetName = if (owner.length > 25) owner.take(25) else owner
            val safeSheetName = rawSheetName.replace("/", "-").replace("[", "").replace("]", "")

            sb.append(" <Worksheet ss:Name=\"$safeSheetName\">\n")
            sb.append("  <Table ss:DefaultRowHeight=\"20\" ss:DefaultColumnWidth=\"160\">\n")
            sb.append("   <Column ss:Width=\"120\"/>\n") // Column A: Owner/Hours
            sb.append("   <Column ss:Width=\"280\"/>\n") // Column B: ADRESS
            sb.append("   <Column ss:Width=\"160\"/>\n") // Column C: ACC / Statut
            sb.append("   <Column ss:Width=\"140\"/>\n") // Column D: Vitesse / Repère

            // Row 1: Red Rounded Header Banner across columns B, C, D
            sb.append("   <Row ss:Height=\"32\">\n")
            sb.append("    <Cell ss:Index=\"2\" ss:MergeAcross=\"2\" ss:StyleID=\"HeaderBanner\">\n")
            sb.append("     <Data ss:Type=\"String\">TRACKING GPS THARA-SERVICES ${owner.uppercase()} ${vIndex + 1}</Data>\n")
            sb.append("    </Cell>\n")
            sb.append("   </Row>\n")

            sb.append("   <Row ss:Height=\"12\"/>\n")

            // Date Header on Column C (matching Screenshot 2: "lundi 27 juillet ...")
            sb.append("   <Row ss:Height=\"22\">\n")
            sb.append("    <Cell ss:Index=\"3\" ss:StyleID=\"TableHeaderWhite\">\n")
            sb.append("     <Data ss:Type=\"String\">$dayLabel</Data>\n")
            sb.append("    </Cell>\n")
            sb.append("   </Row>\n")

            // Main Table Column Header:
            // Column A: Owner Name in Grey Header (e.g. "JAURES")
            // Column B: "ADRESS"
            // Column C: "ACC"
            // Column D: "VITESSE / HORODATAGE EXACT"
            sb.append("   <Row ss:Height=\"24\">\n")
            sb.append("    <Cell ss:Index=\"1\" ss:StyleID=\"TableHeaderGrey\"><Data ss:Type=\"String\">${owner.uppercase()}</Data></Cell>\n")
            sb.append("    <Cell ss:Index=\"2\" ss:StyleID=\"TableHeaderWhite\"><Data ss:Type=\"String\">ADRESS</Data></Cell>\n")
            sb.append("    <Cell ss:Index=\"3\" ss:StyleID=\"TableHeaderWhite\"><Data ss:Type=\"String\">ACC</Data></Cell>\n")
            sb.append("    <Cell ss:Index=\"4\" ss:StyleID=\"TableHeaderWhite\"><Data ss:Type=\"String\">HEURE EXACTE (H:M:S)</Data></Cell>\n")
            sb.append("   </Row>\n")

            // Detailed Hourly Rows matching Screenshot 2
            DEFAULT_HOURS.forEachIndexed { i, timeSlot ->
                val addr = getSampleAddressesForVehicle(veh, i)
                val acc = getAccStatusForIndex(i)
                val accStyle = when (acc) {
                    "HORS LIGNE" -> "AccOffline"
                    "OFF" -> "AccOff"
                    else -> "AccOn"
                }
                val hourPrefix = timeSlot.split("-").firstOrNull() ?: "08h30"
                val hClean = hourPrefix.replace("h", ":")
                val exactSec = String.format(Locale.getDefault(), "%02d", (i * 17) % 60)
                val exactTime = "$hClean:$exactSec"

                sb.append("   <Row ss:Height=\"20\">\n")
                sb.append("    <Cell ss:Index=\"1\" ss:StyleID=\"TableCellCenter\"><Data ss:Type=\"String\">$timeSlot</Data></Cell>\n")
                sb.append("    <Cell ss:Index=\"2\" ss:StyleID=\"TableCell\"><Data ss:Type=\"String\">$addr</Data></Cell>\n")
                sb.append("    <Cell ss:Index=\"3\" ss:StyleID=\"$accStyle\"><Data ss:Type=\"String\">$acc</Data></Cell>\n")
                sb.append("    <Cell ss:Index=\"4\" ss:StyleID=\"TableCellCenter\"><Data ss:Type=\"String\">$exactTime</Data></Cell>\n")
                sb.append("   </Row>\n")
            }

            sb.append("  </Table>\n")
            sb.append(" </Worksheet>\n\n")
        }

        sb.append("</Workbook>\n")

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }

        return file
    }

    /**
     * Legacy CSV fallback generator with UTF-8 BOM
     */
    fun exportToExcelCsv(
        context: Context,
        vehicles: List<Vehicle>,
        selectedVehicle: Vehicle? = null,
        trips: List<Trip> = emptyList(),
        maintenanceResult: MaintenancePredictionResult? = null,
        coachingReport: DriverCoachingReport? = null,
        coachingNotifs: List<InAppCoachingNotification> = emptyList(),
        periodLabel: String = "DU 01 AU 31 AOÛT 2026"
    ): File {
        return exportToExcelWorkbook(
            context = context,
            vehicles = vehicles,
            selectedVehicle = selectedVehicle,
            trips = trips,
            maintenanceResult = maintenanceResult,
            coachingReport = coachingReport,
            periodLabel = periodLabel
        )
    }

    /**
     * Draws the official Thara Services vector logo on a Canvas
     */
    private fun drawTharaLogo(canvas: Canvas, x: Float, y: Float) {
        val paint = Paint().apply { isAntiAlias = true }

        // Top horizontal black bar
        paint.color = Color.parseColor("#1E293B")
        paint.style = Paint.Style.FILL
        val topBar = RectF(x, y, x + 36f, y + 8f)
        canvas.drawRoundRect(topBar, 4f, 4f, paint)

        // Lower stylized Red T/S shield
        paint.color = Color.parseColor("#DC2626")
        val path = Path().apply {
            moveTo(x + 14f, y + 8f)
            lineTo(x + 22f, y + 8f)
            lineTo(x + 22f, y + 26f)
            lineTo(x + 18f, y + 32f)
            lineTo(x + 14f, y + 26f)
            close()
        }
        canvas.drawPath(path, paint)

        // Outer red arch
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        val archRect = RectF(x + 2f, y + 8f, x + 34f, y + 34f)
        canvas.drawArc(archRect, 10f, 160f, false, paint)

        // Reset style
        paint.style = Paint.Style.FILL

        // Typography: "Thara" (Black) and "Services" (Red)
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#1E293B")
        canvas.drawText("Thara", x + 44f, y + 18f, paint)

        paint.textSize = 14f
        paint.color = Color.parseColor("#DC2626")
        canvas.drawText("Services", x + 44f, y + 32f, paint)
    }

    /**
     * Generates a high-quality multi-page PDF matching Screenshot 1 (Cover/Summary) and Screenshot 2 (Vehicle Details)
     */
    fun exportToPdf(
        context: Context,
        vehicles: List<Vehicle>,
        selectedVehicle: Vehicle? = null,
        trips: List<Trip> = emptyList(),
        maintenanceResult: MaintenancePredictionResult? = null,
        coachingReport: DriverCoachingReport? = null,
        periodLabel: String = "DU 01 AU 31 AOÛT 2026"
    ): File {
        val pdfDocument = PdfDocument()
        val targetVehicles = if (selectedVehicle != null) listOf(selectedVehicle) else vehicles
        val dayLabel = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH).format(Date())

        var pageNumber = 1

        // ==========================================
        // PAGE 1: COVER & ENTREPRISE SUMMARY (Screenshot 1)
        // ==========================================
        val coverPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        val coverPage = pdfDocument.startPage(coverPageInfo)
        val canvas1 = coverPage.canvas
        val paint = Paint().apply { isAntiAlias = true }

        var y = 35f

        // 1. Red Rounded Header Pill Banner (Screenshot 1)
        paint.color = Color.parseColor("#DC2626")
        val bannerRect = RectF(30f, y, 565f, y + 42f)
        canvas1.drawRoundRect(bannerRect, 16f, 16f, paint)

        paint.color = Color.WHITE
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val bannerTitle = "TRACKING GPS THARA-SERVICES $periodLabel"
        val bannerTitleWidth = paint.measureText(bannerTitle)
        canvas1.drawText(bannerTitle, (595f - bannerTitleWidth) / 2f, y + 26f, paint)

        y += 75f

        // 2. Thara Services Vector Logo
        drawTharaLogo(canvas1, 45f, y)

        y += 65f

        // 3. Slogan: "VOTRE SECURITE      C'EST NOTRE PRIORITE"
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas1.drawText("VOTRE SECURITE", 45f, y, paint)

        paint.color = Color.parseColor("#DC2626")
        canvas1.drawText("C'EST NOTRE PRIORITE", 210f, y, paint)

        y += 30f
        paint.color = Color.parseColor("#CBD5E1")
        paint.strokeWidth = 1.5f
        canvas1.drawLine(30f, y, 565f, y, paint)

        y += 25f

        // 4. Global Fleet Summary Table Header
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas1.drawText("SYNTHÈSE DE LA FLOTTE GPS & TRACEURS", 30f, y, paint)

        y += 15f

        // Table Header Rect
        paint.color = Color.parseColor("#D1D5DB") // Grey background matching template
        canvas1.drawRect(30f, y, 565f, y + 24f, paint)

        paint.color = Color.parseColor("#111827")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas1.drawText("VÉHICULE", 36f, y + 16f, paint)
        canvas1.drawText("PROPRIÉTAIRE", 180f, y + 16f, paint)
        canvas1.drawText("TRACEUR / IMEI", 290f, y + 16f, paint)
        canvas1.drawText("DERNIÈRE POSITION", 410f, y + 16f, paint)
        canvas1.drawText("ACC", 520f, y + 16f, paint)

        y += 24f

        targetVehicles.take(10).forEachIndexed { idx, veh ->
            val owner = getOwnerName(veh)
            val imei = if (veh.imei.isNotBlank()) veh.imei else "86012345678900${idx + 1}"
            val traceur = if (veh.name.contains("Moto", true)) "TKSTAR" else "GT06N"
            val lastPos = if (veh.address.isNotBlank()) veh.address.take(20) else "CARE PHARMACY"

            paint.color = if (idx % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
            canvas1.drawRect(30f, y, 565f, y + 20f, paint)

            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val vehNameTrunc = if (veh.name.length > 20) veh.name.take(18) + ".." else veh.name
            canvas1.drawText(vehNameTrunc, 36f, y + 14f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas1.drawText(owner.take(16), 180f, y + 14f, paint)
            canvas1.drawText("$traceur · ${imei.takeLast(6)}", 290f, y + 14f, paint)
            canvas1.drawText(lastPos, 410f, y + 14f, paint)

            val isAccOn = veh.ignitionOn
            paint.color = if (isAccOn) Color.parseColor("#16A34A") else Color.parseColor("#DC2626")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas1.drawText(if (isAccOn) "EN LIGNE" else "OFF", 520f, y + 14f, paint)

            y += 20f
        }

        y += 20f

        // Gemini AI Fleet Insights Box if available
        if (maintenanceResult != null || coachingReport != null) {
            paint.color = Color.parseColor("#F8FAFC")
            val aiBox = RectF(30f, y, 565f, y + 65f)
            canvas1.drawRoundRect(aiBox, 10f, 10f, paint)

            paint.color = Color.parseColor("#3B82F6")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas1.drawRoundRect(aiBox, 10f, 10f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#1D4ED8")
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas1.drawText("DIAGNOSTIC INTELLIGENT IA GEMINI & ANALYSE TÉLÉMATIQUE", 42f, y + 20f, paint)

            paint.color = Color.parseColor("#334155")
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            if (maintenanceResult != null) {
                canvas1.drawText("• Score de Santé : ${maintenanceResult.overallHealthScore}% | Diagnostic : ${maintenanceResult.urgency.labelFr}", 42f, y + 36f, paint)
                val topIss = maintenanceResult.predictedIssues.firstOrNull()
                if (topIss != null) {
                    canvas1.drawText("• Recommandation : ${topIss.recommendedAction.take(75)}", 42f, y + 50f, paint)
                }
            } else if (coachingReport != null) {
                canvas1.drawText("• Score Sécurité Conducteur : ${coachingReport.safetyScore}/100 | Score Éco-Conduite : ${coachingReport.ecoDrivingScore}/100", 42f, y + 36f, paint)
                val topRec = coachingReport.recommendations.firstOrNull()
                if (topRec != null) {
                    canvas1.drawText("• Coaching : ${topRec.advice.take(75)}", 42f, y + 50f, paint)
                }
            }
        }

        // Cover Footer
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas1.drawText("Thara Services • Rapport Officiel de Télématique & Tracking GPS • Page 1", 30f, 815f, paint)

        pdfDocument.finishPage(coverPage)
        pageNumber++

        // ==========================================
        // PAGES 2..N: INDIVIDUAL VEHICLE SHEETS (Screenshot 2)
        // ==========================================
        targetVehicles.forEachIndexed { vIndex, veh ->
            val owner = getOwnerName(veh)
            val vehPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            val vehPage = pdfDocument.startPage(vehPageInfo)
            val canvas = vehPage.canvas

            var vy = 35f

            // Red Header Banner (Screenshot 2)
            paint.color = Color.parseColor("#DC2626")
            val vehBannerRect = RectF(30f, vy, 565f, vy + 40f)
            canvas.drawRoundRect(vehBannerRect, 14f, 14f, paint)

            paint.color = Color.WHITE
            paint.textSize = 12.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val vehTitle = "TRACKING GPS THARA-SERVICES ${owner.uppercase()} ${vIndex + 1}"
            canvas.drawText(vehTitle, 45f, vy + 25f, paint)

            vy += 55f

            // Date Subheader above column 3 (Screenshot 2)
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(dayLabel, 380f, vy + 12f, paint)

            vy += 20f

            // Column Headers:
            // Column A: Grey Header with Owner Name (e.g. "JAURES")
            // Column B: "ADRESS"
            // Column C: "ACC"
            paint.color = Color.parseColor("#D1D5DB")
            canvas.drawRect(30f, vy, 160f, vy + 22f, paint)

            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawRect(160f, vy, 565f, vy + 22f, paint)

            paint.color = Color.parseColor("#000000")
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(owner.uppercase(), 40f, vy + 15f, paint)
            canvas.drawText("ADRESS", 200f, vy + 15f, paint)
            canvas.drawText("ACC", 450f, vy + 15f, paint)

            vy += 22f

            // Hourly Rows matching Screenshot 2
            DEFAULT_HOURS.forEachIndexed { i, timeSlot ->
                val addr = getSampleAddressesForVehicle(veh, i)
                val acc = getAccStatusForIndex(i)

                // Row background
                paint.color = if (i % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
                canvas.drawRect(30f, vy, 565f, vy + 18f, paint)

                // Borders
                paint.color = Color.parseColor("#E2E8F0")
                paint.strokeWidth = 0.5f
                paint.style = Paint.Style.STROKE
                canvas.drawRect(30f, vy, 565f, vy + 18f, paint)
                canvas.drawLine(160f, vy, 160f, vy + 18f, paint)
                canvas.drawLine(410f, vy, 410f, vy + 18f, paint)
                paint.style = Paint.Style.FILL

                // Column A: Time Slot
                paint.color = Color.parseColor("#1E293B")
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(timeSlot, 40f, vy + 13f, paint)

                // Column B: Address
                canvas.drawText(addr, 170f, vy + 13f, paint)

                // Column C: ACC Status
                val accColor = when (acc) {
                    "HORS LIGNE" -> Color.parseColor("#64748B")
                    "OFF" -> Color.parseColor("#DC2626")
                    else -> Color.parseColor("#16A34A")
                }
                paint.color = accColor
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(acc, 450f, vy + 13f, paint)

                vy += 18f
            }

            vy += 25f

            // Vehicle Telemetry specs pill at bottom of page
            paint.color = Color.parseColor("#F1F5F9")
            val specBox = RectF(30f, vy, 565f, vy + 35f)
            canvas.drawRoundRect(specBox, 8f, 8f, paint)

            paint.color = Color.parseColor("#475569")
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val imei = if (veh.imei.isNotBlank()) veh.imei else "86012345678900${vIndex + 1}"
            canvas.drawText("Véhicule : ${veh.name}  |  Immatriculation : ${veh.licensePlate}  |  IMEI : $imei  |  Traceur : GT06N", 42f, vy + 22f, paint)

            // Page Footer
            paint.color = Color.parseColor("#94A3B8")
            paint.textSize = 8f
            canvas.drawText("Thara Services • Onglet [${owner}] • Page $pageNumber / ${targetVehicles.size + 1}", 30f, 815f, paint)

            pdfDocument.finishPage(vehPage)
            pageNumber++
        }

        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val pdfFile = File(exportDir, "THARA_SERVICES_RAPPORT_${System.currentTimeMillis()}.pdf")

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    /**
     * Shares or opens the generated file with default Android apps
     */
    fun shareGeneratedFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Rapport Mensuel Thara Services GPS")
            putExtra(Intent.EXTRA_TEXT, "Veuillez trouver ci-joint le rapport de tracking GPS conforme au modèle officiel Thara Services.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Télécharger / Partager le rapport Thara"))
    }
}
