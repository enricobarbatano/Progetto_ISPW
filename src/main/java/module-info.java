module com.ispw.progetto {
  requires javafx.controls;
  requires javafx.fxml;
  requires java.sql;
  requires java.naming;
  requires java.logging;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;
  exports com.ispw.view.gui to javafx.graphics;
  opens com.ispw.view.gui.fxml to javafx.fxml;
  exports com.ispw;
}
