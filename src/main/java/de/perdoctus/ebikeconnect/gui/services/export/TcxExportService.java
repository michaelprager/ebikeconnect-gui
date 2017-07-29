package de.perdoctus.ebikeconnect.gui.services.export;

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


import de.perdoctus.ebikeconnect.gui.models.ActivityDetails;
import de.perdoctus.ebikeconnect.gui.models.Coordinate;
import de.perdoctus.fx.Bundle;
import de.perdoctus.tcx.*;
import de.perdoctus.tcx.extension.ActivityTrackpointExtensionT;
import de.perdoctus.tcx.extension.CadenceSensorTypeT;
import javafx.concurrent.Task;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TcxExportService extends ExportService {

    @Inject
    @Bundle("bundles/General")
    private ResourceBundle rb;

    private final static ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private final static de.perdoctus.tcx.extension.ObjectFactory OBJECT_FACTORY_EXT = new de.perdoctus.tcx.extension.ObjectFactory();

    @Override
    public String getFileExtension() {
        return "tcx";
    }

    @Override
    public String getFileTypeDescription() {
        return rb.getString("tcx-file");
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {

            int totalDistance = 0;

            @Override
            protected Void call() throws Exception {

                final ApplicationT applicationT = OBJECT_FACTORY.createApplicationT();
                applicationT.setName(rb.getString("application-name") + " " + rb.getString("app-version"));

                final ActivityT activity = OBJECT_FACTORY.createActivityT();
                activity.setSport(SportT.BIKING);
                activity.setCreator(applicationT);
                activity.setId(activityDetails.get().get(0).getActivityHeader().getStartTime());

                for (final ActivityDetails activityDetail : activityDetails.get()) {
                    activity.getLap().add(createLap(activityDetail));
                }

                final ActivityListT activities = OBJECT_FACTORY.createActivityListT();
                activities.getActivity().add(activity);

                final TrainingCenterDatabaseT trainingCenterDatabaseT = OBJECT_FACTORY.createTrainingCenterDatabaseT();
                trainingCenterDatabaseT.setAuthor(applicationT);
                trainingCenterDatabaseT.setActivities(activities);

                saveGpxDocument(OBJECT_FACTORY.createTrainingCenterDatabase(trainingCenterDatabaseT));

                return null;
            }

            private void saveGpxDocument(final JAXBElement<TrainingCenterDatabaseT> tcxDocument) throws JAXBException {
                final JAXBContext jaxbContext = JAXBContext.newInstance("de.perdoctus.tcx:de.perdoctus.tcx.extension");
                final Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//                marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new GpxPrefixMapper());
                marshaller.marshal(tcxDocument, file.get());
            }

            private ActivityLapT createLap(final ActivityDetails activityDetail) {
                final LocalDateTime startTime = activityDetail.getActivityHeader().getStartTime();

                final ActivityLapT activityLapT = OBJECT_FACTORY.createActivityLapT();
                activityLapT.setStartTime(startTime);
                activityLapT.setTotalTimeSeconds(activityDetail.getActivityHeader().getDrivingTime().get(ChronoUnit.SECONDS));
                activityLapT.setCalories(activityDetail.getActivityHeader().getCalories());
                activityLapT.setDistanceMeters(activityDetail.getActivityHeader().getDistance());
                activityLapT.setTriggerMethod(TriggerMethodT.MANUAL);
                activityLapT.setIntensity(IntensityT.ACTIVE);

                final List<Short> activityDetailHeartRate = activityDetail.getHeartRate();
                final Optional<Short> heartRateAvg = average(activityDetailHeartRate);
                if (heartRateAvg.isPresent()) {
                    final HeartRateInBeatsPerMinuteT heartRate = OBJECT_FACTORY.createHeartRateInBeatsPerMinuteT();
                    heartRate.setValue(heartRateAvg.get());
                    activityLapT.setAverageHeartRateBpm(heartRate);
                }
                final Optional<Short> heartRateMax = max(activityDetailHeartRate);
                if (heartRateMax.isPresent()) {
                    final HeartRateInBeatsPerMinuteT heartRate = OBJECT_FACTORY.createHeartRateInBeatsPerMinuteT();
                    heartRate.setValue(heartRateMax.get());
                    activityLapT.setMaximumHeartRateBpm(heartRate);
                }

                final OptionalDouble speedMax = maxFloat(activityDetail.getSpeeds());
                if (speedMax.isPresent()) {
                    activityLapT.setMaximumSpeed(speedMax.getAsDouble());
                }

                final Optional<Short> cadenceAvg = average(activityDetail.getCadences());
                if (cadenceAvg.isPresent()) {
                    activityLapT.setCadence(cadenceAvg.get());
                }

                final TrackT track = OBJECT_FACTORY.createTrackT();
                final int tpCount = activityDetail.getTrackPoints().size();
                for (int i = 0; i < tpCount; i++) {
                    final Coordinate coordinate = activityDetail.getTrackPoints().get(i);
                    final Short heartRate = getValueMatchingValueForTrackpoint(i, tpCount, activityDetail.getHeartRate());
                    final Float speed = getValueMatchingValueForTrackpoint(i, tpCount, activityDetail.getSpeeds());
                    final Short cadence = getValueMatchingValueForTrackpoint(i, tpCount, activityDetail.getCadences());
                    final Float altitude = getValueMatchingValueForTrackpoint(i, tpCount, activityDetail.getAltitudes());
                    final Float driverTorque = getValueMatchingValueForTrackpoint(i, tpCount, activityDetail.getDriverTorques());
                    final Short gaineddistance = getValueMatchingValueForTrackpoint(i, tpCount, activityDetail.getGainedDistances());
                    final LocalDateTime trackpointTime = startTime.plus(i, ChronoUnit.SECONDS);

                    if (gaineddistance != null)
                        totalDistance += gaineddistance;
                    track.getTrackpoint().add(createTrackpoint(trackpointTime, coordinate, heartRate, speed, cadence, altitude, driverTorque, totalDistance));
                }
                activityLapT.getTrack().add(track);

                return activityLapT;
            }

            private TrackpointT createTrackpoint(LocalDateTime trackpointTime, Coordinate coordinate, Short heartRate, Float speedKmh, Short cadence, Float altitude, Float driverTorque, int lapDistance) {
                final TrackpointT trackpoint = OBJECT_FACTORY.createTrackpointT();
                trackpoint.setAltitudeMeters(altitude != null ? Double.valueOf(altitude) : null);
                trackpoint.setCadence(cadence);
                trackpoint.setDistanceMeters((double) lapDistance);

                if (heartRate != null) {
                    final HeartRateInBeatsPerMinuteT heartRateInBeatsPerMinuteT = OBJECT_FACTORY.createHeartRateInBeatsPerMinuteT();
                    heartRateInBeatsPerMinuteT.setValue(heartRate);
                    trackpoint.setHeartRateBpm(heartRateInBeatsPerMinuteT);
                }

                trackpoint.setTime(trackpointTime);

                if (coordinate.isValid()) {
                    final PositionT positionT = OBJECT_FACTORY.createPositionT();
                    positionT.setLatitudeDegrees(coordinate.getLat());
                    positionT.setLongitudeDegrees(coordinate.getLng());
                    trackpoint.setPosition(positionT);
                }

                final ExtensionsT extensions = OBJECT_FACTORY.createExtensionsT();
                final ActivityTrackpointExtensionT trackpointExtension = OBJECT_FACTORY_EXT.createActivityTrackpointExtensionT();
                trackpointExtension.setCadenceSensor(CadenceSensorTypeT.BIKE);
                if (speedKmh != null) {
                    double speedMeterPerSecond = speedKmh / 3.6;
                    trackpointExtension.setSpeed(speedMeterPerSecond);
                }
                if (driverTorque != null && cadence != null) {
                    final double power = 2 * Math.PI * cadence / 60 * driverTorque;
                    trackpointExtension.setWatts((int) power);
                }

                extensions.getAny().add(OBJECT_FACTORY_EXT.createTPX(trackpointExtension));
                trackpoint.setExtensions(extensions);

                return trackpoint;
            }

            private OptionalDouble maxFloat(List<Float> speeds) {
                return speeds.parallelStream().filter(value -> value != null).mapToDouble(Float::doubleValue).max();
            }

            private Optional<Short> average(List<Short> values) {
                final OptionalDouble average = values.parallelStream().filter(value -> value != null).mapToInt(Short::intValue).average();

                if (average.isPresent()) {
                    return Optional.of((short) average.getAsDouble());
                } else {
                    return Optional.empty();
                }
            }

            private Optional<Short> max(List<Short> values) {
                final OptionalInt max = values.parallelStream().filter(value -> value != null).mapToInt(Short::intValue).max();

                if (max.isPresent()) {
                    return Optional.of((short) max.getAsInt());
                } else {
                    return Optional.empty();
                }
            }

            private <A> A getValueMatchingValueForTrackpoint(int trackPointNr, int trackPointCount, List<A> values) {
                if (values == null) return null;
                final float valuesPerTrackpoint = values.size() / (float) trackPointCount;
                int matchingValueIndex = getMatchingIndex(trackPointNr, valuesPerTrackpoint);
                return values.get(matchingValueIndex);
            }

            private int getMatchingIndex(int trackPointNr, float valuesPerTrackpoint) {
                float nearestHeightInfo = trackPointNr * valuesPerTrackpoint;
                return (int) Math.floor(nearestHeightInfo);
            }
        };
    }
}
