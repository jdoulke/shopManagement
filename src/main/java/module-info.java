module com.onlinestore.jdoulke.onlinestorefx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires com.oracle.database.jdbc;
    requires java.desktop;

    opens com.onlinestore.jdoulke.onlinestorefx to javafx.fxml;
    exports com.onlinestore.jdoulke.onlinestorefx;
    exports com.onlinestore.jdoulke.onlinestorefx.database;
    exports com.onlinestore.jdoulke.onlinestorefx.controllers;
    opens com.onlinestore.jdoulke.onlinestorefx.controllers to javafx.fxml;
    exports com.onlinestore.jdoulke.onlinestorefx.entities;
    opens com.onlinestore.jdoulke.onlinestorefx.entities to javafx.fxml;
    exports com.onlinestore.jdoulke.onlinestorefx.controllers.users;
    opens com.onlinestore.jdoulke.onlinestorefx.controllers.users to javafx.fxml;
    exports com.onlinestore.jdoulke.onlinestorefx.controllers.customers;
    opens  com.onlinestore.jdoulke.onlinestorefx.controllers.customers to javafx.fxml;
    exports com.onlinestore.jdoulke.onlinestorefx.controllers.orders;
    opens com.onlinestore.jdoulke.onlinestorefx.controllers.orders to javafx.fxml;
    exports com.onlinestore.jdoulke.onlinestorefx.controllers.products;
    opens  com.onlinestore.jdoulke.onlinestorefx.controllers.products to javafx.fxml;
    exports com.onlinestore.jdoulke.onlinestorefx.controllers.sales;
    opens com.onlinestore.jdoulke.onlinestorefx.controllers.sales to javafx.fxml;

}