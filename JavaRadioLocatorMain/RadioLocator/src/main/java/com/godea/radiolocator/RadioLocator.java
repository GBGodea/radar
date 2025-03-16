package com.godea.radiolocator;


import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import com.fazecast.jSerialComm.SerialPort;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RadioLocator extends Application {
    private Label servoAngleLab;
    private Label radarPositionLab;
    private String servoAngle;

    @Override
    public void start(Stage stage) throws IOException {
        // (diametr - height/width) / 2 + radius
        Circle circle = new Circle(275.0, 275.0, 250.0);
        Group group = new Group();
        group.getChildren().add(circle);

        servoAngleLab = new Label("Servo angle: --");
        radarPositionLab = new Label("Radar position: --");
        radarPositionLab.setLayoutY(30);
        group.getChildren().add(servoAngleLab);
        group.getChildren().add(radarPositionLab);

        for (int i = 1; i <= 10; i++) {
            group.getChildren().add(drawCircleMesh(25 * i));
        }

        for (int i = 1; i <= 10; i++) {
            Label rightLabel = new Label(String.valueOf(i * 10));
            // Беру слегка увеличеный радиус круга и складываю с i * на 1 / 10 радиуса круга
            rightLabel.setLayoutX(270 + (i * 25));
            rightLabel.setLayoutY(250.0);
            rightLabel.setTextFill(Color.BLUEVIOLET);

            Label leftLabel = new Label(String.valueOf(i * 10));
            // Беру слегка увеличеный радиус круга и складываю с i * на 1 / 10 радиуса круга
            leftLabel.setLayoutX(270 - (i * 25));
            leftLabel.setLayoutY(250.0);
            leftLabel.setTextFill(Color.BLUEVIOLET);

            group.getChildren().add(rightLabel);
            group.getChildren().add(leftLabel);
        }


        Line line = new Line();
        line.setStroke(Color.GREEN);
        line.setStrokeWidth(5.0);
        line.setStartX(275.0);
        line.setStartY(275.0);
        group.getChildren().add(line);

        Scene scene = new Scene(group, 550, 550);
        scene.setFill(Color.GREEN);
        stage.setTitle("Radar");
        stage.setResizable(false);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.show();

        // Открываю порт - COM порт нужно изменить на свой
        SerialPort serialPort = SerialPort.getCommPort("COM6");
        serialPort.setBaudRate(115200);
        serialPort.openPort();

        if (!serialPort.isOpen()) {
            throw new RuntimeException("Port can't be opened, check arduino IDE, and close the serial monitor");
        }

        // Обязательно открыть новый поток, иначе графическая часть приложения встанет и не будет работать!
        new Thread(() -> {
            try {
                while (true) {
                    while (serialPort.bytesAvailable() > 0) {
                        byte[] buffer = new byte[serialPort.bytesAvailable()];
                        //Эта строка не используется, но она нужна, иначе данные будут выводиться некорректно
                        serialPort.readBytes(buffer, buffer.length);

                        String str = new String(buffer);
                        if (!str.isEmpty()) {
                            str = str.trim();
                            if (str.startsWith("Servo:")) {
                                servoAngle = String.valueOf(str.substring(7).split("\n")[0]);
                                System.out.println("servo: " + servoAngle);
                                Platform.runLater(() -> servoAngleLab.setText("Servo angle: " + servoAngle));
                                Platform.runLater(() -> {
                                    if (servoAngle != null) {
                                        double angleInRadians = Math.toRadians(Double.parseDouble(servoAngle));
//                                        double endX = circle.getCenterX() - circle.getRadius() * Math.cos(angleInRadians);
//                                        double endY = circle.getCenterX() - circle.getRadius() * Math.sin(angleInRadians);
                                        double endX = circle.getCenterX() + circle.getRadius() * Math.cos(angleInRadians);
                                        double endY = circle.getCenterY() - circle.getRadius() * Math.sin(angleInRadians);

                                        line.setEndX(endX);
                                        line.setEndY(endY);
                                    }
                                });
                            } else if (str.startsWith("Radar:")) {
                                String radarPosition = String.valueOf(str.substring(7).split("\n")[0]);
//                                System.out.println("radar: " + radarPosition);
                                Platform.runLater(() -> radarPositionLab.setText("Radar position: " + radarPosition));
                                Platform.runLater(() -> {
                                    if (servoAngle != null && radarPosition != null) {
                                        try {
                                            double maxDistance = 100.0;
                                            short radarPositionVal = Short.parseShort(radarPosition);
                                            double lineLenght = circle.getRadius() * (radarPositionVal / maxDistance);
                                            double angleInRadians = Math.toRadians(Double.parseDouble(servoAngle));
                                            double startX = circle.getCenterX() + lineLenght * Math.cos(angleInRadians);
                                            double startY = circle.getCenterY() - lineLenght * Math.sin(angleInRadians);
                                            double endX = circle.getCenterX() + circle.getRadius() * Math.cos(angleInRadians);
                                            double endY = circle.getCenterY() - circle.getRadius() * Math.sin(angleInRadians);

                                            if(radarPositionVal <= maxDistance) {
                                                List<Line> lines = new ArrayList<>();
                                                Line warningLine = new Line();
                                                warningLine.setOpacity(0.7);
                                                warningLine.setStroke(Color.RED);
                                                warningLine.setEndX(endX);
                                                warningLine.setEndY(endY);
                                                warningLine.setStrokeWidth(5.0);
                                                warningLine.setStartX(startX);
                                                warningLine.setStartY(startY);

                                                lines.add(warningLine);

                                                group.getChildren().add(warningLine);

                                                FadeTransition fadeTransition = new FadeTransition(Duration.seconds(5), warningLine);
                                                fadeTransition.setFromValue(0.7);
                                                fadeTransition.setToValue(0.0);
                                                fadeTransition.setOnFinished(event -> {
                                                    group.getChildren().remove(warningLine);
                                                    lines.remove(warningLine);
                                                });

                                                fadeTransition.play();
                                            }
                                        } catch (Exception ignored) {
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            } catch (SerialPortInvalidPortException e) {
                throw new RuntimeException("Error in thread");
            }
        }).start();
    }

    public Circle drawCircleMesh(double size) {
        Circle circle = new Circle(275, 275, size);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.GRAY);
        circle.setStrokeWidth(2.0);
        return circle;
    }

    public static void main(String[] args) {
        launch();
    }
}