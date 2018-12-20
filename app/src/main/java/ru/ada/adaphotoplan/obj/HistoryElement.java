package ru.ada.adaphotoplan.obj;


import java.util.List;

/**
 * Created by Bitizen on 10.06.17.
 */

public abstract class HistoryElement {

    protected PhotoPlanProject project;

    public HistoryElement(PhotoPlanProject project) {
        this.project = project;
    }

    public abstract HistoryElement revert();

    public static class LineAdd extends HistoryElement {

       private Line addedLine;

        public LineAdd(PhotoPlanProject project, Line addedLine) {
            super(project);
            this.addedLine = addedLine;
        }

        @Override
        public HistoryElement revert() {
            project.deleteLine(addedLine);
            return new HistoryElement.LineDelete(project, addedLine);
        }
    }

    public static class PointMove extends HistoryElement {

        private RealmPoint prevPosition;
        private RealmPoint newPosition;

        public PointMove(PhotoPlanProject project, RealmPoint prevPosition, RealmPoint newPosition) {
            super(project);
            this.prevPosition = prevPosition;
            this.newPosition = newPosition;
        }

        @Override
        public HistoryElement revert() {
            project.movePoint(newPosition, prevPosition);
            return new PointMove(project, newPosition, prevPosition);
        }
    }

    public static class LineDelete extends HistoryElement {

        private Line deletedLine;

        public LineDelete(PhotoPlanProject project, Line deletedLine) {
            super(project);
            this.deletedLine = deletedLine;
        }

        @Override
        public HistoryElement revert() {
            project.addLine(deletedLine);
            return new LineAdd(project, deletedLine);
        }
    }

    public static class PointDelete extends HistoryElement {

        protected List<Line> deletedLines;

        public PointDelete(PhotoPlanProject project, List<Line> deletedLines) {
            super(project);
            this.deletedLines = deletedLines;
        }

        @Override
        public HistoryElement revert() {
            for (Line deletedLine : deletedLines) {
                project.addLine(deletedLine);
            }
            return new PointRestore(project, deletedLines);
        }
    }

    private static class PointRestore extends PointDelete {

        public PointRestore(PhotoPlanProject project, List<Line> deletedLines) {
            super(project, deletedLines);
        }

        @Override
        public HistoryElement revert() {
            for (Line deletedLine : deletedLines) {
                project.deleteLine(deletedLine);
            }
            return new PointDelete(project, deletedLines);
        }
    }

    public static class LabelChanged extends HistoryElement {

        private Line line;
        private String prevLabel;

        public LabelChanged(PhotoPlanProject project, Line line, String prevLabel) {
            super(project);
            this.line = line;
            this.prevLabel = prevLabel;
        }

        @Override
        public HistoryElement revert() {
            String label = line.getTextOnLine();
            project.setTextLine(line, prevLabel);
            return new LabelChanged(project, line, label);
        }
    }
}
