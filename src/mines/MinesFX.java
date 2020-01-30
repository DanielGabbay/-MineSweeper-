/*
 * Created by: Daniel Gabbay
 * Date: 29 January 2020
 */
package mines;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import java.awt.*;

public class MinesFX extends Application {
	// defines
	private static final int WINDOW_WIDTH = 1280;
	private static final int WINDOW_HEIGHT = 800;
	private static final int GAME_BOARD_WIDTH = 850;
	private static final int GAME_BOARD_HEIGHT = 650;
	// timer variables:
	private static Integer secondsPassed;
	private int pauseTime;
	private static Timer timer;
	// stage variables and defines:
	private Scene scene;
	private AnchorPane root;
	// buttons for alert messages:
	ButtonType newGameButton = new ButtonType("New game", ButtonBar.ButtonData.OK_DONE);
	ButtonType exitGameButton = new ButtonType("Quit", ButtonBar.ButtonData.CANCEL_CLOSE);
	private MinesController minesController;
	// board game variables:
	private int numButtonsInRow;
	private int numButtonsInColumn;
	private int minesNumber;
	private int flagsNumber;
	// controller connector variables:
	private Pane gamePaneBack;
	private TextField txtWidth;
	private TextField txtHeight;
	private TextField txtMines;
	private Button btnReset;
	private Button exitBtn;
	private Label txtStatus;
	private Label txtTimer;
	private Label txtNumOfFlags;
	private ImageView sandClockOn;
	private ImageView sandClockOff;
	// game board grid
	private GridPane gameGrid;
	// mines logical class connectors:
	private Mines mines;
	private Button[][] buttonsBoard;
	private double buttonWidth;
	private double buttonHeight;
	//
	private boolean ON = true;

	@Override
	public void start(Stage stage) throws IOException {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("fxmlMines.fxml"));
			root = loader.load();
			minesController = loader.getController();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/* Pointers for controller data */
		gamePaneBack = minesController.getGamePaneBack();
		btnReset = minesController.getBtnReset();
		txtHeight = minesController.getTxtHeight();
		txtWidth = minesController.getTxtWidth();
		txtMines = minesController.getTxtMines();
		exitBtn = minesController.getExitBtn();
		txtTimer = minesController.getTxtTimer();
		txtNumOfFlags = minesController.getTxtNumOfFlags();
		txtStatus = minesController.getTxtStatus();
		sandClockOn = minesController.getSandClockOn();
		sandClockOff = minesController.getSandClockOff();
		//
		resetTimer();
		/*
		 * set the exit button triggers: - close the game window - stop the timer
		 */
		exitBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				stopTimer();
				// close the window
				((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
				System.out.println("EXIT MineSweeper!"); // command indicator (for a programmers)
			}
		});
		/*
		 * set the X (exit of windows) triggers: an alternative to closing the screen
		 * via the button "X" also: close the window & stop the timer
		 */
		stage.setOnCloseRequest(e -> {
			System.out.println("X button pressed!"); // for a monitoring
			stopTimer();
			Platform.exit();
			System.exit(0);
		});
		/*
		 * set the reset button triggers: - set/update the game board dimension - reset
		 * the timer.
		 */
		btnReset.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (txtWidth.getText().equals("") || txtHeight.getText().equals("") || txtMines.getText().equals("")) {
					// if the values of width/height/mines are empty
					createPopUpMessage(0);
				} else if (!isNumeric(txtHeight.getText()) || !isNumeric(txtWidth.getText())
						|| !isNumeric(txtMines.getText())) {
					// elif types invalid characters
					createPopUpMessage(1);
					ClearErrTextFields();
				} else {
					// converts the board width & height & number of mines - from string to integer:
					numButtonsInRow = Integer.parseInt(txtWidth.getText());
					numButtonsInColumn = Integer.parseInt(txtHeight.getText());
					minesNumber = Integer.parseInt(txtMines.getText());
					// if there is negative number / zero => error
					int[] arr = { numButtonsInRow, numButtonsInColumn, minesNumber };
					if (thersNegOrZero(arr)) {
						createPopUpMessage(4);
						ClearErrTextFields();
					} else if (minesNumber >= (numButtonsInColumn * numButtonsInRow)) {
						createPopUpMessage(5);
						txtMines.clear();
					} else {
						// else - start a new game
						createNewGame();
					}
				}
			}
		});
		txtStatus.setText("Insert Dimensions ...");
		setClkGif(!ON);
		scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
		stage.getIcons().add(new Image("/images/mine.png"));
		stage.setScene(scene);
		stage.show();
	}

	// create new game board based on height/weight/numOfMines that the user typed
	private void createNewGame() {
		txtStatus.setText("The game is running...");
		if (gameGrid != null)
			gameGrid.getChildren().clear();
		stopTimer();
		startTimer();
		flagsNumber = 0; // reset the number of flags on the board
		// sets the first game board settings:
		gameGrid = new GridPane();
		gameGrid.setPadding(new Insets(0, 12, 0, 1.2));
		gameGrid.setHgap(0);
		gameGrid.setVgap(0);
		gameGrid.setAlignment(Pos.CENTER);
		// converts the board width & height & number of mines - from string to integer:
		numButtonsInRow = Integer.parseInt(txtWidth.getText());
		numButtonsInColumn = Integer.parseInt(txtHeight.getText());
		minesNumber = Integer.parseInt(txtMines.getText());
		// connection between the visual and the logic of the game:
		mines = new Mines(numButtonsInColumn, numButtonsInRow, minesNumber);
		mines.setShowAll(true);
		mines.setShowAll(false);
		// set the board's buttons
		buttonsBoard = new Button[numButtonsInRow][numButtonsInColumn];
		// calculates the buttons dimensions(width/height):
		buttonHeight = GAME_BOARD_HEIGHT / numButtonsInColumn;
		buttonWidth = GAME_BOARD_WIDTH / numButtonsInRow;
		//
		for (int i = 0; i < numButtonsInRow; i++) {
			for (int j = 0; j < numButtonsInColumn; j++) {
				// sets the button & his ID
				buttonsBoard[i][j] = new Button();
				buttonsBoard[i][j].setId(i + "," + j);
				buttonsBoard[i][j].setPrefSize(GAME_BOARD_WIDTH / numButtonsInColumn,
						GAME_BOARD_HEIGHT / numButtonsInRow); // sets the button dimensions
				gameGrid.add(buttonsBoard[i][j], j, i); // adds the button to the game board
				setButtonTriggers(i, j);
			}
		}
		gamePaneBack.getChildren().addAll(gameGrid);
	}

	// A Timer implementation function that running in the background on a thread:
	private void startTimer() {
		setClkGif(ON);
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						secondsPassed++;
						txtTimer.setText(secondsPassed.toString());
						pauseTime = secondsPassed;
					}
				});
			}
		}, 1000, 1000);
	}

	// another function that stop the Timer that running in background on a thread
	// (kill the timer thread):
	private void stopTimer() {
		setClkGif(!ON);
		if (timer != null) {
			pauseTime = secondsPassed;
			timer.cancel();
		}
		resetTimer();
	}

	// another function that reset the timer values on the visual board (labels) and
	// the code variable (seconds):
	private void resetTimer() {
		secondsPassed = 0;
		txtTimer.setText("0");
	}

	// Function that checks for input string if it contains characters that are not
	// numbers:
	private boolean isNumeric(String str) {
		if (str == null || str.length() == 0) {
			return false;
		}
		try {
			Double.parseDouble(str);
			return true;

		} catch (NumberFormatException e) {
			return false;
		}
	}

	// function that clears all text fields with error text inputed:
	private void ClearErrTextFields() {
		if (!isNumeric(txtHeight.getText()) || thersNegOrZero(new int[] { numButtonsInColumn }))
			txtHeight.clear();
		if (!isNumeric(txtWidth.getText()) || thersNegOrZero(new int[] { numButtonsInRow }))
			txtWidth.clear();
		if (!isNumeric(txtMines.getText()) || thersNegOrZero(new int[] { minesNumber }))
			txtMines.clear();
	}

	// function to create a custom pop-up message
	private void createPopUpMessage(int flag) {
		/*
		 * flag=0: unfilled all all game board data| flag=1: type invalid characters|
		 * flag=2: winner alert| flag=3: loser alert| flag=4: negative/zero typed|
		 * flag=5: minesNumber grater then (numButtonsInColumn * numButtonsInRow)
		 */
		Alert alert = null;
		ImageView imgV;

		switch (flag) {
		case 0:
			alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Input Error!");
			alert.setHeaderText("Input Error!");
			alert.setContentText("Error! You must enter all game board data:\n" + "height, width, number of mines");
			break;
		case 1:
			alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Input Error!");
			alert.setHeaderText("Input Error!");
			alert.setContentText("Input Error! You can only fill numbers and no other characters.\n"
					+ "Try re-typing the invalid fields!");
			break;
		case 2:// winner
			alert = new Alert(Alert.AlertType.WARNING, "", newGameButton, exitGameButton);
			imgV = new ImageView("/images/winner.png");
			imgV.setFitHeight(48);
			imgV.setFitWidth(48);
			alert.setTitle("Winner!");
			alert.setHeaderText("Congratulations!");
			alert.setContentText("You found all the bombs in " + pauseTime + " seconds.");
			alert.getDialogPane().setGraphic(imgV);
			stopTimer();
			playSound("win");
			break;
		case 3:// loser
			alert = new Alert(Alert.AlertType.WARNING, "", newGameButton, exitGameButton);
			imgV = new ImageView("/images/loser.png");
			imgV.setFitHeight(48);
			imgV.setFitWidth(48);
			alert.setTitle("Game Over!");
			alert.setHeaderText("Bomb Exploded!");
			alert.setContentText(
					"Oh no! You clicked on a bomb and caused all the bombs to explode! Better luck next time.");
			alert.getDialogPane().setGraphic(imgV);
			stopTimer();
			playSound("explosion");
			break;
		case 4:// negative or zero number
			alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Input Error!");
			alert.setHeaderText("Input Error!");
			alert.setContentText(
					"Input Error! You can only fill negative numbers or zero\n" + "Try re-typing the invalid fields!");
			break;
		case 5:// minesNumber >= (numButtonsInColumn * numButtonsInRow
			alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Input Error!");
			alert.setHeaderText("Input Error!");
			alert.setContentText(
					"The number of mines you place is greater than the dimensions\n of the game board! Type the number of mines again");
			break;
		}
		if (flag == 2 || flag == 3) {
			Optional<ButtonType> result = alert.showAndWait();

			if (result.get().getText().equals("New game")) {
				stopTimer();
				createNewGame();
			} else if (result.get().getText().equals("Quit")) {
				stopTimer();
				Platform.exit();
				System.exit(0);
			}
		} else {
			playSound("beep");
			alert.showAndWait();
			if (flag == 5)
				txtStatus.setText("Insert the number of mines again...");
			txtStatus.setText("Insert dimensions again...");
		}
	}

	// sets the triggers for the game board buttons
	private void setButtonTriggers(int i, int j) {
		// sets the clicks on the button
		buttonsBoard[i][j].setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				playSound("click");
				MouseButton btnMouseClick = mouseEvent.getButton();
				//
				if (btnMouseClick == MouseButton.PRIMARY) {
					// LEFT CLICK - regular click
					if (mines.open(i, j)) {
						setButtonsOpen();
						if (mines.isDone()) {// if loser
							stopTimer();
							txtStatus.setText("Timer stop in:\n" + pauseTime + " seconds");
							createPopUpMessage(2);
						}
					} else { // if winner
						stopTimer();
						txtStatus.setText("Timer stop in:\n" + pauseTime + " seconds");
						openAllMines();
						createPopUpMessage(3);
					}
				}
				if (btnMouseClick == MouseButton.SECONDARY) {
					// RIGHT CLICK - to toggle flags.
					toggleFlag(i, j);
				}
			}
		});
	}

	// toggle a flag on the game board
	public void toggleFlag(int i, int j) {
		mines.toggleFlag(i, j);
		updateNumOfFlags(i, j);
		ImageView imgV;
		if (mines.getBoard()[i][j].isFlag()) {
			imgV = new ImageView("/images/flag.png");
			imgV.setFitHeight(buttonHeight / 2);
			imgV.setFitWidth(buttonWidth / 2.5);
			buttonsBoard[i][j].setGraphic(imgV);
		} else {
			buttonsBoard[i][j].setGraphic(null);
		}
	}

	// updates the currently flags on the board
	private void updateNumOfFlags(int i, int j) {
		if (mines.getBoard()[i][j].isFlag())
			flagsNumber++;
		else
			flagsNumber--;
		txtNumOfFlags.setText(Integer.toString(flagsNumber));
	}

	// play alerts/messages audio
	private void playSound(String type) {
		if (type.equals("beep")) {
			Toolkit.getDefaultToolkit().beep();
			return;
		} else {
			Runnable task = () -> {
				Platform.runLater(() -> {
					AudioClip winSound = new AudioClip(getClass().getResource("/sound/" + type + ".wav").toString());
					winSound.play();
				});
			};
			Thread audioThread = new Thread(task);
			audioThread.setDaemon(true);
			audioThread.start();
		}
	}

	// sets the graphic of the buttons
	private void setButtonsOpen() {
		for (int i = 0; i < numButtonsInRow; i++) {
			for (int j = 0; j < numButtonsInColumn; j++) {
				Mines.Point currentPoint = mines.getBoard()[i][j];
				if (currentPoint.isOpen()) {
					buttonsBoard[i][j].setText(currentPoint.toString());
					switch (currentPoint.getCountOfMinesArround()) {
					case 0: {
						buttonsBoard[i][j].setStyle("" + "-fx-background-color:blue; -fx-font-size: 16pt; ");
					}
						break;
					case 1: {
						buttonsBoard[i][j].setStyle("" + "-fx-color:#8ACAFE; -fx-font-size: 16pt; ");
					}
						break;
					case 2: {
						buttonsBoard[i][j].setStyle("" + "-fx-color:#00ff00; -fx-font-size: 16pt; ");
					}
						break;
					case 3: {
						buttonsBoard[i][j].setStyle("" + "-fx-color:#2e8b57; -fx-font-size: 16pt; ");
					}
						break;
					case 4: {
						buttonsBoard[i][j].setStyle("" + "-fx-color:#bd3b3b; -fx-font-size: 16pt; ");
					}
						break;
					case 5: {
						buttonsBoard[i][j].setStyle("" + "-fx-color:#377fc3; -fx-font-size: 16pt; ");
					}
						break;
					case 6: {
						buttonsBoard[i][j].setStyle("" + "-fx-color:#55ff55; -fx-font-size: 16pt; ");
					}
						break;
					case 7: {
						buttonsBoard[i][j].setStyle("" + "-fx-color:#ffbf23; -fx-font-size: 16pt; ");
					}
						break;
					case 8: {
						buttonsBoard[i][j].setStyle("" + "-fx-color:#ccccff; -fx-font-size: 16pt; ");
					}
						break;
					}
					buttonsBoard[i][j].setDisable(true);
				}
			}
		}
	}

	private void openAllMines() {
		for (int i = 0; i < numButtonsInRow; i++) {
			for (int j = 0; j < numButtonsInColumn; j++) {
				Mines.Point currentPoint = mines.getBoard()[i][j];
				if (currentPoint.isMine()) {
					ImageView imgV1 = new ImageView("/images/mine.png");
					imgV1.setFitHeight(buttonHeight / 2);
					imgV1.setFitWidth(buttonWidth / 2.5);
					buttonsBoard[i][j].setGraphic(imgV1);
				}
			}
		}
	}

	// set ON/OFF the clock gif
	private void setClkGif(boolean onOff) {
		if (onOff) {
			sandClockOn.setVisible(onOff);
			sandClockOff.setVisible(!onOff);
		} else {
			sandClockOn.setVisible(onOff);
			sandClockOff.setVisible(!onOff);
		}
	}

	// function that checks (returns true) if there is number/s in the integers
	// array that they're negative or equal to 0
	private boolean thersNegOrZero(int num[]) {
		for (int i : num) {
			if (i <= 0)
				return true;
		}
		return false;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
