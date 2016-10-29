package com.bk.android.visualeffect;
import com.bk.android.visualeffect.lock.data.PoppingColorData;

public class EffectDataObj {
    
    public PoppingColorData poppingColorData;
    
    public void setEffect(int effect)
    {
        switch(effect)
        {
        case EffectType.POPPING_COLOUR :
            poppingColorData = new PoppingColorData();
            break;
        }
    }
}
