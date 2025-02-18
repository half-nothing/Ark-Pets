/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.animations.AnimClip.*;
import java.util.*;

import static cn.harryh.arkpets.Const.behaviorBaseWeight;


public class GeneralBehavior extends Behavior {
    protected AnimStage stageCur;
    protected AnimClipGroup stageAnimList;
    protected Iterator<AnimStage> stageItr;
    protected final ArrayList<AnimStage> stageList;
    protected final HashMap<AnimStage, AnimClipGroup> stageAnimMap;
    protected final HashMap<AnimStage, AnimDataWeight[]> stageAnimWeightMap;

    public GeneralBehavior(ArkConfig config, AnimClipGroup animList) {
        super(config, animList);

        stageAnimMap = anim_list.clusterByStage();
        stageAnimWeightMap = new HashMap<>();
        for (AnimStage key : stageAnimMap.keySet()) {
            AnimDataWeight[] temp = getActionList(stageAnimMap.get(key));
            if (temp.length > 0)
                stageAnimWeightMap.put(key, temp);
        }

        stageList = new ArrayList<>(stageAnimWeightMap.keySet().stream().toList());
        stageList.sort(Comparator.comparing(AnimStage::id));
        if (stageList.isEmpty())
            throw new NoSuchElementException("Animation stage map was empty because no animation's name was matched.");
        stageItr = stageList.iterator();

        action_list = new AnimDataWeight[0];
        nextStage();
    }

    public void nextStage() {
        if (!stageItr.hasNext())
            stageItr = stageList.iterator();
        stageCur = stageItr.next();
        stageAnimList = stageAnimMap.get(stageCur);
        action_list = stageAnimWeightMap.get(stageCur);
        autoCtrlReset();
    }

    public Set<AnimStage> getStages() {
        return stageAnimMap.keySet();
    }

    public AnimStage getCurrentStage() {
        return stageCur;
    }

    private AnimDataWeight[] getActionList(AnimClipGroup animList) {
        ArrayList<AnimDataWeight> actionList = new ArrayList<>(List.of(
                new AnimDataWeight(
                        animList.getLoopAnimData(AnimType.IDLE),
                        Math.round(behaviorBaseWeight / (float) Math.sqrt(config.behavior_ai_activation))
                ),
                new AnimDataWeight(
                        animList.getLoopAnimData(AnimType.SIT),
                        config.behavior_allow_sit  ? 1 << 6 : 0
                ),
                new AnimDataWeight(
                        animList.getLoopAnimData(AnimType.SLEEP),
                        config.behavior_allow_sleep ? 1 << 5 : 0
                ),
                new AnimDataWeight(
                        animList.getLoopAnimData(AnimType.MOVE).derive(+1),
                        config.behavior_allow_walk ? 1 << 5 : 0
                ),
                new AnimDataWeight(
                        animList.getLoopAnimData(AnimType.MOVE).derive(-1),
                        config.behavior_allow_walk ? 1 << 5 : 0),
                new AnimDataWeight(
                        animList.getStrictAnimData(AnimType.SPECIAL)
                                .join(animList.getLoopAnimData(AnimType.IDLE)),
                        config.behavior_allow_special ? 1 << 4 : 0
                )
        ));
        actionList.removeIf(e -> e.anim().isEmpty());
        return actionList.toArray(new AnimDataWeight[0]);
    }

    @Override
    public AnimData defaultAnim() {
        return stageAnimList.getLoopAnimData(AnimType.IDLE);
    }

    @Override
    public AnimData clickEnd() {
        AnimData a1 = stageAnimList.getStreamedAnimData(AnimType.ATTACK);
        AnimData a2 = stageAnimList.getStreamedAnimData(AnimType.INTERACT);
        AnimData a3 = a2.isEmpty() ? a1 : a2;
        return new AnimData(a3.animClip(), a3.animNext(), false, true, a3.mobility()).join(defaultAnim());
    }

    @Override
    public AnimData dropped() {
        return clickEnd();
    }
}
