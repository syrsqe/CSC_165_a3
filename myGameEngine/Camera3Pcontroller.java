package myGameEngine;

import net.java.games.input.Component;
import ray.rage.scene.*;
import ray.rml.*;
import ray.input.*;
import ray.input.action.*;

// Camera controller for orbiting around a node
public class Camera3Pcontroller
{
    private Camera camera; //the camera being controlled
    private SceneNode cameraN; //the node the camera is attached to
    private SceneNode target; //the target the camera looks at
    private float cameraAzimuth; //rotation of camera around Y axis
    private float cameraElevation; //elevation of camera above target
    private float radius; //distance between camera and target
    private Vector3 worldUpVec;
    private float rotationAmount;

    private float maxCameraElevation = 55; // camera restrictions
    private float minCameraElevation = -10;
    private float maxCameraRadius = 3.0f;
    private float minCameraRadius = 0.8f;

    private float zoomMultiplier = 0.02f; // slows down zoom speed

    public Camera3Pcontroller(Camera cam, SceneNode camN, SceneNode targ, String controllerName, InputManager im, float rotAmount)
    {
        camera = cam;
        cameraN = camN;
        target = targ;
        rotationAmount = rotAmount;
        cameraAzimuth = 225.0f;// start from BEHIND and ABOVE the target
        cameraElevation = 20.0f;// elevation is in degrees
        radius = 2.0f;
        worldUpVec = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
        setupInput(im, controllerName);
        updateCameraPosition();
    }


    private void setupInput(InputManager im, String cn)
    {
        if (cn.toLowerCase().contains("keyboard"))
        {
            Action orbitRightAction = new OrbitAroundRightAction();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.RIGHT, orbitRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            Action orbitLeftAction = new OrbitAroundLeftAction();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.LEFT, orbitLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            Action orbitUpAction = new OrbitElevationUpAction();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.UP, orbitUpAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            Action orbitDownAction = new OrbitElevationDownAction();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.DOWN, orbitDownAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

            // Z and X keys to zoom in and out
            Action zoomIn = new ZoomInAction();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.Z, zoomIn, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            Action zoomOut = new ZoomOutAction();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.X, zoomOut, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        }

        else if (cn.toLowerCase().contains("controller") || cn.toLowerCase().contains("gamepad"))
        {
            // Intended for use with Playstation 4 controller. Uses the buttons L2 and R2 for camera left and right rotation.
            // Uses the Triangle and X buttons for camera up and down rotation.
            Action orbitUpAction = new OrbitElevationUpAction();
            im.associateAction(cn,net.java.games.input.Component.Identifier.Button._3,orbitUpAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); // triangle button
            Action orbitDownAction = new OrbitElevationDownAction();
            im.associateAction(cn,net.java.games.input.Component.Identifier.Button._1,orbitDownAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); // X button
            GamepadOrbitAroundLeftAction gpLeft = new GamepadOrbitAroundLeftAction();
            im.associateAction(cn, Component.Identifier.Axis.RX,gpLeft,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            GamepadOrbitAroundRightAction gpRight = new GamepadOrbitAroundRightAction();
            im.associateAction(cn, Component.Identifier.Axis.RY,gpRight,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

            // Square and Circle buttons for zooming
            Action zoomIn = new ZoomInAction();
            im.associateAction(cn, Component.Identifier.Button._0,zoomIn,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); // square button
            Action zoomOut = new ZoomOutAction();
            im.associateAction(cn, Component.Identifier.Button._2,zoomOut,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); // circle button
        }

        else
            System.out.println("Error: no keyboard or gamepad connected.");
    }

    private class OrbitAroundLeftAction extends AbstractInputAction
    {
        // Moves the camera around the target (changes camera azimuth).
        public void performAction(float time, net.java.games.input.Event evt)
        {
            float rotAmount;

            if (evt.getValue() > 0.2)
                rotAmount = -rotationAmount;
            else
                rotAmount = 0.0f;

            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    private class OrbitAroundRightAction extends AbstractInputAction
    {
        // Moves the camera around the target (changes camera azimuth).
        public void performAction(float time, net.java.games.input.Event evt)
        {
            float rotAmount;

            if (evt.getValue() > 0.2)
                rotAmount = rotationAmount;
            else
                rotAmount = 0.0f;

            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    private class OrbitElevationUpAction extends AbstractInputAction
    {
        // Moves the camera around the target (changes camera azimuth).
        public void performAction(float time, net.java.games.input.Event evt)
        {
            float rotAmount;

            if (evt.getValue() > 0.2)
                rotAmount = rotationAmount;
            else
                rotAmount = 0.0f;

            if (cameraElevation >= maxCameraElevation)
                rotAmount = 0.0f;

            cameraElevation += rotAmount;
            cameraElevation = cameraElevation % 360;
            updateCameraPosition();
        }
    }

    private class OrbitElevationDownAction extends AbstractInputAction
    {
        // Moves the camera around the target (changes camera azimuth).
        public void performAction(float time, net.java.games.input.Event evt)
        {
            float rotAmount;

            if (evt.getValue() > 0.2)
                rotAmount = -rotationAmount;
            else
                rotAmount = 0.0f;

            if (cameraElevation <= minCameraElevation)
                rotAmount = 0.0f;

            cameraElevation += rotAmount;
            cameraElevation = cameraElevation % 360;
            updateCameraPosition();
        }
    }


    private class GamepadOrbitAroundLeftAction extends AbstractInputAction
    {
        // Moves the camera around the target (changes camera azimuth).
        public void performAction(float time, net.java.games.input.Event evt)
        {
            float rotAmount;

            if (evt.getValue() >= 1.0f)
                rotAmount = -rotationAmount;
            else
                rotAmount = 0.0f;

            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    private class GamepadOrbitAroundRightAction extends AbstractInputAction
    {
        // Moves the camera around the target (changes camera azimuth).
        public void performAction(float time, net.java.games.input.Event evt)
        {
            float rotAmount;

            if (evt.getValue() >= 1.0f)
                rotAmount = rotationAmount;
            else
                rotAmount = 0.0f;

            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    private class ZoomInAction extends AbstractInputAction
    {
        // Moves the camera closer to the target (changes camera radius).
        public void performAction(float time, net.java.games.input.Event evt)
        {
            float rotAmount;

            if (evt.getValue() > 0.2)
                rotAmount = -rotationAmount;
            else
                rotAmount = 0.0f;

            if (radius <= minCameraRadius)
                rotAmount = 0.0f;

            radius += (rotAmount * zoomMultiplier);
            updateCameraPosition();
        }
    }

    private class ZoomOutAction extends AbstractInputAction
    {
        // Moves the camera closer to the target (changes camera radius).
        public void performAction(float time, net.java.games.input.Event evt)
        {
            float rotAmount;

            if (evt.getValue() > 0.2)
                rotAmount = rotationAmount;
            else
                rotAmount = 0.0f;

            if (radius >= maxCameraRadius)
                rotAmount = 0.0f;

            radius += (rotAmount * zoomMultiplier);
            updateCameraPosition();
        }
    }


    // Updates camera position: computes azimuth, elevation, and distance
    // relative to the target in spherical coordinates, then converts those
    // to world Cartesian coordinates and setting the camera position
    public void updateCameraPosition()
    {
        double theta = Math.toRadians(cameraAzimuth);// rot around target
        double phi = Math.toRadians(cameraElevation);// altitude angle
        double x = radius * Math.cos(phi) * Math.sin(theta);
        double y = radius * Math.sin(phi);
        double z = radius * Math.cos(phi) * Math.cos(theta);
        cameraN.setLocalPosition(Vector3f.createFrom((float)x, (float)y, (float)z).add(target.getWorldPosition()));
        cameraN.lookAt(target, worldUpVec);
    }


}