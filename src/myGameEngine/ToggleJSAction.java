package myGameEngine;

import myGame.MyGame;
import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import net.java.games.input.Event;

public class ToggleJSAction extends AbstractInputAction
{
    private MyGame game;

    public ToggleJSAction(MyGame g)
    {
        game = g;
    }

    public void performAction(float time, Event event)
    {
        System.out.println("toggle javascript initiated");
        game.toggleJS();
    }
}

