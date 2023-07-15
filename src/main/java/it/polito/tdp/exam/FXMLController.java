/**
 * Sample Skeleton for 'Scene.fxml' Controller Class
 */

package it.polito.tdp.exam;

import java.net.URL;
import java.util.ResourceBundle;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.exam.model.Archi;
import it.polito.tdp.exam.model.Model;
import it.polito.tdp.exam.model.YearPlayers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FXMLController {

    private Model model;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="btnCreaGrafo"
    private Button btnCreaGrafo; // Value injected by FXMLLoader

    @FXML // fx:id="btnDettagli"
    private Button btnDettagli; // Value injected by FXMLLoader

    @FXML // fx:id="btnSimula"
    private Button btnSimula; // Value injected by FXMLLoader

    @FXML // fx:id="cmbAnno"
    private ComboBox<YearPlayers> cmbAnno; // Value injected by FXMLLoader

    @FXML // fx:id="cmbSquadra"
    private ComboBox<String> cmbSquadra; // Value injected by FXMLLoader

    @FXML // fx:id="txtResult"
    private TextArea txtResult; // Value injected by FXMLLoader

    @FXML // fx:id="txtTifosi"
    private TextField txtTifosi; // Value injected by FXMLLoader

    @FXML
    void handleCreaGrafo(ActionEvent event) {
    	
    	if (this.cmbSquadra.getValue() == null) {
    		txtResult.setText("Scegli una squadra\n");
    		return;
    	}

    	SimpleWeightedGraph<YearPlayers,DefaultWeightedEdge> graph = model.creaGrafo(this.cmbSquadra.getValue());
    	
    	txtResult.setText("Grafo creato con " + graph.vertexSet().size() + " vertici e " + graph.edgeSet().size() + " archi.\n\n");

    	this.cmbAnno.getItems().clear();
    	this.cmbAnno.getItems().addAll(graph.vertexSet());
    	
    	this.btnDettagli.setDisable(false);
    	this.btnSimula.setDisable(false);
    	
    	
    }

    @FXML
    void handleDettagli(ActionEvent event) {
    	
    	if (this.cmbAnno.getValue() == null) {
    		txtResult.setText("Scegli un anno\n");
    		return;
    	}
    	
    	txtResult.appendText("Dettagli per l'anno scelto:\n");
    	
    	for (Archi a : model.getDettagli(cmbAnno.getValue().getYear()))
    		txtResult.appendText(cmbAnno.getValue().toString() + " <-> anno: " + a.getAnno().toString() + "; peso: " + a.getPeso() + '\n');
    	

    }

    @FXML
    void handleSimula(ActionEvent event) {

    	txtResult.setText("Num tifosi persi: " + model.run(this.cmbAnno.getValue().getYear(),Integer.parseInt(txtTifosi.getText())) + "\n\n");
    	
    	for (String player : model.getGiocatoriTifosi().keySet())
    		txtResult.appendText(player + " -> num tifosi: " + model.getGiocatoriTifosi().get(player) + '\n');
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert btnCreaGrafo != null : "fx:id=\"btnCreaGrafo\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnDettagli != null : "fx:id=\"btnDettagli\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnSimula != null : "fx:id=\"btnSimula\" was not injected: check your FXML file 'Scene.fxml'.";
        assert cmbAnno != null : "fx:id=\"cmbAnno\" was not injected: check your FXML file 'Scene.fxml'.";
        assert cmbSquadra != null : "fx:id=\"cmbSquadra\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtTifosi != null : "fx:id=\"txtTifosi\" was not injected: check your FXML file 'Scene.fxml'.";

    }

    public void setModel(Model model) {
        this.model = model;
        this.btnDettagli.setDisable(true);
    	this.btnSimula.setDisable(true);
        
        this.cmbSquadra.getItems().addAll(model.readTeams());
    }

}
