package myGameEngine;

import ray.rage.scene.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;

// Make node bounce up and down continuously
public class BounceController extends AbstractController
{
    private float bounceRate = .01f; // movement per second
    private float cycleTime = 1500.0f; // default cycle time
    private float totalTime = 0.0f;
    private float direction = 1.0f;

    @Override
    protected void updateImpl(float elapsedTimeMillis)
    {
        totalTime += elapsedTimeMillis;
        float moveAmt = direction * bounceRate;

        if (totalTime > cycleTime)
        {
            direction = -direction;
            totalTime = 0.0f;
        }
        for (Node n : super.controlledNodesList)
        {
            Vector3 curPosition = n.getLocalPosition();
            curPosition = Vector3f.createFrom(curPosition.x(), curPosition.y() + moveAmt, curPosition.z());
            n.setLocalPosition(curPosition);
        }
    }
}