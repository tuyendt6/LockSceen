package com.bk.android.visualeffect;

import java.util.HashMap;

/** @hide */
public interface IEffectListener {
    /**
     * A listener receives status of effects
     *
     * @param effect {@link EffectStatus}
     * @param params more detailed information of a effect's status
     */
    void onReceive(int status, HashMap<?, ?> params );
}
