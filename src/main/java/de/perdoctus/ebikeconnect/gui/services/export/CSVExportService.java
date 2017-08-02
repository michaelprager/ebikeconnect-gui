package de.perdoctus.ebikeconnect.gui.services.export;

/*
 * #%L
 * ebikeconnect-gui
 * %%
 * Copyright (C) 2016 - 2017 Christoph Giesche
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

import static java.util.stream.Collectors.toList;

import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import de.perdoctus.ebikeconnect.gui.models.ActivityDetails;
import de.perdoctus.ebikeconnect.gui.models.ActivityHeader;
import de.perdoctus.ebikeconnect.gui.models.ActivityHeaderGroup;
import de.perdoctus.ebikeconnect.gui.util.DurationFormatter;
import de.perdoctus.fx.Bundle;
import javafx.concurrent.Task;

public class CSVExportService extends ExportService {

    @Inject
    @Bundle("bundles/General")
    private ResourceBundle rb;

    
    @Override
    public String getFileExtension() {
        return "csv";
    }

    @Override
    public String getFileTypeDescription() {
        return rb.getString("csv-file");
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (activityHeaderGroups.isNotNull().and(file.isNotNull()).get()) {
                    FileWriter writer = new FileWriter(file.get());
                    final String delimiter = ";";
                    final String lineEnding = "\r\n";
                    final List<String> headers = Arrays.asList("Id", "StartTime", "EndTime", "DrivingTime", "OperationTime", "Distance", "Calories", "ActivityType");
                    writer.write(String.join(delimiter, headers.stream().map(elem -> elem != null ? "\"" + elem.replace("\"", "\"\"") + "\"" : "").collect(toList())) + lineEnding);
                    for (ActivityHeaderGroup group : activityHeaderGroups.get()) {
                        for (ActivityHeader head : group.getActivityHeaders()) {
                            List<String> list = new ArrayList<>();
                            list.add(String.valueOf(head.getActivityId()));
                            list.add(head.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                            list.add(head.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                            list.add(DurationFormatter.formatHhMmSs(head.getDrivingTime()));
                            list.add(DurationFormatter.formatHhMmSs(head.getOperationTime()));
                            list.add(String.valueOf(head.getDistance()));
                            list.add(String.valueOf(head.getCalories()));
                            list.add(String.valueOf(head.getActivityType()));
                            writer.write(String.join(delimiter, list.stream().map(elem -> elem != null ? "\"" + elem.replace("\"", "\"\"") + "\"" : "").collect(toList())) + lineEnding);
                        }
                    }
                    writer.flush();
                    writer.close();
                } else {
                    throw new IllegalArgumentException("No activityDetails given.");
                }
                return null;
            }
        };
    }

}
