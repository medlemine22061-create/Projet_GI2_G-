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
import model.UserPoint;
import model.VoronoiCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Map canvas — MEDADRONE.
 *
 * New features vs previous version:
 *  1. Zoom (mouse wheel) + Pan (middle-click drag or right-click drag)
 *  2. User points displayed with link to nearest site
 *  3. Click on user point → highlights neighbouring zones
 *  4. Click on a Delaunay triangle → shows triangle info panel
 *  5. Drag & drop for sites AND drone bases
 *  6. Stats panel on canvas (no log area)
 */
public class MapCanvas extends Canvas {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color BG           = Color.rgb(44,  44,  42);
    private static final Color GRID_CLR     = Color.rgb(38,  38,  36);
    private static final Color HOSPITAL_CLR = Color.rgb(30,  210, 100);
    private static final Color CENTER_CLR   = Color.rgb(255, 160, 40);
    private static final Color BASE_CLR     = Color.rgb(180, 190, 210);
    private static final Color DRONE_FLY    = Color.rgb(255, 220, 50);
    private static final Color ROUTE_CLR    = Color.rgb(255, 65,  65);
    private static final Color DELAUNAY_CLR = Color.rgb(90,  130, 210, 0.38);
    private static final Color DELAUNAY_SEL = Color.rgb(90,  130, 210, 0.90);
    private static final Color TEXT_CLR     = Color.rgb(215, 228, 248);
    private static final Color PANEL_BG     = Color.rgb(18,  18,  16,  0.94);
    private static final Color ACCENT       = Color.rgb(40,  210, 135);
    private static final Color DRAG_CLR     = Color.rgb(255, 255, 100, 0.6);
    private static final Color USERPT_CLR   = Color.rgb(200, 100, 255);
    private static final Color LINK_CLR     = Color.rgb(200, 100, 255, 0.45);

    private static final Color[] VORONOI_FILL = {
            Color.rgb(30,  210, 100, 0.11), Color.rgb(255, 160, 40,  0.11),
            Color.rgb(100, 150, 255, 0.11), Color.rgb(200, 60,  240, 0.11),
            Color.rgb(255, 60,  120, 0.11), Color.rgb(50,  210, 200, 0.11),
    };
    private static final Color[] VORONOI_DOT = {
            Color.rgb(30,  210, 100, 0.55), Color.rgb(255, 160, 40,  0.55),
            Color.rgb(100, 150, 255, 0.55), Color.rgb(200, 60,  240, 0.55),
            Color.rgb(255, 60,  120, 0.55), Color.rgb(50,  210, 200, 0.55),
    };
    private static final Color[] VORONOI_HIGH = {
            Color.rgb(30,  210, 100, 0.40), Color.rgb(255, 160, 40,  0.40),
            Color.rgb(100, 150, 255, 0.40), Color.rgb(200, 60,  240, 0.40),
            Color.rgb(255, 60,  120, 0.40), Color.rgb(50,  210, 200, 0.40),
    };

    // ── Zoom / Pan ────────────────────────────────────────────────────────────
    private double scale     = 1.0;
    private double offsetX   = 0.0;
    private double offsetY   = 0.0;
    private double panStartX, panStartY;
    private boolean panning  = false;

    // ── Model ─────────────────────────────────────────────────────────────────
    private final MapModel mapModel;
    private Mission currentMission;
    private Position droneBasePos = null;

    // ── Animation ─────────────────────────────────────────────────────────────
    private double        droneAnimProgress = 0.0;
    private boolean       animating         = false;
    private AnimationTimer animationTimer;
    private double        elapsedSec        = 0.0;
    private final List<Position> trail      = new ArrayList<>();
    private boolean missionCompleted        = false;
    private boolean missionCancelled        = false;

    // ── Interaction ───────────────────────────────────────────────────────────
    private MedicalSite hoveredSite   = null;
    private MedicalSite clickedSite   = null;
    private DroneBase   clickedBase   = null;
    private UserPoint   clickedUser   = null;  // highlighted user point
    private Triangle    clickedTriangle = null; // selected triangle
    private VoronoiCell selectedCell  = null;
    private List<VoronoiCell> neighbourCells = new ArrayList<>(); // for user point highlight

    // ── Stats panel ───────────────────────────────────────────────────────────
    // Stats panel (draggable)
    private String  statsText     = null;
    private double  statsPanelX   = -1;
    private double  statsPanelY   = -1;
    private boolean draggingStats = false;
    private double  statsDragOffX = 0;
    private double  statsDragOffY = 0;

    // ── Drag & drop ───────────────────────────────────────────────────────────
    private MedicalSite draggingSite = null;
    private DroneBase   draggingBase = null;
    private UserPoint   draggingUser = null;
    private double      dragOffsetX  = 0;
    private double      dragOffsetY  = 0;
    private boolean     didDrag      = false;

    // ── Constructor ───────────────────────────────────────────────────────────
    public MapCanvas(MapModel mapModel) {
        super(960, 670);
        this.mapModel = mapModel;
        widthProperty().addListener(e  -> draw());
        heightProperty().addListener(e -> draw());
        setupMouse();
    }

    // ── Coordinate transform ──────────────────────────────────────────────────

    /** Convert model coordinates to screen coordinates. */
    private double toScreenX(double mx) { return mx * scale + offsetX; }
    private double toScreenY(double my) { return my * scale + offsetY; }

    /** Convert screen coordinates to model coordinates. */
    private double toModelX(double sx) { return (sx - offsetX) / scale; }
    private double toModelY(double sy) { return (sy - offsetY) / scale; }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setCurrentMission(Mission mission) {
        stopAnimation();
        this.currentMission    = mission;
        this.droneAnimProgress = 0.0;
        this.elapsedSec        = 0.0;
        this.missionCompleted  = false;
        this.missionCancelled  = false;
        this.trail.clear();
        if (mission != null) {
            droneBasePos = findBasePosition(mission.getDrone());
            if (droneBasePos == null)
                droneBasePos = new Position(
                        mission.getDrone().getPosition().getX(),
                        mission.getDrone().getPosition().getY());
        }
        draw();
    }

    public void startDroneAnimation() {
        if (currentMission == null || animating) return;
        animating = true; droneAnimProgress = 0.0;
        elapsedSec = 0.0; missionCompleted = false; missionCancelled = false;
        trail.clear();
        animationTimer = new AnimationTimer() {
            private long last = -1;
            @Override public void handle(long now) {
                if (last < 0) { last = now; return; }
                double dt = (now - last) / 1_000_000_000.0; last = now;
                elapsedSec += dt; droneAnimProgress += dt / 10.0;
                if (droneAnimProgress >= 1.0) { droneAnimProgress = 1.0; animating = false; stop(); }
                draw();
            }
        };
        animationTimer.start();
    }

    public void cancelMission() {
        stopAnimation();
        currentMission = null; droneBasePos = null;
        droneAnimProgress = 0.0; elapsedSec = 0.0;
        missionCompleted = false; missionCancelled = true;
        trail.clear(); draw();
    }

    public void showStats(String text) { this.statsText = text; draw(); }
    public void closeStats()           { this.statsText = null; statsPanelX = -1; statsPanelY = -1; draw(); }

    /** Full redraw. */
    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();

        drawBg(gc);

        // Apply zoom/pan transform
        gc.translate(offsetX, offsetY);
        gc.scale(scale, scale);

        drawVoronoi(gc);
        drawDelaunay(gc);
        drawUserPoints(gc);
        drawRoute(gc);
        drawBases(gc);
        drawSites(gc);
        drawTrail(gc);
        drawAnimDrone(gc);

        gc.restore();

        // UI overlays — NOT affected by zoom/pan
        drawTimer(gc);
        drawInfoPanel(gc);
        drawTrianglePanel(gc);
        drawStatsPanel(gc);
        drawLegend(gc);
        drawZoomIndicator(gc);
        drawDragHint(gc);
    }

    // ── Drawing layers ────────────────────────────────────────────────────────

    private void drawBg(GraphicsContext gc) {
        gc.setFill(BG); gc.fillRect(0, 0, getWidth(), getHeight());
        // Grid adapts to zoom/pan
        gc.setStroke(GRID_CLR); gc.setLineWidth(0.5 / scale);
        double step = 50;
        double startX = -offsetX / scale;
        double startY = -offsetY / scale;
        double endX = startX + getWidth() / scale;
        double endY = startY + getHeight() / scale;
        for (double x = Math.floor(startX/step)*step; x <= endX; x += step)
            gc.strokeLine(x, startY, x, endY);
        for (double y = Math.floor(startY/step)*step; y <= endY; y += step)
            gc.strokeLine(startX, y, endX, y);
    }

    private void drawVoronoi(GraphicsContext gc) {
        List<VoronoiCell> cells = mapModel.getVoronoiDiagram().getCells();
        for (int i = 0; i < cells.size(); i++) {
            VoronoiCell cell = cells.get(i);
            boolean sel  = cell.equals(selectedCell);
            boolean high = neighbourCells.contains(cell);
            Color fill = high    ? VORONOI_HIGH[i % VORONOI_HIGH.length]
                    : sel     ? VORONOI_DOT [i % VORONOI_DOT.length]
                    : VORONOI_FILL[i % VORONOI_FILL.length];
            gc.setFill(fill);
            int k = 0;
            for (Position p : cell.getPoints()) {
                if (k % 2 == 0) gc.fillRect(p.getX()-1, p.getY()-1, 4, 4);
                k++;
            }
        }
    }

    /**
     * Draws Delaunay triangles.
     * Selected triangle is drawn filled + highlighted.
     */
    private void drawDelaunay(GraphicsContext gc) {
        for (Triangle t : mapModel.getDelaunayTriangulation().getTriangles()) {
            Position a = t.getSiteA().getPosition();
            Position b = t.getSiteB().getPosition();
            Position c = t.getSiteC().getPosition();

            if (t.equals(clickedTriangle)) {
                // Fill selected triangle
                gc.setFill(Color.rgb(90, 130, 210, 0.22));
                gc.fillPolygon(
                        new double[]{a.getX(), b.getX(), c.getX()},
                        new double[]{a.getY(), b.getY(), c.getY()}, 3);
                gc.setStroke(DELAUNAY_SEL); gc.setLineWidth(2.0 / scale);
            } else {
                gc.setStroke(DELAUNAY_CLR); gc.setLineWidth(1.2 / scale);
            }
            gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
            gc.strokeLine(b.getX(), b.getY(), c.getX(), c.getY());
            gc.strokeLine(c.getX(), c.getY(), a.getX(), a.getY());

            // Draw circumcenter dot for selected triangle
            if (t.equals(clickedTriangle)) {
                Position cc = t.getCircumcenter();
                if (cc != null) {
                    gc.setFill(Color.rgb(90, 130, 255, 0.7));
                    gc.fillOval(cc.getX()-4, cc.getY()-4, 8, 8);
                    gc.setStroke(Color.rgb(90,130,255,0.9));
                    gc.setLineWidth(1.0 / scale);
                    gc.strokeOval(cc.getX() - t.getCircumradius(),
                            cc.getY() - t.getCircumradius(),
                            t.getCircumradius()*2, t.getCircumradius()*2);
                }
            }
        }
    }

    /**
     * Draws user points as small purple diamonds.
     * For each user point, draws a dashed line to its nearest site.
     * Clicked user point is highlighted larger.
     */
    private void drawUserPoints(GraphicsContext gc) {
        for (UserPoint up : mapModel.getUserPoints()) {
            double x = up.getPosition().getX();
            double y = up.getPosition().getY();
            boolean sel = up.equals(clickedUser);
            double r = sel ? 6 : 4;

            // Link to nearest site
            if (up.getNearestSite() != null) {
                Position ns = up.getNearestSite().getPosition();
                gc.setStroke(sel ? Color.rgb(200,100,255,0.75) : LINK_CLR);
                gc.setLineWidth((sel ? 1.2 : 0.8) / scale);
                gc.setLineDashes(4, 4);
                gc.strokeLine(x, y, ns.getX(), ns.getY());
                gc.setLineDashes((double[]) null);
            }

            // Diamond shape
            gc.setFill(sel ? Color.rgb(220, 130, 255) : USERPT_CLR);
            double[] px = {x, x+r, x, x-r};
            double[] py = {y-r, y, y+r, y};
            gc.fillPolygon(px, py, 4);

            if (sel) {
                gc.setStroke(Color.WHITE); gc.setLineWidth(1.2 / scale);
                gc.strokePolygon(px, py, 4);
            }
        }
    }

    private void drawRoute(GraphicsContext gc) {
        if (currentMission == null || missionCancelled) return;
        Route route = currentMission.getRoute();
        if (route == null) return;
        gc.setStroke(ROUTE_CLR); gc.setLineWidth(2.2 / scale);
        gc.setLineDashes(10, 6);
        Position start  = (droneBasePos != null) ? droneBasePos : route.getOrigin().getPosition();
        Position center = route.getOrigin().getPosition();
        gc.strokeLine(start.getX(), start.getY(), center.getX(), center.getY());
        Position cur = center;
        for (Position wp : route.getWaypoints()) {
            gc.strokeLine(cur.getX(), cur.getY(), wp.getX(), wp.getY()); cur = wp;
        }
        Position dest = route.getDestination().getPosition();
        gc.strokeLine(cur.getX(), cur.getY(), dest.getX(), dest.getY());
        gc.setLineDashes((double[]) null);
        dot(gc, ROUTE_CLR, center.getX(), center.getY(), 5 / scale);
        dot(gc, ROUTE_CLR, dest.getX(),   dest.getY(),   5 / scale);
    }

    private void drawBases(GraphicsContext gc) {
        for (DroneBase base : mapModel.getDroneBases()) {
            double x = base.getPosition().getX();
            double y = base.getPosition().getY();
            boolean clicked  = base.equals(clickedBase);
            boolean dragging = base.equals(draggingBase);
            if (clicked || dragging) {
                gc.setFill(Color.rgb(180,190,210,0.18));
                gc.fillOval(x-22, y-22, 44, 44);
            }
            gc.setFill(Color.rgb(38, 48, 64));
            double[] px = {x, x-13, x-13, x, x+13, x+13};
            double[] py = {y-15, y-7, y+7, y+15, y+7, y-7};
            gc.fillPolygon(px, py, 6);
            gc.setStroke(clicked ? Color.WHITE : BASE_CLR);
            gc.setLineWidth((clicked ? 2.2 : 1.5) / scale);
            gc.strokePolygon(px, py, 6);
            gc.setFill(BASE_CLR);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 10 / scale));
            gc.fillText("H", x-3, y+4);
            label(gc, "Base  " + base.getName(), x+17, y+4);
            int ly = 18;
            for (Drone d : base.getDrones()) {
                boolean inFlight = currentMission != null
                        && d.equals(currentMission.getDrone())
                        && (animating || missionCompleted);
                String icon = inFlight ? "✈" : "●";
                gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9 / scale));
                gc.setFill(inFlight ? DRONE_FLY : Color.rgb(100, 200, 120));
                gc.fillText(icon + " " + d.getId() + "  " + (int)d.getBatteryLevel() + "%", x+17, y+ly);
                ly += 13;
            }
        }
    }

    private void drawSites(GraphicsContext gc) {
        for (MedicalSite site : mapModel.getMedicalSites()) {
            double x = site.getPosition().getX();
            double y = site.getPosition().getY();
            boolean hov  = site.equals(hoveredSite);
            boolean drag = site.equals(draggingSite);
            double r = (hov || drag) ? 11 : 8;
            if (site instanceof Hospital) {
                if (hov || drag) { gc.setFill(Color.rgb(30,210,100,0.20)); gc.fillOval(x-20,y-20,40,40); }
                gc.setFill(drag ? DRAG_CLR : HOSPITAL_CLR);
                gc.fillOval(x-r, y-r, r*2, r*2);
                gc.setStroke(Color.WHITE); gc.setLineWidth(2 / scale);
                gc.strokeLine(x-4, y, x+4, y); gc.strokeLine(x, y-4, x, y+4);
                label(gc, "H  " + site.getName(), x+13, y+4);
            } else if (site instanceof CollectionCenter) {
                if (hov || drag) { gc.setFill(Color.rgb(255,160,40,0.20)); gc.fillOval(x-20,y-20,40,40); }
                gc.setFill(drag ? DRAG_CLR : CENTER_CLR);
                gc.fillRect(x-r, y-r, r*2, r*2);
                gc.setFill(Color.WHITE); gc.fillOval(x-3, y-3, 6, 6);
                label(gc, "C  " + site.getName(), x+13, y+4);
            }
        }
    }

    private void drawTrail(GraphicsContext gc) {
        for (int i = 0; i < trail.size(); i++) {
            double alpha = (double) i / trail.size() * 0.38;
            double size  = 4 + (double) i / trail.size() * 7;
            gc.setFill(Color.rgb(255, 210, 50, alpha));
            Position p = trail.get(i);
            gc.fillOval(p.getX()-size/2, p.getY()-size/2, size, size);
        }
    }

    private void drawAnimDrone(GraphicsContext gc) {
        if (currentMission == null || missionCancelled) return;
        Route route = currentMission.getRoute();
        Drone drone = currentMission.getDrone();
        List<Position> path = new ArrayList<>();
        if (droneBasePos != null) path.add(droneBasePos);
        path.add(route.getOrigin().getPosition());
        path.addAll(route.getWaypoints());
        path.add(route.getDestination().getPosition());
        if (missionCompleted) {
            Position dest = route.getDestination().getPosition();
            gc.setFill(Color.rgb(30,210,100,0.20));
            gc.fillOval(dest.getX()-26, dest.getY()-26, 52, 52);
            droneIcon(gc, dest.getX(), dest.getY(), DRONE_FLY, drone.getId() + " ✔", true);
            return;
        }
        if (!animating && droneAnimProgress <= 0) return;
        Position pos = interpolate(path, droneAnimProgress);
        if (pos == null) return;
        trail.add(new Position(pos.getX(), pos.getY()));
        if (trail.size() > 20) trail.remove(0);
        drone.updatePosition(pos);
        gc.setFill(Color.rgb(255,220,60,0.15));
        gc.fillOval(pos.getX()-24, pos.getY()-24, 48, 48);
        droneIcon(gc, pos.getX(), pos.getY(), DRONE_FLY, drone.getId(), true);
        double bw = 54, bx = pos.getX()-bw/2, by = pos.getY()+26;
        gc.setFill(Color.rgb(40,50,68)); gc.fillRoundRect(bx, by, bw, 5, 3, 3);
        gc.setFill(DRONE_FLY); gc.fillRoundRect(bx, by, bw*droneAnimProgress, 5, 3, 3);
    }

    private void drawTimer(GraphicsContext gc) {
        if (!animating && elapsedSec <= 0) return;
        double px = getWidth()-188, py = 12;
        gc.setFill(PANEL_BG); gc.fillRoundRect(px-10, py-2, 182, 38, 8, 8);
        gc.setStroke(DRONE_FLY); gc.setLineWidth(1);
        gc.strokeRoundRect(px-10, py-2, 182, 38, 8, 8);
        int min = (int)elapsedSec/60, sec = (int)elapsedSec%60;
        int ds  = (int)((elapsedSec-(int)elapsedSec)*10);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 19));
        gc.setFill(DRONE_FLY);
        gc.fillText(String.format("✈  %02d:%02d.%d", min, sec, ds), px, py+26);
    }

    /**
     * Info panel for clicked site or base.
     * If a user point is clicked, shows its nearest site info.
     */
    private void drawInfoPanel(GraphicsContext gc) {
        // ── Base panel ────────────────────────────────────────────────────────
        if (clickedBase != null) {
            double pw = 235, py = animating ? 62 : 12;
            double px = getWidth() - pw - 12;
            int dc = clickedBase.getDrones().size();
            double ph = 110 + dc * 18;
            gc.setFill(PANEL_BG); gc.fillRoundRect(px, py, pw, ph, 12, 12);
            gc.setStroke(BASE_CLR); gc.setLineWidth(1.8);
            gc.strokeRoundRect(px, py, pw, ph, 12, 12);
            gc.setFill(Color.rgb(180,190,210,0.18));
            gc.fillRoundRect(px, py, pw, 28, 12, 12); gc.fillRect(px, py+16, pw, 12);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            gc.setFill(BASE_CLR); gc.fillText("DRONE BASE", px+12, py+19);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
            gc.setFill(Color.WHITE); gc.fillText(clickedBase.getId(), px+12, py+52);
            gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
            gc.setFill(Color.rgb(160,180,210)); gc.fillText("ID", px+12, py+34);
            gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            gc.setFill(TEXT_CLR); gc.fillText(clickedBase.getName(), px+12, py+68);
            gc.setFill(Color.rgb(140,160,190));
            gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
            gc.fillText("X=" + (int)clickedBase.getPosition().getX()
                    + "  Y=" + (int)clickedBase.getPosition().getY(), px+12, py+82);
            gc.setStroke(Color.rgb(50,70,100)); gc.setLineWidth(1);
            gc.strokeLine(px+8, py+90, px+pw-8, py+90);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
            gc.setFill(Color.rgb(100,140,180));
            gc.fillText("DRONES (" + clickedBase.getAvailableDrones().size()
                    + "/" + dc + " avail)", px+12, py+104);
            int ly = 118;
            for (Drone d : clickedBase.getDrones()) {
                boolean inFlight = currentMission != null
                        && d.equals(currentMission.getDrone())
                        && (animating || missionCompleted);
                String icon = inFlight ? "✈ " : (d.isAvailable() ? "● " : "✕ ");
                Color dc2 = inFlight ? DRONE_FLY
                        : d.isAvailable() ? Color.rgb(100,200,120) : Color.rgb(255,80,80);
                gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
                gc.setFill(dc2);
                gc.fillText(icon + d.getId() + "  bat=" + (int)d.getBatteryLevel()
                        + "%  auto=" + (int)d.getAutonomy() + "km"
                        + (inFlight ? "  [IN FLIGHT]" : ""), px+12, py+ly);
                ly += 16;
            }
            return;
        }

        // ── User point panel ──────────────────────────────────────────────────
        if (clickedUser != null) {
            double pw = 230, ph = 120;
            double px = getWidth() - pw - 12;
            double py = animating ? 62 : 12;
            gc.setFill(PANEL_BG); gc.fillRoundRect(px, py, pw, ph, 12, 12);
            gc.setStroke(USERPT_CLR); gc.setLineWidth(1.8);
            gc.strokeRoundRect(px, py, pw, ph, 12, 12);
            gc.setFill(Color.rgb(200,100,255,0.18));
            gc.fillRoundRect(px, py, pw, 28, 12, 12); gc.fillRect(px, py+16, pw, 12);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            gc.setFill(USERPT_CLR); gc.fillText("USER POINT", px+12, py+19);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
            gc.setFill(Color.WHITE); gc.fillText(clickedUser.getId(), px+12, py+52);
            gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
            gc.setFill(Color.rgb(160,180,210)); gc.fillText("ID", px+12, py+34);
            gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
            gc.setFill(Color.rgb(140,160,190));
            gc.fillText("X=" + (int)clickedUser.getPosition().getX()
                    + "  Y=" + (int)clickedUser.getPosition().getY(), px+12, py+68);
            gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            gc.setFill(TEXT_CLR);
            String nearest = clickedUser.getNearestSite() != null
                    ? clickedUser.getNearestSite().getName() : "None";
            gc.fillText("Nearest: " + nearest, px+12, py+86);
            gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
            gc.setFill(Color.rgb(100,120,150));
            gc.fillText("Neighbours highlighted  |  Drag to move", px+12, py+104);
            return;
        }

        // ── Site panel ────────────────────────────────────────────────────────
        MedicalSite display = (clickedSite != null) ? clickedSite : hoveredSite;
        if (display == null) return;
        VoronoiCell cell = mapModel.getVoronoiDiagram().getCellBySite(display);
        boolean isHospital = display instanceof Hospital;
        Color   typeColor  = isHospital ? HOSPITAL_CLR : CENTER_CLR;
        String  typeLabel  = isHospital ? "HOSPITAL" : "COLLECTION CENTER";
        double  pw = 230, ph = cell != null ? 188 : 118;
        double  px = getWidth() - pw - 12;
        double  py = animating ? 62 : 12;
        gc.setFill(PANEL_BG); gc.fillRoundRect(px, py, pw, ph, 12, 12);
        gc.setStroke(typeColor); gc.setLineWidth(1.8);
        gc.strokeRoundRect(px, py, pw, ph, 12, 12);
        gc.setFill(Color.rgb((int)(typeColor.getRed()*255),
                (int)(typeColor.getGreen()*255),(int)(typeColor.getBlue()*255),0.18));
        gc.fillRoundRect(px, py, pw, 28, 12, 12); gc.fillRect(px, py+16, pw, 12);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        gc.setFill(typeColor); gc.fillText(typeLabel, px+12, py+19);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        gc.setFill(Color.WHITE); gc.fillText(display.getId(), px+12, py+58);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
        gc.setFill(Color.rgb(160,180,210)); gc.fillText("ID", px+12, py+37);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        gc.setFill(TEXT_CLR); gc.fillText(display.getName(), px+12, py+76);
        gc.setFill(Color.rgb(140,160,190));
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
        gc.fillText("X=" + (int)display.getPosition().getX()
                + "  Y=" + (int)display.getPosition().getY(), px+12, py+92);
        gc.setStroke(Color.rgb(50,70,100)); gc.setLineWidth(1);
        gc.strokeLine(px+8, py+100, px+pw-8, py+100);
        if (cell != null) {
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
            gc.setFill(Color.rgb(100,140,180)); gc.fillText("VORONOI ZONE", px+12, py+114);
            gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
            gc.setFill(TEXT_CLR);
            gc.fillText("Surface  : " + (int)cell.getSurface(),   px+12, py+130);
            gc.fillText("UserPts  : " + cell.getNumberOfUserPoints(), px+12, py+145);
            gc.fillText("Min dist : " + String.format("%.1f", cell.getMinDistanceToUserPoints()), px+12, py+160);
            gc.fillText("Avg dist : " + String.format("%.1f", cell.getAverageDistanceToUserPoints()), px+12, py+175);
        }
        if (clickedSite == null) {
            gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
            gc.setFill(Color.rgb(100,120,150));
            gc.fillText("Click to pin  |  Drag to move", px+12, py+ph-8);
        }
    }

    /**
     * Panel shown when a Delaunay triangle is selected.
     * Shows: surface, distances AB/BC/CA, user points per vertex.
     */
    private void drawTrianglePanel(GraphicsContext gc) {
        if (clickedTriangle == null) return;

        double pw = 270, ph = 200;
        double px = 12;
        double py = animating ? 62 : 12;

        gc.setFill(PANEL_BG); gc.fillRoundRect(px, py, pw, ph, 12, 12);
        gc.setStroke(DELAUNAY_SEL); gc.setLineWidth(1.8);
        gc.strokeRoundRect(px, py, pw, ph, 12, 12);

        gc.setFill(Color.rgb(90,130,210,0.18));
        gc.fillRoundRect(px, py, pw, 28, 12, 12); gc.fillRect(px, py+16, pw, 12);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        gc.setFill(DELAUNAY_SEL);
        gc.fillText("DELAUNAY TRIANGLE", px+12, py+19);

        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        gc.setFill(TEXT_CLR);

        // Vertices
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        gc.setFill(Color.rgb(100,140,180)); gc.fillText("VERTICES", px+12, py+42);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10)); gc.setFill(TEXT_CLR);
        gc.fillText("A: " + clickedTriangle.getSiteA().getName(), px+12, py+56);
        gc.fillText("B: " + clickedTriangle.getSiteB().getName(), px+12, py+70);
        gc.fillText("C: " + clickedTriangle.getSiteC().getName(), px+12, py+84);

        // Distances
        gc.setStroke(Color.rgb(50,70,100)); gc.setLineWidth(1);
        gc.strokeLine(px+8, py+92, px+pw-8, py+92);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        gc.setFill(Color.rgb(100,140,180)); gc.fillText("DISTANCES", px+12, py+106);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10)); gc.setFill(TEXT_CLR);
        gc.fillText("AB: " + String.format("%.1f", clickedTriangle.getDistanceAB()), px+12, py+120);
        gc.fillText("BC: " + String.format("%.1f", clickedTriangle.getDistanceBC()), px+12, py+134);
        gc.fillText("CA: " + String.format("%.1f", clickedTriangle.getDistanceCA()), px+12, py+148);

        // Surface
        gc.strokeLine(px+8, py+155, px+pw-8, py+155);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        gc.setFill(Color.rgb(100,140,180)); gc.fillText("SURFACE & USER POINTS", px+12, py+169);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10)); gc.setFill(TEXT_CLR);
        gc.fillText("Surface : " + String.format("%.0f", clickedTriangle.computeSurface()), px+12, py+183);

        // User points per vertex
        int ua = userPointsInCell(clickedTriangle.getSiteA());
        int ub = userPointsInCell(clickedTriangle.getSiteB());
        int uc = userPointsInCell(clickedTriangle.getSiteC());
        gc.fillText("UserPts  A=" + ua + "  B=" + ub + "  C=" + uc
                + "  imbalance=" + Math.abs(ua-ub)+"/"+Math.abs(ub-uc), px+12, py+197);

        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
        gc.setFill(Color.rgb(100,120,150));
        gc.fillText("Click elsewhere to deselect", px+12, py+ph-8);
    }

    private void drawStatsPanel(GraphicsContext gc) {
        if (statsText == null) return;
        String[] statLines = statsText.split("\n");
        double pw = 380, ph = 34 + statLines.length * 17 + 16;
        if (statsPanelX < 0) statsPanelX = (getWidth()  - pw) / 2;
        if (statsPanelY < 0) statsPanelY = (getHeight() - ph) / 2;
        double px = statsPanelX;
        double py = statsPanelY;
        gc.setFill(Color.rgb(0,0,0,0.45));
        gc.fillRoundRect(px+4, py+4, pw, ph, 14, 14);
        gc.setFill(PANEL_BG); gc.fillRoundRect(px, py, pw, ph, 14, 14);
        gc.setStroke(ACCENT); gc.setLineWidth(1.8);
        gc.strokeRoundRect(px, py, pw, ph, 14, 14);
        gc.setFill(Color.rgb(40,210,135,0.18));
        gc.fillRoundRect(px, py, pw, 30, 14, 14);
        gc.fillRect(px, py+18, pw, 12);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        gc.setFill(ACCENT); gc.fillText("STATISTICS", px+14, py+20);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        gc.setFill(Color.rgb(255,80,80));
        gc.fillText("X", px+pw-24, py+20);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
        gc.setFill(Color.rgb(100,160,120));
        gc.fillText("drag to move", px+110, py+20);
        for (int i = 0; i < statLines.length; i++) {
            String sl = statLines[i];
            if (sl.startsWith("--") || sl.startsWith("==")) {
                gc.setFill(Color.rgb(100,140,180));
                gc.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            } else {
                gc.setFill(TEXT_CLR);
                gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            }
            gc.fillText(sl, px+14, py+46 + i*17);
        }
    }

    /** Small zoom level indicator bottom-right. */
    private void drawZoomIndicator(GraphicsContext gc) {
        String txt = String.format("zoom: %.0f%%", scale * 100);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
        gc.setFill(Color.rgb(80,100,130));
        gc.fillText(txt, getWidth() - 90, getHeight() - 8);
    }

    private void drawLegend(GraphicsContext gc) {
        double x = 12, y = getHeight()-168;
        gc.setFill(PANEL_BG); gc.fillRoundRect(x-8, y-18, 215, 158, 10, 10);
        gc.setStroke(Color.rgb(80,120,180,0.35)); gc.setLineWidth(1);
        gc.strokeRoundRect(x-8, y-18, 215, 158, 10, 10);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        gc.setFill(ACCENT); gc.fillText("LEGEND", x, y);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        legendDot(gc, HOSPITAL_CLR, x, y+14); gc.setFill(TEXT_CLR); gc.fillText("Hospital",          x+18, y+23);
        legendSqr(gc, CENTER_CLR,   x, y+32); gc.setFill(TEXT_CLR); gc.fillText("Collection Center", x+18, y+41);
        legendHex(gc, BASE_CLR,     x, y+50); gc.setFill(TEXT_CLR); gc.fillText("Drone Base",         x+18, y+59);
        legendDot(gc, DRONE_FLY,    x, y+68); gc.setFill(TEXT_CLR); gc.fillText("Drone (in flight)",  x+18, y+77);
        legendDiamond(gc, USERPT_CLR, x, y+86); gc.setFill(TEXT_CLR); gc.fillText("User point",       x+18, y+95);
        gc.setStroke(ROUTE_CLR); gc.setLineWidth(2); gc.setLineDashes(6,4);
        gc.strokeLine(x, y+108, x+10, y+108); gc.setLineDashes((double[]) null);
        gc.setFill(TEXT_CLR); gc.fillText("Mission route",  x+18, y+112);
        gc.setStroke(DELAUNAY_CLR); gc.setLineWidth(1.5);
        gc.strokeLine(x, y+126, x+10, y+126);
        gc.setFill(TEXT_CLR); gc.fillText("Delaunay edge",  x+18, y+130);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
        gc.setFill(Color.rgb(80,100,130));
        gc.fillText("Wheel=zoom  Mid-drag=pan", x, y+148);
    }

    private void drawDragHint(GraphicsContext gc) {
        if (draggingSite != null || draggingBase != null || draggingUser != null) return;
        if (clickedSite != null || clickedBase != null || clickedUser != null) return;
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
        gc.setFill(Color.rgb(80, 100, 130));
        gc.fillText("Click = info  |  Drag = move  |  Wheel = zoom  |  Middle-drag = pan",
                14, getHeight() - 8);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    private void setupMouse() {
        // ── Zoom with mouse wheel ─────────────────────────────────────────────
        setOnScroll(e -> {
            double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            double mx = e.getX(), my = e.getY();
            // Zoom centered on cursor
            double newScale = Math.max(0.2, Math.min(5.0, scale * zoomFactor));
            offsetX = mx - (mx - offsetX) * (newScale / scale);
            offsetY = my - (my - offsetY) * (newScale / scale);
            scale = newScale;
            draw();
        });

        // ── Hover ─────────────────────────────────────────────────────────────
        setOnMouseMoved(e -> {
            double mx = toModelX(e.getX()), my = toModelY(e.getY());
            MedicalSite f = siteAt(mx, my);
            if (f != hoveredSite) {
                hoveredSite  = f;
                selectedCell = f != null ? mapModel.getVoronoiDiagram().getCellBySite(f) : null;
                draw();
            }
        });

        // ── Press ─────────────────────────────────────────────────────────────
        setOnMousePressed(e -> {
            didDrag = false;
            double sx = e.getX(), sy = e.getY();
            double mx = toModelX(sx), my = toModelY(sy);

            // Middle or secondary button → start pan
            if (e.isMiddleButtonDown() || e.isSecondaryButtonDown()) {
                panning = true; panStartX = sx; panStartY = sy; return;
            }

            // Stats close X
            if (statsText != null && isCloseX(sx, sy)) {
                statsText = null; statsPanelX = -1; statsPanelY = -1; draw(); return;
            }

            // Drag stats panel (click on header area)
            if (statsText != null && isStatsHeader(sx, sy)) {
                draggingStats = true;
                statsDragOffX = sx - statsPanelX;
                statsDragOffY = sy - statsPanelY;
                return;
            }

            // Try drag site
            MedicalSite site = siteAt(mx, my);
            if (site != null) {
                draggingSite = site;
                dragOffsetX  = mx - site.getPosition().getX();
                dragOffsetY  = my - site.getPosition().getY();
                return;
            }
            // Try drag base
            DroneBase base = baseAt(mx, my);
            if (base != null) {
                draggingBase = base;
                dragOffsetX  = mx - base.getPosition().getX();
                dragOffsetY  = my - base.getPosition().getY();
                return;
            }
            // Try drag user point
            UserPoint up = userPointAt(mx, my);
            if (up != null) {
                draggingUser = up;
                dragOffsetX  = mx - up.getPosition().getX();
                dragOffsetY  = my - up.getPosition().getY();
            }
        });

        // ── Drag ─────────────────────────────────────────────────────────────
        setOnMouseDragged(e -> {
            double sx = e.getX(), sy = e.getY();
            double mx = toModelX(sx), my = toModelY(sy);

            if (panning) {
                offsetX += sx - panStartX;
                offsetY += sy - panStartY;
                panStartX = sx; panStartY = sy;
                draw(); return;
            }

            if (draggingStats) {
                statsPanelX = sx - statsDragOffX;
                statsPanelY = sy - statsDragOffY;
                draw(); return;
            }

            didDrag = true;
            if (draggingSite != null) {
                mapModel.moveMedicalSite(draggingSite,
                        new Position(mx - dragOffsetX, my - dragOffsetY));
                draw();
            } else if (draggingBase != null) {
                draggingBase.getPosition().setX(mx - dragOffsetX);
                draggingBase.getPosition().setY(my - dragOffsetY);
                draw();
            } else if (draggingUser != null) {
                mapModel.moveUserPoint(draggingUser,
                        new Position(mx - dragOffsetX, my - dragOffsetY));
                draw();
            }
        });

        // ── Release ───────────────────────────────────────────────────────────
        setOnMouseReleased(e -> {
            if (panning) { panning = false; return; }
            if (draggingStats) { draggingStats = false; return; }
            boolean wasDragging = draggingSite != null
                    || draggingBase != null || draggingUser != null;
            draggingSite = null; draggingBase = null; draggingUser = null;
            if (!didDrag) handleClick(e.getX(), e.getY());
            didDrag = false;
            draw();
        });
    }

    private void handleClick(double sx, double sy) {
        double mx = toModelX(sx), my = toModelY(sy);

        if (statsText != null && isCloseX(sx, sy)) {
            statsText = null; statsPanelX = -1; statsPanelY = -1; draw(); return;
        }
        if (statsText != null && !isInsideStatsPanel(sx, sy)) {
            statsText = null; statsPanelX = -1; statsPanelY = -1;
        }

        // Priority: site > base > user point > triangle > empty
        MedicalSite site = siteAt(mx, my);
        if (site != null) {
            clickedBase = null; clickedUser = null; clickedTriangle = null;
            neighbourCells.clear();
            clickedSite  = site.equals(clickedSite) ? null : site;
            selectedCell = clickedSite != null
                    ? mapModel.getVoronoiDiagram().getCellBySite(clickedSite) : null;
            hoveredSite = site; draw(); return;
        }

        DroneBase base = baseAt(mx, my);
        if (base != null) {
            clickedSite = null; selectedCell = null;
            clickedUser = null; clickedTriangle = null; neighbourCells.clear();
            clickedBase = base.equals(clickedBase) ? null : base;
            draw(); return;
        }

        UserPoint up = userPointAt(mx, my);
        if (up != null) {
            clickedSite = null; selectedCell = null; clickedBase = null; clickedTriangle = null;
            if (up.equals(clickedUser)) {
                clickedUser = null; neighbourCells.clear();
            } else {
                clickedUser = up;
                neighbourCells = getNeighbourCells(up);
            }
            draw(); return;
        }

        // Try triangle click
        Triangle tri = triangleAt(mx, my);
        if (tri != null) {
            clickedSite = null; selectedCell = null;
            clickedBase = null; clickedUser = null; neighbourCells.clear();
            clickedTriangle = tri.equals(clickedTriangle) ? null : tri;
            draw(); return;
        }

        // Click in empty area → deselect all
        clickedSite = null; selectedCell = null; clickedBase = null;
        clickedUser = null; neighbourCells.clear(); clickedTriangle = null;
        draw();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns the cells that are neighbours of the given user point's zone
     * (all cells adjacent to cells in the triangulation that contain the nearest site).
     */
    private List<VoronoiCell> getNeighbourCells(UserPoint up) {
        List<VoronoiCell> result = new ArrayList<>();
        if (up.getNearestSite() == null) return result;
        List<VoronoiCell> cells = mapModel.getVoronoiDiagram().getCells();
        // Find the triangles that contain the nearest site
        for (Triangle t : mapModel.getDelaunayTriangulation().getTriangles()) {
            if (t.containsSite(up.getNearestSite())) {
                for (model.MedicalSite s : t.getSites()) {
                    VoronoiCell c = mapModel.getVoronoiDiagram().getCellBySite(s);
                    if (c != null && !result.contains(c)) result.add(c);
                }
            }
        }
        return result;
    }

    /** Returns number of user points in the Voronoi cell of the given site. */
    private int userPointsInCell(MedicalSite site) {
        VoronoiCell cell = mapModel.getVoronoiDiagram().getCellBySite(site);
        return cell != null ? cell.getNumberOfUserPoints() : 0;
    }

    /**
     * Returns the triangle whose centroid is closest to (mx,my) within 30px.
     */
    private Triangle triangleAt(double mx, double my) {
        Triangle best = null; double bestDist = 30;
        for (Triangle t : mapModel.getDelaunayTriangulation().getTriangles()) {
            double cx = (t.getSiteA().getPosition().getX()
                    + t.getSiteB().getPosition().getX()
                    + t.getSiteC().getPosition().getX()) / 3.0;
            double cy = (t.getSiteA().getPosition().getY()
                    + t.getSiteB().getPosition().getY()
                    + t.getSiteC().getPosition().getY()) / 3.0;
            double d = Math.sqrt((cx-mx)*(cx-mx)+(cy-my)*(cy-my));
            if (d < bestDist) { bestDist = d; best = t; }
        }
        return best;
    }

    private boolean isStatsHeader(double sx, double sy) {
        if (statsText == null || statsPanelX < 0) return false;
        String[] sl = statsText.split("\n");
        double pw = 380;
        return sx >= statsPanelX && sx <= statsPanelX + pw - 30
                && sy >= statsPanelY && sy <= statsPanelY + 30;
    }

    private boolean isCloseX(double sx, double sy) {
        if (statsText == null || statsPanelX < 0) return false;
        String[] sl = statsText.split("\n");
        double pw = 380;
        return sx >= statsPanelX+pw-32 && sx <= statsPanelX+pw-8
                && sy >= statsPanelY+4    && sy <= statsPanelY+28;
    }

    private boolean isInsideStatsPanel(double sx, double sy) {
        if (statsText == null || statsPanelX < 0) return false;
        String[] sl = statsText.split("\n");
        double pw = 380, ph = 34 + sl.length*17 + 16;
        return sx >= statsPanelX && sx <= statsPanelX+pw
                && sy >= statsPanelY && sy <= statsPanelY+ph;
    }

    private Position findBasePosition(Drone drone) {
        for (DroneBase base : mapModel.getDroneBases())
            for (Drone d : base.getDrones())
                if (d.equals(drone))
                    return new Position(base.getPosition().getX(), base.getPosition().getY());
        return null;
    }

    private void stopAnimation() {
        if (animationTimer != null) { animationTimer.stop(); animationTimer = null; }
        animating = false;
    }

    private MedicalSite siteAt(double mx, double my) {
        for (MedicalSite s : mapModel.getMedicalSites()) {
            double dx = s.getPosition().getX()-mx, dy = s.getPosition().getY()-my;
            if (Math.sqrt(dx*dx+dy*dy) < 14) return s;
        }
        return null;
    }

    private DroneBase baseAt(double mx, double my) {
        for (DroneBase b : mapModel.getDroneBases()) {
            double dx = b.getPosition().getX()-mx, dy = b.getPosition().getY()-my;
            if (Math.sqrt(dx*dx+dy*dy) < 16) return b;
        }
        return null;
    }

    private UserPoint userPointAt(double mx, double my) {
        for (UserPoint up : mapModel.getUserPoints()) {
            double dx = up.getPosition().getX()-mx, dy = up.getPosition().getY()-my;
            if (Math.sqrt(dx*dx+dy*dy) < 10) return up;
        }
        return null;
    }

    private void droneIcon(GraphicsContext gc, double x, double y,
                           Color color, String id, boolean flying) {
        double s = flying ? 1.45 : 1.0;
        gc.setFill(color); gc.setStroke(color);
        gc.setLineWidth((flying ? 2.4 : 1.8) / scale);
        gc.fillOval(x-5*s, y-5*s, 10*s, 10*s);
        gc.strokeLine(x, y, x-11*s, y-11*s); gc.strokeLine(x, y, x+11*s, y-11*s);
        gc.strokeLine(x, y, x-11*s, y+11*s); gc.strokeLine(x, y, x+11*s, y+11*s);
        gc.setFill(Color.rgb(255,255,255, flying ? 0.82 : 0.45));
        double rs = 4.5*s;
        gc.fillOval(x-14*s-rs, y-14*s-rs, rs*2, rs*2);
        gc.fillOval(x+14*s-rs, y-14*s-rs, rs*2, rs*2);
        gc.fillOval(x-14*s-rs, y+14*s-rs, rs*2, rs*2);
        gc.fillOval(x+14*s-rs, y+14*s-rs, rs*2, rs*2);
        label(gc, id, x+16*s, y+5);
    }

    private Position interpolate(List<Position> path, double t) {
        if (path == null || path.size() < 2) return null;
        double total = 0;
        for (int i = 0; i < path.size()-1; i++) total += path.get(i).distanceTo(path.get(i+1));
        if (total == 0) return null;
        double target = t * total, walked = 0;
        for (int i = 0; i < path.size()-1; i++) {
            Position from = path.get(i), to = path.get(i+1);
            double seg = from.distanceTo(to);
            if (walked + seg >= target) {
                double u = (target - walked) / seg;
                return new Position(from.getX()+u*(to.getX()-from.getX()),
                        from.getY()+u*(to.getY()-from.getY()));
            }
            walked += seg;
        }
        return path.get(path.size()-1);
    }

    private void label(GraphicsContext gc, String t, double x, double y) {
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, Math.max(8, 11 / scale)));
        gc.setFill(Color.rgb(0,0,0,0.55)); gc.fillText(t, x+1, y+1);
        gc.setFill(TEXT_CLR);              gc.fillText(t, x,   y);
    }

    private void dot(GraphicsContext gc, Color c, double cx, double cy, double r)
    { gc.setFill(c); gc.fillOval(cx-r, cy-r, r*2, r*2); }
    private void legendDot(GraphicsContext gc, Color c, double x, double y)
    { gc.setFill(c); gc.fillOval(x, y, 10, 10); }
    private void legendSqr(GraphicsContext gc, Color c, double x, double y)
    { gc.setFill(c); gc.fillRect(x, y, 10, 10); }
    private void legendDiamond(GraphicsContext gc, Color c, double x, double y) {
        gc.setFill(c);
        double cx = x+5, cy = y+5;
        gc.fillPolygon(new double[]{cx,cx+5,cx,cx-5}, new double[]{cy-5,cy,cy+5,cy}, 4);
    }
    private void legendHex(GraphicsContext gc, Color c, double x, double y) {
        gc.setFill(c);
        double cx = x+5, cy = y+5;
        gc.fillPolygon(new double[]{cx,cx-5,cx-5,cx,cx+5,cx+5},
                new double[]{cy-6,cy-3,cy+3,cy+6,cy+3,cy-3}, 6);
    }
}