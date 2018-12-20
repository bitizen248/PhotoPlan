package ru.ada.adaphotoplan.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import ru.ada.adaphotoplan.obj.Line;
import ru.ada.adaphotoplan.obj.PhotoPlanProject;
import ru.ada.adaphotoplan.obj.RealmPoint;


/**
 * Created by Bitizen on 09.06.17.
 */

public class DrawHelper {
    private static final String TAG = "DrawHelper";

    private float DPI;

    private Palette palette;

    private PhotoPlanProject project;

    public DrawHelper(Context context, PhotoPlanProject project) {
        this.DPI = context.getResources().getDisplayMetrics().density;
        this.palette = new Palette();
        this.project = project;
    }

    public void drawBackground(Canvas canvas) {
        if (project.getType() == PhotoPlanProject.TYPE_GRID) {
            int width = project.getWidth();
            int height = project.getHeight();
            int interval = project.getInterval();
            for (int i = interval; i < height; i += interval) {
                canvas.drawLine(0, i, width, i, palette.prelines);
            }
            for (int i = interval; i < width; i += interval) {
                canvas.drawLine(i, 0, i, height, palette.prelines);
            }
        } else if (project.getType() == PhotoPlanProject.TYPE_BITMAP) {
            Bitmap bitmap = project.getBitmap();
            if (bitmap != null)
                canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

    public void drawTouch(Canvas canvas, int x, int y, float scale) {
        canvas.drawCircle(x, y, palette.touchRadius / scale, palette.touch);
    }

    public void drawOnePoint(Canvas canvas, int x, int y) {
        canvas.drawCircle(x, y, palette.prepointRadius, palette.prePoint);
    }

    public void drawFutureLine(Canvas canvas, RealmPoint start, RealmPoint end) {
        drawLine(canvas, new Line(start, end), false);
        drawPoint(canvas, start, false);
        drawPoint(canvas, end, false);
    }

    public void drawLines(Canvas canvas, RealmPoint movingPoint, RealmPoint newPoint, RealmPoint selectedPoint, Line selectedLine) {
        Set<RealmPoint> selectedPoints = new HashSet<>();
        selectedPoints.add(selectedPoint);

        for (Line line : project.getLines()) {
            Line copy = new Line(line);
            RealmPoint start = line.start;
            RealmPoint end = line.end;
            if (line.equals(selectedLine)) {
                selectedPoints.add(start);
                selectedPoints.add(end);
            }
            if (start.equals(movingPoint)) {
                if (newPoint != null)
                    start = newPoint;
            }
            if (end.equals(movingPoint)) {
                if (newPoint != null)
                    end = newPoint;
            }
            copy.start = start;
            copy.end = end;

            drawLine(canvas, copy, line.equals(selectedLine));
        }

        for (RealmPoint point : project.getPoints()) {
            if (!point.equals(movingPoint))
                drawPoint(canvas, point, selectedPoints.contains(point));
            else
                drawPoint(canvas, newPoint, true);
        }
    }

    private void drawPoint(Canvas canvas, RealmPoint point, boolean isSelected) {
        if (isSelected) {
            canvas.drawCircle(point.x, point.y, palette.pointRadius, palette.selectedPointBg);
            canvas.drawCircle(point.x, point.y, palette.pointRadius, palette.selectedPoint);
        } else  {
            canvas.drawCircle(point.x, point.y, palette.pointRadius, palette.pointBg);
            canvas.drawCircle(point.x, point.y, (10* DPI), palette.strokePoint);
            canvas.drawCircle(point.x, point.y, (6 * DPI), palette.centerPoint);
        }
    }

    private void drawLine(Canvas canvas, Line line, boolean isSelected) {
        if (line.getTextOnLine() != null) {
            int x = (line.start.x + line.end.x) / 2;
            int y = (line.start.y + line.end.y) / 2;
            double angle = Math.toDegrees(Math.atan2(line.end.x - line.start.x, line.end.y - line.start.y));
            canvas.save();
            Paint.FontMetrics fm = new Paint.FontMetrics();
            Log.i(TAG, "drawLine: " + angle);
            if (angle >= 0 && angle < 90) {
                angle = -angle + 90;
            } else if (angle >= 90 && angle <= 180) {
                angle = -angle - 270;
            } else if (angle < 0 && angle > -90) {
                angle = -angle - 90;
            } else if (angle <= -90 && angle >= -180) {
                angle = -angle + 270;
            }
            canvas.rotate((float) angle, x,y);
            int margin = (int) (4 * DPI);
            x += (angle / -angle)*palette.text.measureText(line.getTextOnLine()) / 2;
            y -= (4 * DPI) + margin;
            palette.text.getFontMetrics(fm);
            canvas.drawRect(x - margin,
                    y + fm.top,
                    x + palette.text.measureText(line.getTextOnLine()) + margin,
                    y + fm.bottom + margin,
                    palette.textBg);
            canvas.drawText(line.getTextOnLine(), x, y, palette.text);
            canvas.restore();
        }
        if (isSelected) {
            canvas.drawLine(line.start.x, line.start.y, line.end.x, line.end.y, palette.selectedLineBg);
            canvas.drawLine(line.start.x, line.start.y, line.end.x, line.end.y, palette.line );
        } else  {
            canvas.drawLine(line.start.x, line.start.y, line.end.x, line.end.y, palette.lineBg);
            canvas.drawLine(line.start.x, line.start.y, line.end.x, line.end.y, palette.line );
        }
    }

    @Nullable
    public static Bitmap renderThumbnail(Context context, int width, int height, int id) {
        PhotoPlanProject project = ProjectFactory.openProject(id, true, false);
        DrawHelper helper = new DrawHelper(context, project);
        if (width > 0 && height > 0 && project != null) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            RealmPoint[] corner = project.getCorners();
            if (corner != null)
                canvas.translate(-(corner[0].x - 100), -(corner[0].y - 100));
            helper.drawBackground(canvas);
            helper.drawLines(canvas, null, null, null, null);
            return bitmap;
        }
        return null;
    }

    @Nullable
    public static Bitmap renderPlan(Context context, int id) {
        PhotoPlanProject project = ProjectFactory.openProject(id, true, false);
        if (project == null)
            return null;
        DrawHelper helper = new DrawHelper(context, project);
        Bitmap bitmap = null;
        if (project.getType() == PhotoPlanProject.TYPE_GRID) {
            RealmPoint points[] = project.getCorners();
            if (points != null) {
                Log.e(TAG, "renderPlan: {" + points[0].x + " " + points[0].y + " },{ "  + points[1].x + " " + points[1].y + "}");
                int width = points[1].x - points[0].x + project.getInterval() * 4;
                int height = points[1].y - points[0].y + project.getInterval() * 4;
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                canvas.translate(-(points[0].x - project.getInterval() * 2), -(points[0].y - project.getInterval() * 2));
                helper.drawBackground(canvas);
                helper.drawLines(canvas, null, null, null, null);
            }
        } else if (project.getType() == PhotoPlanProject.TYPE_BITMAP) {
            bitmap = Bitmap.createBitmap(project.getWidth(), project.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            helper.drawBackground(canvas);
            helper.drawLines(canvas, null, null, null, null);
        }
        return bitmap;
    }

    private class Palette {

        Paint prelines;

        Paint touch;
        int touchRadius = (int) (20 * DPI);

        Paint line;
        Paint lineBg;
        Paint selectedLineBg;

        int pointRadius = (int) (12 * DPI);

        Paint selectedPointBg;
        Paint selectedPoint;

        Paint pointBg;
        Paint strokePoint;
        Paint centerPoint;

        Paint prePoint;
        int prepointRadius = (int) (4 * DPI);

        Paint textBg;
        Paint text;

        Palette() {
            prelines = new Paint();
            prelines.setColor(Color.parseColor("#80ffffff"));
            prelines.setStrokeWidth((int)(1 * DPI));

            touch = new Paint();
            touch.setColor(Color.parseColor("#80ee1c1c"));

            line = new Paint();
            line.setColor(Color.WHITE);
            line.setStrokeWidth((int)(4 * DPI));

            lineBg = new Paint();
            lineBg.setColor(Color.BLACK);
            lineBg.setStrokeWidth((int)(8 * DPI));

            selectedLineBg = new Paint();
            selectedLineBg.setColor(Color.parseColor("#ee1c1c"));
            selectedLineBg.setStrokeWidth((int)(8 * DPI));

            selectedPointBg = new Paint();
            selectedPointBg.setStyle(Paint.Style.FILL);
            selectedPointBg.setColor(Color.parseColor("#ee1c1c"));

            selectedPoint = new Paint();
            selectedPoint.setStrokeWidth(2 * DPI);
            selectedPoint.setStyle(Paint.Style.STROKE);
            selectedPoint.setColor(Color.WHITE);

            pointBg = new Paint();
            pointBg.setStyle(Paint.Style.FILL);
            pointBg.setColor(Color.BLACK);

            strokePoint = new Paint();
            strokePoint.setStyle(Paint.Style.STROKE);
            strokePoint.setColor(Color.WHITE);
            strokePoint.setStrokeWidth(2 * DPI);

            centerPoint = new Paint();
            centerPoint.setStyle(Paint.Style.FILL);
            centerPoint.setColor(Color.WHITE);

            prePoint = new Paint();
            prePoint.setColor(Color.WHITE);

            textBg = new Paint();
            textBg.setStyle(Paint.Style.FILL);
            textBg.setColor(Color.WHITE);

            text = new Paint();
            text.setColor(Color.BLACK);
            text.setTextSize(14f * DPI);
        }
    }
}
