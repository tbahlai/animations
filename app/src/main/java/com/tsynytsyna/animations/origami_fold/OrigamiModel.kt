package com.tsynytsyna.animations.origami_fold

import androidx.compose.ui.graphics.Color

enum class OrigamiTheme(
    val title: String,
    val paperColor: Color,
    val inkColor: Color,
    val accentColor: Color,
    val secondaryInk: Color,
    val creaseColor: Color,
    val deskColor: Color
) {
    WASHI(
        title = "Washi Paper",
        paperColor = Color(0xFFFDFBF7),
        inkColor = Color(0xFF1C1917),
        accentColor = Color(0xFFD32F2F),
        secondaryInk = Color(0xFF78716C),
        creaseColor = Color(0xFF292524),
        deskColor = Color(0xFF0C0A09)
    ),
    BLUEPRINT(
        title = "Tokyo Blueprint",
        paperColor = Color(0xFF0F1E36),
        inkColor = Color(0xFFE2E8F0),
        accentColor = Color(0xFF00E5FF),
        secondaryInk = Color(0xFF64748B),
        creaseColor = Color(0xFF020617),
        deskColor = Color(0xFF030712)
    ),
    VINTAGE(
        title = "Cartography",
        paperColor = Color(0xFFF4EAD4),
        inkColor = Color(0xFF332211),
        accentColor = Color(0xFFB8860B),
        secondaryInk = Color(0xFF8B6508),
        creaseColor = Color(0xFF2E1C0C),
        deskColor = Color(0xFF140D07)
    ),
    BAUHAUS(
        title = "Bauhaus Style",
        paperColor = Color(0xFFF0EFEB),
        inkColor = Color(0xFF111111),
        accentColor = Color(0xFFE63946),
        secondaryInk = Color(0xFF457B9D),
        creaseColor = Color(0xFF1D3557),
        deskColor = Color(0xFF0A0908)
    )
}

enum class FoldPreset(
    val targetAngle: Float,
    val label: String
) {
    FLAT(0f, "FLAT 0°"),
    ACCORDION(42f, "ACCORDION 42°"),
    Z_FOLD(65f, "Z-FOLD 65°"),
    CLOSED(84f, "CLOSED 84°")
}

data class PanelContent(
    val sectionNumber: String,
    val headerTitle: String,
    val subHeader: String,
    val bodyParagraph: String,
    val badgeLabel: String,
    val coordinates: String,
    val metaTags: List<String>
)

object OrigamiData {
    val frontPanels = listOf(
        PanelContent(
            sectionNumber = "01 / ARCHIVE",
            headerTitle = "KYOTO TRANSIT",
            subHeader = "Tokaido Shinkansen Route",
            bodyParagraph = "High-precision geometric folding guide for subterranean transit nodes and high-speed rail intersections. Keep folded when navigating crowded stations.",
            badgeLabel = "SECTOR A-4",
            coordinates = "35.0116° N, 135.7681° E",
            metaTags = listOf("ELEV: 52M", "GATE 12", "PLATFORM 3")
        ),
        PanelContent(
            sectionNumber = "02 / CENTRAL",
            headerTitle = "ORIGAMI FOLD",
            subHeader = "Structural Geometry & Creases",
            bodyParagraph = "Every crease functions as an elastic spatial hinge. Ambient occlusion shadows multiply along the inner vertex as dihedral angles compress towards zero.",
            badgeLabel = "RATIO 1:√2",
            coordinates = "MIURA-ORI PATTERN",
            metaTags = listOf("TESSELLATION", "ISOMETRIC", "RIGID FOLD")
        ),
        PanelContent(
            sectionNumber = "03 / TELEMETRY",
            headerTitle = "FIELD METRICS",
            subHeader = "Sensor Log & Topography",
            bodyParagraph = "Topographical elevation gradient vectors plotted across the transverse axis. Dynamic paper thickness: 120 gsm washi mulberry fiber weave.",
            badgeLabel = "EXPEDITION OK",
            coordinates = "ALT: 840M — PEAK",
            metaTags = listOf("BEARING 240°", "HUMIDITY 48%", "VERIFIED")
        )
    )

    val backPanels = listOf(
        PanelContent(
            sectionNumber = "04 / REVERSE",
            headerTitle = "SECRET PASS",
            subHeader = "Sub-surface Walking Trail",
            bodyParagraph = "Bypassing the main terminal boulevard via shaded bamboo groves and dry stone zen gardens. Illuminated solely by stone lanterns after dusk.",
            badgeLabel = "NIGHT ROUTE",
            coordinates = "TORII ALIGNMENT",
            metaTags = listOf("QUIET ZONE", "LANTERNS ON", "STEP 120")
        ),
        PanelContent(
            sectionNumber = "05 / SCHEMATIC",
            headerTitle = "SPATIAL GRID",
            subHeader = "Cartesian Coordinate Net",
            bodyParagraph = "Topological map projection preserving conformal angles across paper facets. Stress distribution along fold edges validated under cyclic load.",
            badgeLabel = "TOPOLOGY",
            coordinates = "EPSG:3857 PROJECTED",
            metaTags = listOf("CONFORMAL", "SCALE 1:25K", "SURVEYED")
        ),
        PanelContent(
            sectionNumber = "06 / MANIFEST",
            headerTitle = "TRAVEL CREST",
            subHeader = "Official Inspection Seal",
            bodyParagraph = "Authenticated travel document authorized for inter-district traversal. Reverse side contains emergency coordinates and radio frequencies.",
            badgeLabel = "SEAL VALID",
            coordinates = "FREQ: 144.800 MHz",
            metaTags = listOf("STAMPED", "OFFICIAL", "SERIES-X")
        )
    )
}
