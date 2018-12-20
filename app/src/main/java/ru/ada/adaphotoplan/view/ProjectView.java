package ru.ada.adaphotoplan.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.List;

import ru.ada.adaphotoplan.helpers.DrawHelper;
import ru.ada.adaphotoplan.helpers.MathHelper;
import ru.ada.adaphotoplan.interfaces.OnSelectionListener;
import ru.ada.adaphotoplan.interfaces.OnTouchPreview;
import ru.ada.adaphotoplan.obj.HistoryElement;
import ru.ada.adaphotoplan.obj.Line;
import ru.ada.adaphotoplan.obj.PhotoPlanProject;
import ru.ada.adaphotoplan.obj.RealmPoint;

/**
 * Created by Bitizen on 09.06.17.
 */

public class ProjectView extends View {
    private static final String TAG = "ProjectView";

    /* Screen measures */

    private PhotoPlanProject project;

    private boolean drawMode = false;

    private DrawHelper drawHelper;

    /* Pre-adding mode */
    private RealmPoint touch;

    // Line buffers
    private RealmPoint startPoint;
    private RealmPoint endPoint;

    // Cursor buffers
    private RealmPoint movingPoint;
    private RealmPoint newPoint;
    private RealmPoint selectedPoint;
    private Line selectedLine;

    private OnSelectionListener selectionListener;

    private RealmPoint pureTouch;
    private OnTouchPreview touchPreview;

    private HistoryElement historyElements;


    public ProjectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setProject(PhotoPlanProject project) {
        this.project = project;
        this.drawHelper = new DrawHelper(getContext(), this.project);
        invalidate();
    }

    private float[] f = new float[9];

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (project != null) {
            limitZoom(viewMatrix);
            canvas.save();
            canvas.concat(viewMatrix);

            drawHelper.drawBackground(canvas);

            if (touch != null)
                drawHelper.drawTouch(canvas, touch.x, touch.y, f[Matrix.MSCALE_X]);
            if (startPoint != null && endPoint == null)
                drawHelper.drawOnePoint(canvas, startPoint.x, startPoint.y);
            else if (startPoint != null) {
                drawHelper.drawFutureLine(canvas, startPoint, endPoint);
            }

            drawHelper.drawLines(canvas, movingPoint, newPoint, selectedPoint, selectedLine);


            canvas.restore();

        }
    }

    public void setPosition(int x, int y, float scale) {
        viewMatrix.setScale(scale, scale);
        viewMatrix.setTranslate(x, y);
        invertMatrix = new Matrix(viewMatrix);
        invertMatrix.invert(invertMatrix);
        invalidate();
    }

    private static final float MIN_ZOOM = .4f;
    private static final float MAX_ZOOM = 3.f;

    private void limitZoom(Matrix m) {

        float[] values = new float[9];
        m.getValues(values);
        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];
        if(scaleX > MAX_ZOOM) {
            scaleX = MAX_ZOOM;
        } else if(scaleX < MIN_ZOOM) {
            scaleX = MIN_ZOOM;
        }

        if(scaleY > MAX_ZOOM) {
            scaleY = MAX_ZOOM;
        } else if(scaleY < MIN_ZOOM) {
            scaleY = MIN_ZOOM;
        }

        values[Matrix.MSCALE_X] = scaleX;
        values[Matrix.MSCALE_Y] = scaleY;
        m.setValues(values);
    }

    private ScaleGestureDetector scaleGestureDetector;
    private Matrix viewMatrix = new Matrix();
    private Matrix invertMatrix = new Matrix();
    private int prevX;
    private int prevY;

    private boolean isPoint = false;
    private boolean multitouch = false;

    private int sensDist = 45;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        int action = event.getAction();
        MotionEvent pureEvent = MotionEvent.obtain(event);
        event.transform(invertMatrix);
        int x = (int) event.getX();
        int y = (int) event.getY();
        int inteval = project.getInterval();
        int mX = x - x % inteval;
        int mY = y - y % inteval;

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN)
            isPoint = false;

        for (RealmPoint point : project.getPoints()) {
            if (point.equals(movingPoint))
                continue;
            if (MathHelper.distance(new RealmPoint(mX, mY), point) < sensDist) {
                mX = point.x;
                mY = point.y;
                isPoint = true;
                break;
            }
        }
        if (!drawMode && action == MotionEvent.ACTION_DOWN && !isPoint && event.getPointerCount() == 1) {
            for (Line line : project.getLines()) {
                double lineLength = MathHelper.distance(line.start, line.end);
                double sum = MathHelper.distance(line.start, new RealmPoint(x, y)) +
                        MathHelper.distance(line.end, new RealmPoint(x, y));
                if (lineLength > sum - 20) { // думаю стоит переделать
                    selectedLine = line;
                    if (selectionListener != null)
                        selectionListener.selected();
                    selectedPoint = null;
                    break;
                }
            }
        }

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            touch = new RealmPoint(x, y);
            Bitmap b = Bitmap.createBitmap(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            layout(getLeft(), getTop(), getRight(), getBottom());
            draw(c);
            int width = 128, height = 128;
            if (pureEvent.getX() - width/2 >= 0 &&
                    (int) pureEvent.getY() - height/2 >= 0 &&
                    (int) pureEvent.getX() + width <= b.getWidth() &&
                    (int) pureEvent.getY() + height <= b.getHeight())
                touchPreview.onTouch(Bitmap.createBitmap(b, (int) pureEvent.getX() - width/2, (int) pureEvent.getY() - height/2, width, height), (int) pureEvent.getX(), (int) pureEvent.getY());
        } else if (action == MotionEvent.ACTION_UP) {
            touch = null;
            if (touchPreview != null){
                touchPreview.onRelease();
            }
        }

        if (drawMode) {
            if (action == MotionEvent.ACTION_DOWN) {
                if (startPoint == null)
                    startPoint = new RealmPoint(mX, mY);
                else if (endPoint == null)
                    endPoint = new RealmPoint(mX, mY);
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (MathHelper.distance(new RealmPoint(mX, mY), startPoint) > sensDist )
                    endPoint = new RealmPoint(mX, mY);

            } else if (action == MotionEvent.ACTION_UP) {
                if (startPoint != null && endPoint != null) {
                    Line line = new Line(startPoint, endPoint);
                    project.addLine(line);
                    historyElements = new HistoryElement.LineAdd(project, line);
                    startPoint = null;
                    endPoint = null;
                }
            }
        }

        if (!drawMode) {
            if (isPoint && action == MotionEvent.ACTION_DOWN ) {
                selectedLine = null;
                if (selectionListener != null)
                    selectionListener.unSelected();
                movingPoint = new RealmPoint(mX, mY);
                newPoint = movingPoint;
                selectedPoint = movingPoint;
                if (selectionListener != null)
                    selectionListener.selected();
            }
            if (action == MotionEvent.ACTION_MOVE && movingPoint != null) {
                double distance = MathHelper.distance(movingPoint, new RealmPoint(mX, mY));
                if (distance > sensDist || !movingPoint.equals(new RealmPoint(mX, mY))) {
                    newPoint = new RealmPoint(mX, mY);
                    if (selectionListener != null)
                        selectionListener.selected();
                    selectedPoint = newPoint;
                }
            }
            if (action == MotionEvent.ACTION_UP) {
                if (newPoint != null) {
                    selectedPoint = newPoint;
                    if (selectionListener != null)
                        selectionListener.selected();
                    project.movePoint(movingPoint, newPoint);
                    historyElements = new HistoryElement.PointMove(project, movingPoint, newPoint);
                    movingPoint = null;
                    newPoint = null;
                }
            }
        }
        if (!drawMode) {
            if (event.getPointerCount() >= 2) {
                scaleGestureDetector.onTouchEvent(pureEvent);
                multitouch = true;
            } else if (!isPoint && !multitouch){
                if (action == MotionEvent.ACTION_DOWN) {
                    prevX = x;
                    prevY = y;
                } else if (action == MotionEvent.ACTION_MOVE) {
                    Matrix transformMatrix = new Matrix();
                    transformMatrix.postTranslate(-x, -y);

                    float shiftX = x - prevX;
                    float shiftY = y - prevY;
                    transformMatrix.postTranslate(x + shiftX, y + shiftY);
                    viewMatrix.postConcat(transformMatrix);
                }
            }
        }

        if (action == MotionEvent.ACTION_UP)
            multitouch = false;
        invertMatrix = new Matrix(viewMatrix);
        invertMatrix.invert(invertMatrix);

        postInvalidateOnAnimation();
        return true;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        float lastFocusX;
        float lastFocusY;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            lastFocusX = detector.getFocusX();
            lastFocusY = detector.getFocusY();
            return true;
        }
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            Matrix transformationMatrix = new Matrix();
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            transformationMatrix.postTranslate(-focusX, -focusY);

            transformationMatrix.postScale(detector.getScaleFactor(), detector.getScaleFactor());

            float focusShiftX = focusX - lastFocusX;
            float focusShiftY = focusY - lastFocusY;
            transformationMatrix.postTranslate(focusX + focusShiftX, focusY + focusShiftY);
            viewMatrix.postConcat(transformationMatrix);
            lastFocusX = focusX;
            lastFocusY = focusY;

            invalidate();
            return true;
        }
    }

    public void undo() {
        if (historyElements != null)
            historyElements = historyElements.revert();
        invalidate();
    }

    public boolean isDrawMode() {
        return drawMode;
    }

    public void setDrawMode(boolean drawMode) {
        this.drawMode = drawMode;
        resetSelected();
        invalidate();
    }

    public void setSelectionListener(OnSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    public void setTouchPreview(OnTouchPreview touchPreview) {
        this.touchPreview = touchPreview;
    }

    public boolean addLabel(String string) {
        if (selectedLine == null)
            return false;
        historyElements = new HistoryElement.LabelChanged(project, selectedLine, selectedLine.getTextOnLine());
        project.setTextLine(selectedLine, string);
        invalidate();
        return true;
    }

    public void deleteSelected() {
        if (selectedPoint != null) {
            List<Line> lines = project.deleteByPoint(selectedPoint);
            historyElements = new HistoryElement.PointDelete(project, lines);
        }
        if (selectedLine != null) {
            project.deleteLine(selectedLine);
            historyElements = new HistoryElement.LineDelete(project, selectedLine);
        }
        selectedPoint = null;
        selectedLine = null;
        selectionListener.unSelected();
        invalidate();
    }

    private void resetSelected() {
        movingPoint = null;
        newPoint = null;
        selectedPoint = null;
        selectedLine = null;
        if (selectionListener != null)
            selectionListener.unSelected();
        startPoint = null;
    }
}
