package myGameEngine;

import networking.*;

import net.java.games.input.Component;
import net.java.games.input.Event;
import ray.rage.scene.*;
import ray.input.*;
import ray.input.action.*;
import ray.rml.Degreef;

//not sure if needed
import ray.rage.game.*;
import ray.rml.*;

// Player movement controller
public class PlayerController {
    private SceneNode player; // the player we will be controlling
    private float movementSpeed;
    private float rotationMultiplier = 7.0f; // turning is too slow if just using movementSpeed. Multiply it with this value.
    private ProtocolClient protClient;


    public PlayerController(SceneNode playerN, String controllerName, InputManager im, float speed) {
        player = playerN;
        movementSpeed = speed;
        setupInput(im, controllerName);
    }

    //constructor if game is a client
    public PlayerController(SceneNode playerN, String controllerName, InputManager im, float speed, ProtocolClient p) {
        player = playerN;
        movementSpeed = speed;
        setupInput(im, controllerName);
        protClient = p;
    }

    private void setupInput(InputManager im, String cn) {
        if (cn.toLowerCase().contains("keyboard")) {
            // WASD keys for movement
            MoveForwardAction moveForwardAction = new MoveForwardAction();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.W, moveForwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            MoveLeftAction moveLeftAction = new MoveLeftAction();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.A, moveLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            MoveRightAction moveRightAction = new MoveRightAction();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.D, moveRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            MoveBackwardAction moveBackwardAction = new MoveBackwardAction();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.S, moveBackwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

            // Q and E keys for turning
            RotateLeftAction turnLeftAction = new RotateLeftAction();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.Q, turnLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            RotateRightAction turnRightAction = new RotateRightAction();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.E, turnRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        } else if (cn.toLowerCase().contains("controller") || cn.toLowerCase().contains("gamepad")) {
            // NOTE: due to sensitivity issues when using the left and right sticks on a PS4 controller, movement is controlled with the directional buttons

            // movement in all 4 directions
            GamepadMovementAction gamepadMovementAction = new GamepadMovementAction(); // handles all 4 directional buttons
            im.associateAction(cn, Component.Identifier.Axis.POV, gamepadMovementAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

            // tank controls, doesn't as work well on controller as it does on keyboard
            //GamepadMovementAndRotateAction gamepadAction = new GamepadMovementAndRotateAction(); // handles all 4 directional buttons
            //im.associateAction(cn, Component.Identifier.Axis.POV,gamepadAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

            // L1 and R1 buttons for rotation
            RotateLeftAction leftAction = new RotateLeftAction();
            im.associateAction(cn, Component.Identifier.Button._4, leftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); // L1
            RotateRightAction rightAction = new RotateRightAction();
            im.associateAction(cn, Component.Identifier.Button._5, rightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); // R1
        } else
            System.out.println("Error: no keyboard or gamepad connected.");
    }


    private class MoveLeftAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            player.moveLeft(-movementSpeed);
            if (protClient != null) {
                Quaternion playerRotation = player.getWorldRotation().toQuaternion();

                protClient.sendMoveMessage((Vector3f) player.getWorldPosition(), playerRotation);
            }
        }
    }

    private class MoveRightAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            player.moveRight(-movementSpeed);

            if (protClient != null) {
                Quaternion playerRotation = player.getWorldRotation().toQuaternion();

                protClient.sendMoveMessage((Vector3f) player.getWorldPosition(), playerRotation);
            }
        }
    }

    private class MoveForwardAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            player.moveForward(movementSpeed);

            if (protClient != null) {
                Quaternion playerRotation = player.getWorldRotation().toQuaternion();
                protClient.sendMoveMessage((Vector3f) player.getWorldPosition(), playerRotation);

            }
        }

    }

    private class MoveBackwardAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            //node.moveBackward(movementSpeed); // not working
            player.moveForward(-movementSpeed);

            if (protClient != null) {
                Quaternion playerRotation = player.getWorldRotation().toQuaternion();

                protClient.sendMoveMessage((Vector3f) player.getWorldPosition(), playerRotation);
            }
        }
    }

    private class GamepadMovementAction extends AbstractInputAction {
        // intended for use with Playstation 4 controller. Uses the 4 directional buttons on the left side of the controller.
        public void performAction(float time, Event e) {
            // There are 4 directional buttons. Each is represented by its own unique input value.
            // LEFT = 1.0
            // RIGHT = 0.5
            // DOWN = 0.75
            // Up = 0.25
            // These values do not change no matter how hard the button is pressed.

            float value = e.getValue();
            if (value == 1f)
                player.moveLeft(-movementSpeed);
            else if (value == 0.5f)
                player.moveRight(-movementSpeed);
            else if (value == 0.75f)
                player.moveForward(-movementSpeed);
            else if (value == 0.25f)
                player.moveForward(movementSpeed);
        }
    }

    private class GamepadMovementAndRotateAction extends AbstractInputAction {
        // Tank controls
        // intended for use with Playstation 4 controller. Uses the 4 directional buttons on the left side of the controller.
        public void performAction(float time, Event e) {
            // There are 4 directional buttons. Each is represented by its own unique input value.
            // LEFT = 1.0
            // RIGHT = 0.5
            // DOWN = 0.75
            // Up = 0.25
            // These values do not change no matter how hard the button is pressed.

            float value = e.getValue();
            if (value == 1f) {
                Degreef a = Degreef.createFrom(movementSpeed * rotationMultiplier);
                player.yaw(a);
            } else if (value == 0.5f) {
                Degreef a = Degreef.createFrom(-movementSpeed * rotationMultiplier);
                player.yaw(a);
            } else if (value == 0.75f)
                player.moveForward(-movementSpeed);
            else if (value == 0.25f)
                player.moveForward(movementSpeed);
        }
    }

    private class RotateLeftAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            Degreef a = Degreef.createFrom(movementSpeed * rotationMultiplier);
            player.yaw(a);
            if (protClient != null) {
                Quaternion playerRotation = player.getWorldRotation().toQuaternion();
                protClient.sendMoveMessage((Vector3f) player.getWorldPosition(), playerRotation);

            }
        }
    }

    private class RotateRightAction extends AbstractInputAction {
        public void performAction(float time, Event e) {
            Degreef a = Degreef.createFrom(-movementSpeed * rotationMultiplier);
            player.yaw(a);
            if (protClient != null) {
                Quaternion playerRotation = player.getWorldRotation().toQuaternion();
                protClient.sendMoveMessage((Vector3f) player.getWorldPosition(), playerRotation);

            }
        }
    }

    public void updateSpeed(float s) {
        movementSpeed = s;
    }
}
