
package com.bk.android.visualeffect;

import java.util.HashMap;

import android.view.MotionEvent;
import android.view.View;


/**
 * Alll effects in visual effect library implement IEffect. 
 * @author hs0726.park
 *
 */
/** @hide */
public interface IEffectView {
    
    void init(EffectDataObj data);
    
    void reInit(EffectDataObj data);
    
    void handleTouchEvent(MotionEvent event, View view);
    /**
     * @param cmd {@link EffectCmdType}
     * @param params hashmap object
     */
    void handleCustomEvent(int cmd, HashMap<?, ?> params);
    
    void clearScreen();
    
    void setListener(IEffectListener listener);
    void removeListener();

    /**
     * When the clearEffect() called, all effect should disappear at once.
     */
}
