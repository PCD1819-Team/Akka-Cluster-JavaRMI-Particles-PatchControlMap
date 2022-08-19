package it.unibo.isi.pcd.view;

import java.awt.geom.Point2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import it.unibo.isi.pcd.utils.Event;
import it.unibo.isi.pcd.utils.GeographicArea;
import it.unibo.isi.pcd.utils.StartEvent;
import it.unibo.isi.pcd.utils.StopEvent;
import it.unibo.isi.pcd.utils.UpdatePatchEventState;
import it.unibo.isi.pcd.utils.notifiable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;

public class MainFXMLController implements Initializable {

	private static final int GUARDIANS_DIVIDER_FOR_PATCH = 5;
	private static final int SENSORS_SIZE = 10;
	private static final int MAX_CIRCLES = 500;
	private static final Color EARLY_ALERT_COLOR = Color.DARKORANGE;
	private static final Color ALERT_COLOR = Color.PALEVIOLETRED;
	private static final Color OK_COLOR = Color.PALEGREEN;
	private static final Color DISABLE_COLOR = Color.GREY;

	private Stage mainStage;
	private Double oldHeigth;
	private Double oldWidth;
	private int numRow;
	private int numCol;
	private final List<Rectangle> rectanglePatches;
	private final List<Pair<Rectangle, Rectangle>> rectangleGuardians;
	private final List<Label> labels;
	private final Circle[] circleSensors;
	private notifiable dashboard;

	/** Nodes **/

	@FXML
	private AnchorPane anPane;

	@FXML
	private Pane pane2D;

	@FXML
	private ScrollPane scroolPane2D;
	@FXML
	private Button startStopBtn;

	@FXML
	private TextField numberRow;

	@FXML
	private TextField numberColoumns;

	@FXML
	private GridPane gridPane;

	@FXML
	private final List<Pair<Integer, Integer>> listGuardiansNumbers;

	public MainFXMLController() {
		this.listGuardiansNumbers = new ArrayList<>();
		this.circleSensors = new Circle[MainFXMLController.MAX_CIRCLES];
		this.rectangleGuardians = new ArrayList<>();
		this.rectanglePatches = new ArrayList<>();
		this.labels = new ArrayList<>();
		this.oldHeigth = 0.0;
		this.oldWidth = 0.0;

	}

	@FXML
	private void startStopAction() {

		this.startStopBtn.setDisable(true);
		this.numberRow.setDisable(true);
		this.numberColoumns.setDisable(true);
		this.numRow = Integer.parseInt(this.numberRow.getText());
		this.numCol = Integer.parseInt(this.numberColoumns.getText());

		this.oldWidth = this.scroolPane2D.getWidth();
		this.oldHeigth = this.scroolPane2D.getHeight();
		this.pane2D.getChildren().clear();

		this.pane2D.setPrefHeight(this.scroolPane2D.getHeight() - 2);
		this.pane2D.setPrefWidth(this.scroolPane2D.getWidth() - 2);
		this.pane2D.setMaxHeight(Double.MAX_VALUE);
		this.pane2D.setMaxWidth(Double.MAX_VALUE);
		final double width = this.oldWidth / this.numCol;
		final double heigth = this.oldHeigth / this.numRow;

		for (int i = 0; i < this.numCol; i++) {
			for (int j = 0; j < this.numRow; j++) {
				this.rectanglePatches.add(new Rectangle());
			}
		}

		for (int i = 0, counter = 0; i < this.numCol; i++, counter = i) {
			for (int j = 0; j < this.numRow; j++, counter += this.numCol) {
				final javafx.scene.control.Label lab = new javafx.scene.control.Label();

				this.rectanglePatches.get(counter).setX(i * width);
				this.rectanglePatches.get(counter).setY(j * heigth);
				this.rectanglePatches.get(counter).setWidth(width);
				this.rectanglePatches.get(counter).setHeight(heigth);
				this.rectanglePatches.get(counter).setFill(null);
				this.rectanglePatches.get(counter).setStroke(javafx.scene.paint.Color.BLACK);
				this.listGuardiansNumbers.add(new Pair<>(1, 1));
				lab.setText(String.valueOf(counter));
				lab.setTranslateX((i * width) + 2);
				lab.setTranslateY((j * heigth) + 2);

				this.labels.add(lab);
				this.pane2D.getChildren().addAll(this.rectanglePatches.get(counter), lab);

				this.rectanglePatches.get(counter).setOnMouseClicked(event -> {
					final Rectangle rectangle = (Rectangle) event.getSource();
					if (rectangle.getFill().equals(MainFXMLController.ALERT_COLOR)) {
						this.dashboard.addEvent(new UpdatePatchEventState(Event.EventType.RESTORE,
								this.rectanglePatches.indexOf(rectangle)));
					}
				});
			}
		}

		final List<Pair<Point2D, Point2D>> listPatch = this.rectanglePatches.stream().map(rectangle -> {
			final Point2D topLeftPoint;
			final Point2D bottomRigthPoint;
			topLeftPoint = new Point2D.Double(rectangle.getX(), rectangle.getY());
			bottomRigthPoint = new Point2D.Double(rectangle.getX() + rectangle.getWidth(),
					rectangle.getY() + rectangle.getHeight());
			return new Pair<>(topLeftPoint, bottomRigthPoint);

		}).collect(Collectors.toList());

		this.dashboard.addEvent(new StartEvent(this.numRow * this.numCol, listPatch));
	}

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
		this.initTextField();
	}

	private void initTextField() {
		this.numberRow.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*") || (this.numberRow.getText().length() > 1)) {
				this.numberRow.setText(oldValue);
			}
		});
		this.numberRow.setText("5");

		this.numberColoumns.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*") || (this.numberColoumns.getText().length() > 1)) {
				this.numberColoumns.setText(oldValue);
			}
		});
		this.numberColoumns.setText("5");
	}

	public void setStage(final Stage mainStage) {
		this.mainStage = mainStage;
		this.mainStage.setOnCloseRequest(content -> {
			this.dashboard.addEvent(new StopEvent());
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
			}
			Platform.runLater(() -> {
				Platform.exit();
			});
		});
	}

	public void removeSensors(final int sensorsNumber) {

		Platform.runLater(() -> {
			final Circle cir = this.circleSensors[sensorsNumber];

			if (cir != null) {
				this.circleSensors[sensorsNumber] = null;
				this.pane2D.getChildren().remove(cir);
			}
		});

	}

	public void AlertPatch(final int patchID) {
		Platform.runLater(() -> {
			this.rectanglePatches.get(patchID).setFill(MainFXMLController.ALERT_COLOR);

			final List<Pair<Rectangle, Rectangle>> c = this.rectangleGuardians.stream()
					.filter(val -> val.getSecond().equals(this.rectanglePatches.get(patchID)))
					.collect(Collectors.toList());
			final int counter = 0;
			for (final Pair<Rectangle, Rectangle> x : c) {
				x.getFirst().setFill(MainFXMLController.ALERT_COLOR);
			}
		});
	}

	public void EarlyAlertGuard(final int patchID) {
		Platform.runLater(() -> {

			final java.util.Optional<Rectangle> guard = this.rectangleGuardians.stream()
					.filter(val -> val.getSecond().equals(this.rectanglePatches.get(patchID))).map(x -> x.getKey())
					.filter(g -> (g.getFill().equals(MainFXMLController.OK_COLOR))).findAny();
			if (guard.isPresent()) {

				guard.get().setFill(MainFXMLController.EARLY_ALERT_COLOR);
			}
		});

	}

	public void addGuardian(final int patchID) {
		Platform.runLater(() -> {

			final Rectangle guardian = new Rectangle();
			int guardiansPresentsWidth = this.listGuardiansNumbers.get(patchID).getFirst();
			int guardiansPresentsHeight = this.listGuardiansNumbers.get(patchID).getSecond();
			guardian.setFill(MainFXMLController.OK_COLOR);
			guardian.setStroke(Color.BLACK);
			guardian.setStrokeType(StrokeType.INSIDE);
			guardian.setStrokeWidth(1);
			guardian.setHeight(
					this.rectanglePatches.get(patchID).getHeight() / MainFXMLController.GUARDIANS_DIVIDER_FOR_PATCH);
			guardian.setWidth(
					this.rectanglePatches.get(patchID).getWidth() / MainFXMLController.GUARDIANS_DIVIDER_FOR_PATCH);

			guardian.setX((this.rectanglePatches.get(patchID).getX() + this.rectanglePatches.get(patchID).getWidth())
					- ((guardian.getWidth()) * guardiansPresentsWidth));
			guardian.setY((this.rectanglePatches.get(patchID).getY() + this.rectanglePatches.get(patchID).getHeight())
					- (guardian.getHeight() * guardiansPresentsHeight));

			guardiansPresentsHeight += guardiansPresentsWidth >= MainFXMLController.GUARDIANS_DIVIDER_FOR_PATCH ? 1 : 0;
			guardiansPresentsWidth = guardiansPresentsWidth < MainFXMLController.GUARDIANS_DIVIDER_FOR_PATCH
					? guardiansPresentsWidth + 1
					: 1;
			this.listGuardiansNumbers.set(patchID, new Pair<>(guardiansPresentsWidth, guardiansPresentsHeight));
			this.rectangleGuardians.add(new Pair<>(guardian, this.rectanglePatches.get(patchID)));
			this.pane2D.getChildren().add(guardian);

		});
	}

	public void removeGuardian(final int patchID) {
		Platform.runLater(() -> {

			final Optional<Pair<Rectangle, Rectangle>> opt = this.rectangleGuardians.stream().filter(pair -> {
				return pair.getSecond().equals(this.rectanglePatches.get(patchID));
			}).filter(pair -> {
				return pair.getFirst().getFill().equals(MainFXMLController.DISABLE_COLOR);
			}).findAny();

			if (opt.isPresent()) {
				this.pane2D.getChildren().remove(opt.get().getFirst());
				this.rectangleGuardians.remove(opt.get());
			}

		});
	}

	public void disableGuardian(final int patchID) {
		Platform.runLater(() -> {

			final Optional<Pair<Rectangle, Rectangle>> opt = this.rectangleGuardians.stream().filter(pair -> {
				return pair.getSecond().equals(this.rectanglePatches.get(patchID));
			}).findAny();

			if (opt.isPresent()) {
				opt.get().getFirst().setFill(MainFXMLController.DISABLE_COLOR);
			}

		});
	}

	public void enableGuardian(final int patchID) {
		Platform.runLater(() -> {

			final Optional<Pair<Rectangle, Rectangle>> opt = this.rectangleGuardians.stream().filter(pair -> {
				return pair.getSecond().equals(this.rectanglePatches.get(patchID));
			}).findAny();

			if (opt.isPresent()) {
				opt.get().getFirst().setFill(MainFXMLController.OK_COLOR);
			}

		});
	}

	public void restorePatch(final int patchID) {
		this.rectanglePatches.get(patchID).setFill(null);

		this.rectangleGuardians.stream().filter(pair -> {
			return pair.getSecond().equals(this.rectanglePatches.get(patchID));
		}).forEach(pair -> {
			pair.getFirst().setFill(MainFXMLController.OK_COLOR);
		});

	}

	public void addDashboard(final notifiable dashboard) {
		this.dashboard = dashboard;
	}

	public List<GeographicArea> getPatchesAreas() {
		final List<GeographicArea> areas = new ArrayList<>();
		double topLeftBoundX;
		double topLeftBoundY;
		double bottomRightBoundX;
		double bottomRightBoundY;
		for (int i = 0; i < this.rectanglePatches.size(); i++) {
			topLeftBoundX = this.rectanglePatches.get(i).getX();
			topLeftBoundY = this.rectanglePatches.get(i).getY();
			bottomRightBoundX = topLeftBoundX + this.rectanglePatches.get(i).getWidth();
			bottomRightBoundY = topLeftBoundY + this.rectanglePatches.get(i).getHeight();
			areas.add(new GeographicArea(new Point2D.Double(topLeftBoundX, topLeftBoundY),
					new Point2D.Double(bottomRightBoundX, bottomRightBoundY)));

		}
		return areas;
	}

	public void addAndMoveSensors(final int sensorID, final Point2D position) {
		Platform.runLater(() -> {
			System.out.println("X" + position.getX());
			System.out.println("Y" + position.getY());
			if (this.circleSensors[sensorID] != null) {
				this.circleSensors[sensorID].setCenterX(position.getX());
				this.circleSensors[sensorID].setCenterY(position.getY());
			} else {

				this.circleSensors[sensorID] = new Circle(position.getX(), position.getY(),
						MainFXMLController.SENSORS_SIZE, Color.BLUE);
				this.pane2D.getChildren().add(this.circleSensors[sensorID]);
			}
		});

	}
}
