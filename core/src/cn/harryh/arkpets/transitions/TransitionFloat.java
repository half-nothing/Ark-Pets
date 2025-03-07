/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.transitions;


/** The class represents a transition,
 * which controls a float number transit from its starting value to its ending value.
 */
public class TransitionFloat extends Transition<Float> {
    protected final EasingFunction easing;

    public TransitionFloat(EasingFunction easingFunction, float totalProgress) {
        super(totalProgress);
        easing = easingFunction;
        start = 0f;
        end = 0f;
    }

    @Override
    public Float atProgress(float progress) {
        return totalProgress > 0 ? easing.apply(start, end, currentProgress / totalProgress) : end;
    }
}
