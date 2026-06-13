package ui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.CollectionCenter;
import model.Drone;
import model.DroneBase;
import model.Hospital;
import model.MapModel;
import model.MedicalSite;
import model.Mission;
import model.Position;
import model.Route;
import model.Triangle;
import model.VoronoiCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Map canvas for the MEDADRONE application.
 *
 * Displays:
 *  - Voronoi zones (colored dots per zone)
 *  - Delaunay triangulation edges
 *  - Hospitals (green circles), Collection Centers (orange squares)
 *  - Drone bases (hexagons), User points / doctors (purple diamonds)
 *  - Mission route and animated drone
 *  - Info panels on click, stats panel, triangle info panel
 *
 * Interactions:
 *  - Click on site  -> info panel
 *  - Click on base  -> base info panel
 *  - Click on triangle centroid -> triangle info panel
 *  - Drag site or base -> move it
 *  - Drag user point -> move it
 *  - Mouse wheel -> zoom
 *  - Middle/right drag -> pan
 */
public class MapCanvas extends Canvas {

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color BG          = Color.rgb(10,  20,  40);
    private static final Color GRID        = Color.rgb(20,  36,  68);
    private static final Color COL_HOSP    = Color.rgb(0,   220, 130);
    private static final Color COL_CENTER  = Color.rgb(255, 165, 50);
    private static final Color COL_BASE    = Color.rgb(140, 100, 255);
    private static final Color COL_DRONE   = Color.rgb(80,  180, 255);
    private static final Color COL_ROUTE   = Color.rgb(255, 80,  80);
    private static final Color COL_DEL     = Color.rgb(100, 160, 255, 0.75);
    private static final Color COL_TEXT    = Color.rgb(210, 230, 255);
    private static final Color COL_PANEL   = Color.rgb(15,  30,  60,  0.88);
    private static final Color COL_ACCENT  = Color.rgb(40,  210, 135);

    // Voronoi zone fill colors — each cell gets a distinct tint
    private static final Color[] VOR_FILL = {
            Color.rgb(0,   220, 130, 0.22), Color.rgb(255, 165, 50,  0.22),
            Color.rgb(80,  180, 255, 0.22), Color.rgb(200, 60,  240, 0.22),
            Color.rgb(255, 60,  120, 0.22), Color.rgb(50,  210, 200, 0.22)
    };
    // Highlighted colors when a neighbouring user point is selected
    private static final Color[] VOR_NEIGH = {
            Color.rgb(0,   220, 130, 0.55), Color.rgb(255, 165, 50,  0.55),
            Color.rgb(80,  180, 255, 0.55), Color.rgb(200, 60,  240, 0.55),
            Color.rgb(255, 60,  120, 0.55), Color.rgb(50,  210, 200, 0.55)
    };

    // ── Model ─────────────────────────────────────────────────────────────────
    private final MapModel model;

    // ── Mission / animation ───────────────────────────────────────────────────
    private Mission        mission       = null;
    private Position       droneStart    = null;
    private double         animProgress  = 0;
    private boolean        animating     = false;
    private double         elapsedSec    = 0;
    private AnimationTimer timer         = null;
    private boolean        cancelled     = false;

    // ── Zoom / pan ────────────────────────────────────────────────────────────
    private double scale   = 1.0;
    private double offX    = 0;
    private double offY    = 0;
    private double panX, panY;
    private boolean panning = false;

    // ── Selection ─────────────────────────────────────────────────────────────
    private MedicalSite    selSite     = null;
    private MedicalSite    hovSite     = null;
    private DroneBase      selBase     = null;
    private Triangle       selTri      = null;
    private VoronoiCell    selCell     = null;
    private VoronoiCell    hovCell     = null;  // cell under mouse hover

    // ── Stats panel ───────────────────────────────────────────────────────────
    // Stats panel
    private String  statsText  = null;
    private double  spX = -1,  spY = -1;
    // Info panels (site, base, triangle) — all draggable
    private double  siteX = -1, siteY = -1;
    private double  baseX = -1, baseY = -1;
    private double  triX  = -1, triY  = -1;
    // Which panel is being dragged
    private boolean dragStats = false;
    private boolean dragSiteP = false;
    private boolean dragBaseP = false;
    private boolean dragTriP  = false;
    private double  dspX, dspY;

    // ── Drag & drop ───────────────────────────────────────────────────────────
    private MedicalSite dragSite = null;
    private DroneBase   dragBase = null;
    private double      dox, doy;
    private boolean     didDrag  = false;

    // ── Constructor ───────────────────────────────────────────────────────────
    public MapCanvas(MapModel model) {
        super(960, 670);
        this.model = model;
        widthProperty().addListener(e -> draw());
        heightProperty().addListener(e -> draw());
        setupMouse();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setCurrentMission(Mission m) {
        stopAnim();
        this.mission      = m;
        this.animProgress = 0;
        this.elapsedSec   = 0;
        this.cancelled    = false;
        if (m != null) droneStart = basePos(m.getDrone());
        draw();
    }

    public void startDroneAnimation() {
        if (mission == null || animating) return;
        animating = true; animProgress = 0; elapsedSec = 0; cancelled = false;
        timer = new AnimationTimer() {
            private long last = -1;
            public void handle(long now) {
                if (last < 0) { last = now; return; }
                double dt = (now - last) / 1e9; last = now;
                elapsedSec   += dt;
                animProgress += dt / 10.0;
                if (animProgress >= 1.0) { animProgress = 1.0; animating = false; stop(); }
                draw();
            }
        };
        timer.start();
    }

    public void cancelMission() {
        stopAnim();
        mission = null; droneStart = null;
        animProgress = 0; elapsedSec = 0; cancelled = true;
        draw();
    }

    public void showStats(String text) { statsText = text; spX = -1; spY = -1; draw(); }
    public void closeStats()           { statsText = null; draw(); }

    /** Returns current animation progress (0.0 to 1.0). */
    public double getAnimProgress() { return animProgress; }

    /** Returns true if drone animation is currently running. */
    public boolean isAnimating() { return animating; }

    /** Full redraw. */
    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();

        // Background
        gc.setFill(BG);
        gc.fillRect(0, 0, getWidth(), getHeight());

        // Grid
        gc.setStroke(GRID); gc.setLineWidth(0.5);
        double step = 50 * scale;
        double sx0  = offX % step;
        double sy0  = offY % step;
        for (double x = sx0; x < getWidth();  x += step) gc.strokeLine(x, 0, x, getHeight());
        for (double y = sy0; y < getHeight(); y += step) gc.strokeLine(0, y, getWidth(), y);

        // World-space layers
        gc.save();
        gc.translate(offX, offY);
        gc.scale(scale, scale);

        drawVoronoi(gc);
        drawDelaunay(gc);
        drawRoute(gc);
        drawBases(gc);
        drawSites(gc);
        drawDrone(gc);

        gc.restore();

        // UI overlays (screen-space)
        drawTimer(gc);
        drawSitePanel(gc);
        drawBasePanel(gc);
        drawTrianglePanel(gc);
        drawStatsPanel(gc);
        drawLegend(gc);
        drawHint(gc);
    }

    // ── World-space layers ────────────────────────────────────────────────────

    /**
     * Draws Voronoi zones with three visual states:
     *
     * 1. NORMAL     : light tint dots
     * 2. HOVERED    : when the mouse hovers a site, its Voronoi zone
     *                 lights up brightly (glowing effect)
     * 3. NEIGHBOUR  : when a user point is selected, adjacent zones
     *                 are highlighted
     *
     * Dot size adapts to zoom: bigger dots when zoomed in (approaching a site).
     */
    /**
     * Draws Voronoi zones.
     * - Normal state  : light tint dots
     * - Hovered/selected : zone lights up when mouse is over a site
     * Dot size adapts to zoom so zones become more visible when zooming in.
     */
    private void drawVoronoi(GraphicsContext gc) {
        List<VoronoiCell> cells = model.getVoronoiDiagram().getCells();
        double dotSize = Math.max(3, Math.min(10, 4 * scale));
        double half    = dotSize / 2.0;

        for (int i = 0; i < cells.size(); i++) {
            VoronoiCell cell    = cells.get(i);
            boolean lit         = cell.equals(hovCell) || cell.equals(selCell);
            Color   fill        = lit ? VOR_NEIGH[i % VOR_NEIGH.length]
                    : VOR_FILL [i % VOR_FILL.length];
            gc.setFill(fill);
            for (Position p : cell.getPoints()) {
                gc.fillRect(p.getX() - half, p.getY() - half, dotSize, dotSize);
            }
            if (lit) {
                gc.setStroke(VOR_NEIGH[i % VOR_NEIGH.length]);
                gc.setLineWidth(1.5 / scale);
                for (Position p : cell.getPoints()) {
                    gc.strokeRect(p.getX() - half, p.getY() - half, dotSize, dotSize);
                }
            }
        }
    }

    private void drawDelaunay(GraphicsContext gc) {
        for (Triangle t : model.getDelaunayTriangulation().getTriangles()) {
            Position a = t.getSiteA().getPosition();
            Position b = t.getSiteB().getPosition();
            Position c = t.getSiteC().getPosition();

            boolean sel = t.equals(selTri);
            if (sel) {
                // Fill selected triangle
                gc.setFill(Color.rgb(90, 130, 210, 0.20));
                gc.fillPolygon(
                        new double[]{a.getX(), b.getX(), c.getX()},
                        new double[]{a.getY(), b.getY(), c.getY()}, 3);
                gc.setStroke(Color.rgb(90, 130, 255, 0.9));
                gc.setLineWidth(2.0);
                // Draw circumcircle
                Position cc = t.getCircumcenter();
                if (cc != null) {
                    double r = t.getCircumradius();
                    gc.setStroke(Color.rgb(90, 130, 255, 0.5));
                    gc.setLineWidth(1.0);
                    gc.strokeOval(cc.getX() - r, cc.getY() - r, r * 2, r * 2);
                    gc.setFill(Color.rgb(90, 130, 255, 0.7));
                    gc.fillOval(cc.getX() - 3, cc.getY() - 3, 6, 6);
                }
            } else {
                gc.setStroke(COL_DEL);
                gc.setLineWidth(1.8);
            }
            gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
            gc.strokeLine(b.getX(), b.getY(), c.getX(), c.getY());
            gc.strokeLine(c.getX(), c.getY(), a.getX(), a.getY());
        }
    }


    private void drawRoute(GraphicsContext gc) {
        if (mission == null || cancelled) return;
        Route route = mission.getRoute();
        if (route == null) return;

        gc.setStroke(COL_ROUTE); gc.setLineWidth(2.0);
        gc.setLineDashes(10, 6);

        Position from  = droneStart != null ? droneStart : route.getOrigin().getPosition();
        Position ctr   = route.getOrigin().getPosition();
        Position dest  = route.getDestination().getPosition();

        gc.strokeLine(from.getX(), from.getY(), ctr.getX(), ctr.getY());
        gc.strokeLine(ctr.getX(), ctr.getY(), dest.getX(), dest.getY());
        gc.setLineDashes((double[]) null);

        // Endpoint markers
        gc.setFill(COL_ROUTE);
        gc.fillOval(ctr.getX()  - 4, ctr.getY()  - 4, 8, 8);
        gc.fillOval(dest.getX() - 4, dest.getY() - 4, 8, 8);
    }

    private void drawBases(GraphicsContext gc) {
        for (DroneBase base : model.getDroneBases()) {
            double x = base.getPosition().getX();
            double y = base.getPosition().getY();
            boolean sel = base.equals(selBase);

            if (sel) { gc.setFill(Color.rgb(180, 190, 210, 0.18)); gc.fillOval(x-20, y-20, 40, 40); }

            // Hexagon
            double[] px = {x, x-12, x-12, x, x+12, x+12};
            double[] py = {y-14, y-7, y+7, y+14, y+7, y-7};
            gc.setFill(Color.rgb(38, 48, 64));
            gc.fillPolygon(px, py, 6);
            gc.setStroke(sel ? Color.WHITE : COL_BASE);
            gc.setLineWidth(sel ? 2.0 : 1.4);
            gc.strokePolygon(px, py, 6);

            // "H" label inside
            gc.setFill(COL_BASE);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            gc.fillText("H", x - 3, y + 4);

            lbl(gc, "Base  " + base.getName(), x + 16, y + 4);

            // Drones list
            int ly = 18;
            for (Drone d : base.getDrones()) {
                boolean fly = mission != null && d.equals(mission.getDrone()) && animating;
                gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
                gc.setFill(fly ? COL_DRONE : Color.rgb(100, 200, 120));
                gc.fillText((fly ? ">" : "*") + " " + d.getId()
                        + "  " + (int) d.getBatteryLevel() + "%", x + 16, y + ly);
                ly += 13;
            }
        }
    }

    private void drawSites(GraphicsContext gc) {
        for (MedicalSite site : model.getMedicalSites()) {
            double x = site.getPosition().getX();
            double y = site.getPosition().getY();
            boolean hov = site.equals(hovSite);
            double r = hov ? 10 : 8;

            if (site instanceof Hospital) {
                if (hov) { gc.setFill(Color.rgb(30, 210, 100, 0.18)); gc.fillOval(x-18, y-18, 36, 36); }
                gc.setFill(site.equals(dragSite) ? Color.YELLOW : COL_HOSP);
                gc.fillOval(x - r, y - r, r * 2, r * 2);
                gc.setStroke(Color.WHITE); gc.setLineWidth(1.8);
                gc.strokeLine(x - 4, y, x + 4, y);
                gc.strokeLine(x, y - 4, x, y + 4);
                lbl(gc, "H  " + site.getName(), x + 12, y + 4);

            } else if (site instanceof CollectionCenter) {
                if (hov) { gc.setFill(Color.rgb(255, 160, 40, 0.18)); gc.fillOval(x-18, y-18, 36, 36); }
                gc.setFill(site.equals(dragSite) ? Color.YELLOW : COL_CENTER);
                gc.fillRect(x - r, y - r, r * 2, r * 2);
                gc.setFill(Color.WHITE); gc.fillOval(x - 3, y - 3, 6, 6);
                lbl(gc, "C  " + site.getName(), x + 12, y + 4);
            }
        }
    }

    /**
     * Draws the animated drone moving along: base -> collection center -> hospital.
     * Uses linear interpolation along the route path.
     */
    private void drawDrone(GraphicsContext gc) {
        if (mission == null || cancelled) return;
        if (!animating && animProgress <= 0) return;

        Route route = mission.getRoute();
        List<Position> path = new ArrayList<>();
        if (droneStart != null) path.add(droneStart);
        path.add(route.getOrigin().getPosition());
        path.add(route.getDestination().getPosition());

        Position pos = interpolate(path, animProgress);
        if (pos == null) return;

        mission.getDrone().updatePosition(pos);

        // Simple drone icon: body + 4 arms + 4 rotors
        double x = pos.getX(), y = pos.getY();
        gc.setFill(COL_DRONE); gc.setStroke(COL_DRONE); gc.setLineWidth(2.0);
        gc.fillOval(x - 5, y - 5, 10, 10);
        gc.strokeLine(x, y, x - 10, y - 10); gc.strokeLine(x, y, x + 10, y - 10);
        gc.strokeLine(x, y, x - 10, y + 10); gc.strokeLine(x, y, x + 10, y + 10);
        gc.setFill(Color.rgb(255, 255, 255, 0.7));
        gc.fillOval(x - 15, y - 15, 7, 7); gc.fillOval(x + 8, y - 15, 7, 7);
        gc.fillOval(x - 15, y + 8, 7, 7);  gc.fillOval(x + 8, y + 8, 7, 7);

        // Progress bar below drone
        double bw = 50;
        double bx = x - bw / 2, by = y + 22;
        gc.setFill(Color.rgb(40, 50, 68)); gc.fillRoundRect(bx, by, bw, 5, 3, 3);
        gc.setFill(COL_DRONE); gc.fillRoundRect(bx, by, bw * animProgress, 5, 3, 3);

        lbl(gc, mission.getDrone().getId(), x + 14, y + 4);
    }

    // ── Screen-space overlays ─────────────────────────────────────────────────

    private void drawTimer(GraphicsContext gc) {
        if (!animating && elapsedSec <= 0) return;
        int min = (int) elapsedSec / 60;
        int sec = (int) elapsedSec % 60;
        int ds  = (int) ((elapsedSec - (int) elapsedSec) * 10);

        double px = getWidth() - 185, py = 12;
        gc.setFill(COL_PANEL); gc.fillRoundRect(px - 10, py - 2, 178, 36, 8, 8);
        gc.setStroke(COL_DRONE); gc.setLineWidth(1); gc.strokeRoundRect(px - 10, py - 2, 178, 36, 8, 8);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        gc.setFill(COL_DRONE);
        gc.fillText(String.format("> %02d:%02d.%d", min, sec, ds), px, py + 24);
    }

    /** Info panel for the selected medical site. */
    private void drawSitePanel(GraphicsContext gc) {
        MedicalSite display = selSite != null ? selSite : hovSite;
        if (display == null || selBase != null) return;

        VoronoiCell cell  = model.getVoronoiDiagram().getCellBySite(display);
        boolean isHosp    = display instanceof Hospital;
        Color   typeColor = isHosp ? COL_HOSP : COL_CENTER;
        String  typeLabel = isHosp ? "HOSPITAL" : "COLLECTION CENTER";

        double pw = 230, ph = cell != null ? 185 : 110;
        if (siteX < 0) siteX = getWidth() - pw - 12;
        if (siteY < 0) siteY = animating ? 60 : 12;
        siteX = Math.max(0, Math.min(siteX, getWidth()  - pw));
        siteY = Math.max(0, Math.min(siteY, getHeight() - ph));
        double px = siteX, py = siteY;

        panel(gc, px, py, pw, ph, typeColor);

        // Type header
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        gc.setFill(typeColor); gc.fillText(typeLabel, px + 12, py + 19);

        // ID large
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 20));
        gc.setFill(Color.WHITE); gc.fillText(display.getId(), px + 12, py + 52);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
        gc.setFill(Color.rgb(160, 180, 210)); gc.fillText("ID", px + 12, py + 34);

        // Name + coords
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        gc.setFill(COL_TEXT);
        gc.fillText(display.getName(), px + 12, py + 70);
        gc.setFill(Color.rgb(140, 160, 190));
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
        gc.fillText("X=" + (int) display.getPosition().getX()
                + "  Y=" + (int) display.getPosition().getY(), px + 12, py + 84);

        if (cell != null) {
            divider(gc, px, py + 92, pw);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
            gc.setFill(Color.rgb(100, 140, 180)); gc.fillText("VORONOI ZONE", px + 12, py + 106);
            gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
            gc.setFill(COL_TEXT);
            gc.fillText("Surface  : " + (int) cell.getSurface(), px + 12, py + 120);
            gc.fillText("Doctors  : " + cell.getNumberOfUserPoints() + " in zone", px + 12, py + 134);
            gc.fillText("Min dist : " + String.format("%.1f", cell.getMinDistanceToUserPoints()) + " (to doctors)", px + 12, py + 148);
            gc.fillText("Avg dist : " + String.format("%.1f", cell.getAverageDistanceToUserPoints()) + " (to doctors)", px + 12, py + 162);
        }

        if (selSite == null) {
            gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
            gc.setFill(Color.rgb(100, 120, 150));
            gc.fillText("Click to pin  |  Drag to move", px + 12, py + ph - 8);
        }
    }

    /** Info panel for the selected drone base. */
    private void drawBasePanel(GraphicsContext gc) {
        if (selBase == null) return;

        int  dc  = selBase.getDrones().size();
        double pw = 235, ph = 106 + dc * 17;
        if (baseX < 0) baseX = getWidth() - pw - 12;
        if (baseY < 0) baseY = animating ? 60 : 12;
        baseX = Math.max(0, Math.min(baseX, getWidth()  - pw));
        baseY = Math.max(0, Math.min(baseY, getHeight() - ph));
        double px = baseX, py = baseY;

        panel(gc, px, py, pw, ph, COL_BASE);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        gc.setFill(COL_BASE); gc.fillText("DRONE BASE", px + 12, py + 19);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        gc.setFill(Color.WHITE); gc.fillText(selBase.getId(), px + 12, py + 52);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
        gc.setFill(Color.rgb(160, 180, 210)); gc.fillText("ID", px + 12, py + 34);

        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        gc.setFill(COL_TEXT); gc.fillText(selBase.getName(), px + 12, py + 68);
        gc.setFill(Color.rgb(140, 160, 190));
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
        gc.fillText("X=" + (int) selBase.getPosition().getX()
                + "  Y=" + (int) selBase.getPosition().getY(), px + 12, py + 82);

        divider(gc, px, py + 90, pw);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        gc.setFill(Color.rgb(100, 140, 180));
        gc.fillText("DRONES  (" + selBase.getAvailableDrones().size()
                + "/" + dc + " available)", px + 12, py + 104);

        int ly = 118;
        for (Drone d : selBase.getDrones()) {
            boolean fly = mission != null && d.equals(mission.getDrone()) && animating;
            gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
            gc.setFill(fly ? COL_DRONE : d.isAvailable()
                    ? Color.rgb(100, 200, 120) : Color.rgb(255, 80, 80));
            gc.fillText((fly ? "> " : "* ") + d.getId()
                    + "  bat=" + (int) d.getBatteryLevel() + "%"
                    + "  auto=" + (int) d.getAutonomy() + "km"
                    + (fly ? "  [FLYING]" : ""), px + 12, py + ly);
            ly += 16;
        }
    }

    /** Info panel for the selected Delaunay triangle. */
    private void drawTrianglePanel(GraphicsContext gc) {
        if (selTri == null) return;

        double pw = 265, ph = 198;
        if (triX < 0) triX = 12;
        if (triY < 0) triY = animating ? 60 : 12;
        triX = Math.max(0, Math.min(triX, getWidth()  - pw));
        triY = Math.max(0, Math.min(triY, getHeight() - ph));
        double px = triX, py = triY;

        panel(gc, px, py, pw, ph, Color.rgb(90, 130, 210, 0.9));

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        gc.setFill(Color.rgb(90, 150, 255));
        gc.fillText("DELAUNAY TRIANGLE", px + 12, py + 19);

        // Vertices
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        gc.setFill(Color.rgb(100, 140, 180)); gc.fillText("VERTICES", px + 12, py + 40);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
        gc.setFill(COL_TEXT);
        gc.fillText("A: " + selTri.getSiteA().getName(), px + 12, py + 54);
        gc.fillText("B: " + selTri.getSiteB().getName(), px + 12, py + 68);
        gc.fillText("C: " + selTri.getSiteC().getName(), px + 12, py + 82);

        divider(gc, px, py + 90, pw);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        gc.setFill(Color.rgb(100, 140, 180)); gc.fillText("DISTANCES", px + 12, py + 104);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
        gc.setFill(COL_TEXT);
        gc.fillText("AB : " + fmt(selTri.getDistanceAB()), px + 12, py + 118);
        gc.fillText("BC : " + fmt(selTri.getDistanceBC()), px + 12, py + 132);
        gc.fillText("CA : " + fmt(selTri.getDistanceCA()), px + 12, py + 146);

        divider(gc, px, py + 154, pw);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        gc.setFill(Color.rgb(100, 140, 180)); gc.fillText("STATS", px + 12, py + 168);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
        gc.setFill(COL_TEXT);
        gc.fillText("Surface  : " + fmt(selTri.computeSurface()), px + 12, py + 182);

        int ua = doctorsInCell(selTri.getSiteA());
        int ub = doctorsInCell(selTri.getSiteB());
        int uc = doctorsInCell(selTri.getSiteC());
        gc.fillText("Doctors  A=" + ua + " B=" + ub + " C=" + uc, px + 12, py + 196);
    }

    /** Draggable stats panel. */
    /**
     * Draws the draggable stats panel with a max height of 420px.
     * If content is taller, it is clipped (no overflow).
     * Drag the header to move the panel anywhere on screen.
     */
    private void drawStatsPanel(GraphicsContext gc) {
        if (statsText == null) return;
        String[] lines = statsText.split("\\n");

        double pw      = 390;
        double maxPh   = Math.min(getHeight() - 40, 420);  // max height
        double fullPh  = 32 + lines.length * 16 + 14;
        double ph      = Math.min(fullPh, maxPh);

        // Default position: top-center
        if (spX < 0) spX = (getWidth() - pw) / 2;
        if (spY < 0) spY = 20;

        // Keep panel inside window bounds
        spX = Math.max(0, Math.min(spX, getWidth()  - pw));
        spY = Math.max(0, Math.min(spY, getHeight() - ph));

        // Shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.35));
        gc.fillRoundRect(spX + 4, spY + 4, pw, ph, 12, 12);

        panel(gc, spX, spY, pw, ph, COL_ACCENT);

        // Header
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        gc.setFill(COL_ACCENT);
        gc.fillText("STATISTICS", spX + 12, spY + 18);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        gc.setFill(Color.rgb(255, 80, 80));
        gc.fillText("X", spX + pw - 22, spY + 18);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
        gc.setFill(Color.rgb(100, 160, 120));
        gc.fillText("drag to move", spX + 105, spY + 18);

        // Clip content to panel bounds (avoid overflow)
        gc.save();
        gc.beginPath();
        gc.rect(spX, spY + 26, pw, ph - 28);
        gc.clip();

        for (int i = 0; i < lines.length; i++) {
            double ly = spY + 34 + i * 16;
            if (ly > spY + ph) break;
            boolean isHeader = lines[i].startsWith("--") || lines[i].startsWith("==");
            gc.setFont(Font.font("Monospace",
                    isHeader ? FontWeight.BOLD : FontWeight.NORMAL,
                    isHeader ? 10 : 11));
            gc.setFill(isHeader ? Color.rgb(100, 140, 180) : COL_TEXT);
            gc.fillText(lines[i], spX + 12, ly);
        }
        gc.restore();
    }

    private void drawLegend(GraphicsContext gc) {
        double x = 12, y = getHeight() - 144;
        gc.setFill(COL_PANEL); gc.fillRoundRect(x - 8, y - 18, 215, 134, 10, 10);
        gc.setStroke(Color.rgb(80, 120, 180, 0.3)); gc.setLineWidth(1);
        gc.strokeRoundRect(x - 8, y - 18, 215, 134, 10, 10);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        gc.setFill(COL_ACCENT); gc.fillText("LEGEND", x, y);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));

        dot(gc, COL_HOSP, x, y+14);  tf(gc, "Hospital",          x+18, y+23);
        sqr(gc, COL_CENTER, x, y+32); tf(gc, "Collection Center", x+18, y+41);
        hex(gc, COL_BASE, x, y+50);   tf(gc, "Drone Base",         x+18, y+59);
        dot(gc, COL_DRONE, x, y+68);  tf(gc, "Drone (in flight)",  x+18, y+77);

        gc.setStroke(COL_ROUTE); gc.setLineWidth(2); gc.setLineDashes(6, 4);
        gc.strokeLine(x, y+108, x+10, y+108); gc.setLineDashes((double[]) null);
        tf(gc, "Mission route", x+18, y+112);

        gc.setStroke(COL_DEL); gc.setLineWidth(1.5);
        gc.strokeLine(x, y+126, x+10, y+126);
        tf(gc, "Delaunay edge", x+18, y+130);

        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
        gc.setFill(Color.rgb(80, 100, 130));
        gc.fillText("Wheel=zoom  Mid-drag=pan", x, y + 148);
    }

    private void drawHint(GraphicsContext gc) {
        if (dragSite != null || dragBase != null) return;
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
        gc.setFill(Color.rgb(80, 100, 130));
        gc.fillText("Click=info  Drag=move  Wheel=zoom  Mid-drag=pan  Click triangle centroid=inspect",
                12, getHeight() - 8);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    private void setupMouse() {
        // Zoom
        setOnScroll(e -> {
            double f  = e.getDeltaY() > 0 ? 1.1 : 0.9;
            double ns = Math.max(0.2, Math.min(5.0, scale * f));
            offX = e.getX() - (e.getX() - offX) * (ns / scale);
            offY = e.getY() - (e.getY() - offY) * (ns / scale);
            scale = ns; draw();
        });

        // Hover
        setOnMouseMoved(e -> {
            MedicalSite f    = siteAt(toMX(e.getX()), toMY(e.getY()));
            VoronoiCell cell = f != null
                    ? model.getVoronoiDiagram().getCellBySite(f) : null;
            if (f != hovSite || cell != hovCell) {
                hovSite = f;
                hovCell = cell;
                draw();
            }
        });

        // Press
        setOnMousePressed(e -> {
            didDrag = false;
            double sx = e.getX(), sy = e.getY();
            double mx = toMX(sx), my = toMY(sy);

            if (e.isMiddleButtonDown() || e.isSecondaryButtonDown()) {
                panning = true; panX = sx; panY = sy; return;
            }
            if (statsText != null && closeX(sx, sy)) {
                statsText = null; draw(); return;
            }
            if (statsText != null && statsHeader(sx, sy)) {
                dragStats = true; dspX = sx - spX; dspY = sy - spY; return;
            }
            // Drag site panel
            if ((selSite != null || hovSite != null) && isPanelHeader(sx, sy, siteX, siteY, 230)) {
                dragSiteP = true; dspX = sx - siteX; dspY = sy - siteY; return;
            }
            // Drag base panel
            if (selBase != null && isPanelHeader(sx, sy, baseX, baseY, 235)) {
                dragBaseP = true; dspX = sx - baseX; dspY = sy - baseY; return;
            }
            // Drag triangle panel
            if (selTri != null && isPanelHeader(sx, sy, triX, triY, 265)) {
                dragTriP = true; dspX = sx - triX; dspY = sy - triY; return;
            }

            MedicalSite site = siteAt(mx, my);
            if (site != null) { dragSite = site; dox = mx - site.getPosition().getX(); doy = my - site.getPosition().getY(); return; }

            DroneBase base = baseAt(mx, my);
            if (base != null) { dragBase = base; dox = mx - base.getPosition().getX(); doy = my - base.getPosition().getY(); return; }

        });

        // Drag
        setOnMouseDragged(e -> {
            double sx = e.getX(), sy = e.getY();
            if (panning) { offX += sx - panX; offY += sy - panY; panX = sx; panY = sy; draw(); return; }
            if (dragStats)  { spX   = sx - dspX; spY   = sy - dspY; draw(); return; }
            if (dragSiteP)  { siteX = sx - dspX; siteY = sy - dspY; draw(); return; }
            if (dragBaseP)  { baseX = sx - dspX; baseY = sy - dspY; draw(); return; }
            if (dragTriP)   { triX  = sx - dspX; triY  = sy - dspY; draw(); return; }
            didDrag = true;
            double mx = toMX(sx), my = toMY(sy);
            if (dragSite != null) { model.moveMedicalSite(dragSite, new Position(mx - dox, my - doy)); draw(); }
            else if (dragBase != null) { dragBase.getPosition().setX(mx - dox); dragBase.getPosition().setY(my - doy); draw(); }
        });

        // Release
        setOnMouseReleased(e -> {
            if (panning)   { panning = false; return; }
            if (dragStats  || dragSiteP || dragBaseP || dragTriP) {
                dragStats = false; dragSiteP = false;
                dragBaseP = false; dragTriP  = false;
                return;
            }
            boolean wasDrag = dragSite != null || dragBase != null;
            dragSite = null; dragBase = null;
            if (!didDrag) click(e.getX(), e.getY());
            didDrag = false; draw();
        });
    }

    private void click(double sx, double sy) {
        double mx = toMX(sx), my = toMY(sy);

        if (statsText != null && closeX(sx, sy)) { statsText = null; draw(); return; }
        if (statsText != null && !insideStats(sx, sy)) { statsText = null; }

        MedicalSite site = siteAt(mx, my);
        if (site != null) {
            selBase = null; selTri = null;
            selSite = site.equals(selSite) ? null : site;
            selCell = selSite != null ? model.getVoronoiDiagram().getCellBySite(selSite) : null;
            if (selSite != null) { siteX = -1; siteY = -1; } // reset to default pos
            hovSite = site; draw(); return;
        }

        DroneBase base = baseAt(mx, my);
        if (base != null) {
            selSite = null; selCell = null; selTri = null;
            selBase = base.equals(selBase) ? null : base;
            if (selBase != null) { baseX = -1; baseY = -1; }
            draw(); return;
        }


        Triangle tri = triAt(mx, my);
        if (tri != null) {
            selSite = null; selCell = null; selBase = null;
            selTri = tri.equals(selTri) ? null : tri;
            if (selTri != null) { triX = -1; triY = -1; }
            draw(); return;
        }

        selSite = null; selCell = null; selBase = null;
        selTri = null;
        draw();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double toMX(double sx) { return (sx - offX) / scale; }
    private double toMY(double sy) { return (sy - offY) / scale; }

    private MedicalSite siteAt(double mx, double my) {
        for (MedicalSite s : model.getMedicalSites()) {
            double dx = s.getPosition().getX() - mx, dy = s.getPosition().getY() - my;
            if (Math.sqrt(dx*dx + dy*dy) < 14) return s;
        }
        return null;
    }

    private DroneBase baseAt(double mx, double my) {
        for (DroneBase b : model.getDroneBases()) {
            double dx = b.getPosition().getX() - mx, dy = b.getPosition().getY() - my;
            if (Math.sqrt(dx*dx + dy*dy) < 16) return b;
        }
        return null;
    }


    private Triangle triAt(double mx, double my) {
        Triangle best = null; double bd = 28;
        for (Triangle t : model.getDelaunayTriangulation().getTriangles()) {
            double cx = (t.getSiteA().getPosition().getX() + t.getSiteB().getPosition().getX() + t.getSiteC().getPosition().getX()) / 3;
            double cy = (t.getSiteA().getPosition().getY() + t.getSiteB().getPosition().getY() + t.getSiteC().getPosition().getY()) / 3;
            double d  = Math.sqrt((cx-mx)*(cx-mx) + (cy-my)*(cy-my));
            if (d < bd) { bd = d; best = t; }
        }
        return best;
    }


    private int doctorsInCell(MedicalSite site) {
        VoronoiCell c = model.getVoronoiDiagram().getCellBySite(site);
        return c != null ? c.getNumberOfUserPoints() : 0;
    }

    private Position basePos(Drone drone) {
        for (DroneBase b : model.getDroneBases())
            for (Drone d : b.getDrones())
                if (d.equals(drone)) return new Position(b.getPosition().getX(), b.getPosition().getY());
        return null;
    }

    private Position interpolate(List<Position> path, double t) {
        if (path == null || path.size() < 2) return null;
        double total = 0;
        for (int i = 0; i < path.size()-1; i++) total += path.get(i).distanceTo(path.get(i+1));
        if (total == 0) return null;
        double target = t * total, walked = 0;
        for (int i = 0; i < path.size()-1; i++) {
            Position f = path.get(i), to = path.get(i+1);
            double seg = f.distanceTo(to);
            if (walked + seg >= target) {
                double u = (target - walked) / seg;
                return new Position(f.getX() + u*(to.getX()-f.getX()), f.getY() + u*(to.getY()-f.getY()));
            }
            walked += seg;
        }
        return path.get(path.size()-1);
    }

    private void stopAnim() {
        if (timer != null) { timer.stop(); timer = null; }
        animating = false;
    }

    // Stats panel helpers
    private boolean statsHeader(double sx, double sy) {
        if (statsText == null || spX < 0) return false;
        return sx >= spX && sx <= spX + 340 && sy >= spY && sy <= spY + 26;
    }
    /**
     * Returns true if (sx, sy) is inside the draggable header area
     * of a panel positioned at (px, py) with the given width.
     */
    private boolean isPanelHeader(double sx, double sy,
                                  double px, double py, double pw) {
        if (px < 0) return false;
        return sx >= px && sx <= px + pw && sy >= py && sy <= py + 28;
    }

    private boolean closeX(double sx, double sy) {
        if (statsText == null || spX < 0) return false;
        String[] l = statsText.split("\\n");
        double pw = 370;
        return sx >= spX + pw - 28 && sx <= spX + pw - 6 && sy >= spY + 4 && sy <= spY + 26;
    }
    private boolean insideStats(double sx, double sy) {
        if (statsText == null || spX < 0) return false;
        String[] l = statsText.split("\\n");
        double pw = 370, ph = 32 + l.length * 16 + 14;
        return sx >= spX && sx <= spX + pw && sy >= spY && sy <= spY + ph;
    }

    // Draw helpers
    private void panel(GraphicsContext gc, double px, double py, double pw, double ph, Color border) {
        gc.setFill(COL_PANEL); gc.fillRoundRect(px, py, pw, ph, 12, 12);
        gc.setStroke(border); gc.setLineWidth(1.5); gc.strokeRoundRect(px, py, pw, ph, 12, 12);
        // Header band
        gc.setFill(Color.rgb((int)(border.getRed()*255),(int)(border.getGreen()*255),(int)(border.getBlue()*255),0.16));
        gc.fillRoundRect(px, py, pw, 28, 12, 12); gc.fillRect(px, py+16, pw, 12);
    }
    private void divider(GraphicsContext gc, double px, double y, double pw) {
        gc.setStroke(Color.rgb(50, 70, 100)); gc.setLineWidth(1);
        gc.strokeLine(px + 8, y, px + pw - 8, y);
    }
    private void lbl(GraphicsContext gc, String t, double x, double y) {
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        gc.setFill(Color.rgb(0,0,0,0.5)); gc.fillText(t, x+1, y+1);
        gc.setFill(COL_TEXT); gc.fillText(t, x, y);
    }
    private void tf(GraphicsContext gc, String t, double x, double y)
    { gc.setFill(COL_TEXT); gc.fillText(t, x, y); }
    private String fmt(double v) { return String.format("%.1f", v); }
    private void dot(GraphicsContext gc, Color c, double x, double y)
    { gc.setFill(c); gc.fillOval(x, y, 10, 10); }
    private void sqr(GraphicsContext gc, Color c, double x, double y)
    { gc.setFill(c); gc.fillRect(x, y, 10, 10); }
    private void hex(GraphicsContext gc, Color c, double x, double y) {
        gc.setFill(c);
        double cx = x+5, cy = y+5;
        gc.fillPolygon(new double[]{cx,cx-5,cx-5,cx,cx+5,cx+5},
                new double[]{cy-6,cy-3,cy+3,cy+6,cy+3,cy-3}, 6);
    }
}