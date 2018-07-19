package com.ylsk.datafilter;

public class CompassFilter implements IFilter {

    private float smoothFactorCompass = 0.5f;
    private float smoothThresholdCompass = 30.0f;
    private float oldCompass = 0.0f;

    public CompassFilter(float smoothFactorCompass, float smoothThresholdCompass) {
        this.smoothFactorCompass = smoothFactorCompass;
        this.smoothThresholdCompass = smoothThresholdCompass;
    }

    @Override
    public float doFilter(float newCompass){
        if(Math.abs(newCompass - oldCompass) < 180){
            if(Math.abs(newCompass - this.oldCompass) > smoothThresholdCompass)
            {
                oldCompass = newCompass;
            }
            else
            {
                oldCompass = oldCompass + smoothFactorCompass * (newCompass - oldCompass);
            }
        }else{
            if(360 - Math.abs(newCompass - oldCompass) > smoothThresholdCompass){
                oldCompass = newCompass;
            }else{
                if(oldCompass > newCompass){
                    oldCompass = ( oldCompass + smoothFactorCompass * ((360 + newCompass - oldCompass)%360)) % 360;
                }else{
                    oldCompass = ( oldCompass - smoothFactorCompass * ((360 - newCompass + oldCompass)%360)) % 360;
                }
            }
        }

        if(oldCompass < 0)
        {
            oldCompass+=360;
        }

        return oldCompass;
    }
}
