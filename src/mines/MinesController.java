package mines;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class MinesController {
	@FXML
	private AnchorPane root;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Pane gamePaneBack;

	@FXML
	private TextField txtWidth;

	@FXML
	private TextField txtHeight;

	@FXML
	private TextField txtMines;

	@FXML
	private Label lblWidth;

	@FXML
	private Label lblWidth11;

	@FXML
	private Label lblWidth111;

	@FXML
	private Button btnReset;

	@FXML
	private Label txtStatus;

	@FXML
	private Label txtTimer;

	@FXML
	private Label txtNumOfFlags;

	@FXML
	private Button exitBtn;

	@FXML
	private ImageView sandClockOn;

	@FXML
	private ImageView sandClockOff;

	@FXML
	void initialize() {
		assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert exitBtn != null : "fx:id=\"exitBtn\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert txtWidth != null : "fx:id=\"txtWidth\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert txtHeight != null : "fx:id=\"txtHeight\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert txtMines != null : "fx:id=\"txtMines\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert lblWidth != null : "fx:id=\"lblWidth\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert lblWidth11 != null : "fx:id=\"lblWidth11\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert lblWidth111 != null : "fx:id=\"lblWidth111\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert btnReset != null : "fx:id=\"btnReset\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert txtStatus != null : "fx:id=\"txtStatus\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert txtTimer != null : "fx:id=\"txtTimer\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert txtNumOfFlags != null : "fx:id=\"txtNumOfFlags\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert gamePaneBack != null : "fx:id=\"gamePaneBack\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert sandClockOn != null : "fx:id=\"sandClockOn\" was not injected: check your FXML file 'fxmlMines.fxml'.";
		assert sandClockOff != null : "fx:id=\"sandClockOff\" was not injected: check your FXML file 'fxmlMines.fxml'.";
	}

	public ImageView getSandClockOn() {
		return sandClockOn;
	}

	public ImageView getSandClockOff() {
		return sandClockOff;
	}

	public AnchorPane getRoot() {
		return root;
	}

	public ResourceBundle getResources() {
		return resources;
	}

	public URL getLocation() {
		return location;
	}

	public Pane getGamePaneBack() {
		return gamePaneBack;
	}

	public TextField getTxtWidth() {
		return txtWidth;
	}

	public TextField getTxtHeight() {
		return txtHeight;
	}

	public TextField getTxtMines() {
		return txtMines;
	}

	public Label getLblWidth() {
		return lblWidth;
	}

	public Label getLblWidth11() {
		return lblWidth11;
	}

	public Label getLblWidth111() {
		return lblWidth111;
	}

	public Button getBtnReset() {
		return btnReset;
	}

	public Label getTxtStatus() {
		return txtStatus;
	}

	public Label getTxtTimer() {
		return txtTimer;
	}

	public Label getTxtNumOfFlags() {
		return txtNumOfFlags;
	}

	public Button getExitBtn() {
		return exitBtn;
	}

	public void openLinkedIn() throws IOException {
		String s = "https://www.linkedin.com/in/DanielGabbayLI/";
		Desktop desktop = Desktop.getDesktop();
		desktop.browse(URI.create(s));
	}

}
