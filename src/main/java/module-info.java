module com.mycompany.securepasswordmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;

    opens com.mycompany.securepasswordmanager to javafx.fxml;
    exports com.mycompany.securepasswordmanager;
}
