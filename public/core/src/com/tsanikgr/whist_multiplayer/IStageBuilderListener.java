package com.tsanikgr.whist_multiplayer;

import com.tsanikgr.whist_multiplayer.myactors.MyGroup;

public interface IStageBuilderListener {
    void onGroupBuilded(String fileName, MyGroup group);
    void onGroupBuildFailed(String fileName, Exception e);
}
