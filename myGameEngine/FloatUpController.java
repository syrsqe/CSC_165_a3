package myGameEngine;

import ray.rage.scene.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;

// Make node float upward for a certain length of time
public class FloatUpController extends AbstractController
{
    private float moveRate = .01f; // movement per second
    private float animationTime = 12000.0f; // how long the objects move
    private float totalTime = 0.0f;
    private float direction = 1.0f;

    @Override
    protected void updateImpl(float elapsedTimeMillis)
    {
        totalTime += elapsedTimeMillis;
        float moveAmt = direction * moveRate;

        if (totalTime <= animationTime)
        {
            for (Node n : super.controlledNodesList)
            {
                Vector3 curPosition = n.getLocalPosition();
                curPosition = Vector3f.createFrom(curPosition.x(), curPosition.y() + moveAmt, curPosition.z());
                n.setLocalPosition(curPosition);
            }
        }
    }

}
