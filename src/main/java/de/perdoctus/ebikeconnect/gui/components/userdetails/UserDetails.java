package de.perdoctus.ebikeconnect.gui.components.userdetails;

import de.perdoctus.ebikeconnect.api.login.EBCAddress;

/*
 * #%L
 * ebikeconnect-gui
 * %%
 * Copyright (C) 2016 Christoph Giesche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import de.perdoctus.ebikeconnect.api.login.EBCUser;
import de.perdoctus.fx.Bundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class UserDetails extends GridPane {

    @Inject
    @Bundle("bundles/General")
    private ResourceBundle rb;

    private ObjectProperty<EBCUser> user = new SimpleObjectProperty<>();

    private int rowCount;

    public UserDetails() {
        setPadding(new Insets(20, 10, 0, 10));
        setHgap(10);
        setPrefWidth(400);

        getColumnConstraints().add(new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.LEFT, false));
        getColumnConstraints().add(new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.LEFT, true));

        user.addListener((observable, oldValue, newValue) -> {
            getChildren().clear();
            if (newValue != null) {
                fillGridWithUserDetails(newValue);
            }
        });
    }

    private void fillGridWithUserDetails(final EBCUser user) {
        addRow(rb.getString("lastname"), user.getLastName() != null ? user.getLastName() : "");
        addRow(rb.getString("firstname"), user.getFirstName() != null ? user.getFirstName() : "");
        addRow(rb.getString("gender"), user.getGender() != null ? user.getGender() : "");
        addRow(rb.getString("email-address"), user.getEmail() != null ? user.getEmail() : "");
        addRow(rb.getString("date-of-birth"), user.getDateOfBirth() != null ? user.getDateOfBirth() : "");
        addRow(rb.getString("height"), String.valueOf(user.getHeight()));
        addRow(rb.getString("weight"), String.valueOf(user.getWeight()));
        addRow(rb.getString("activity-level"), String.valueOf(user.getActivityLevel()));
        EBCAddress homeAddr = user.getHomeAddress();
        String homeAddrStr = "";
        if (homeAddr != null) {
            homeAddrStr = (homeAddr.getStreet() != null ? homeAddr.getStreet() : "") + " " + 
                          (homeAddr.getNumber() != null ? homeAddr.getNumber() : "") + ", " + 
                          (homeAddr.getZip() != null ? homeAddr.getZip() : "") + " " + 
                          (homeAddr.getCity() != null ? homeAddr.getCity() : "");
        }
        addRow(rb.getString("home-adress"), homeAddrStr);
        EBCAddress workAddr = user.getWorkAddress();
        String workAddrStr = "";
        if (workAddr != null) {
            workAddrStr = (workAddr.getStreet() != null ? workAddr.getStreet() : "") + " " + 
                          (workAddr.getNumber() != null ? workAddr.getNumber() : "") + ", " + 
                          (workAddr.getZip() != null ? workAddr.getZip() : "") + " " + 
                          (workAddr.getCity() != null ? workAddr.getCity() : "");
        }
        addRow(rb.getString("work-adress"), workAddrStr);
        addRow(rb.getString("remaining-home-changes"), String.valueOf(user.getRemainingHomeChanges()));
    }

    private void addRow(String key, String value) {
        final Label keyLabel = new Label(key);
        final Label valueLabel = new Label(value);

        addRow(rowCount, keyLabel, valueLabel);
        rowCount++;
    }

    public EBCUser getUser() {
        return user.get();
    }

    public ObjectProperty<EBCUser> userProperty() {
        return user;
    }

    public void setUser(final EBCUser user) {
        this.user.set(user);
    }
}
