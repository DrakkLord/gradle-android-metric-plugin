package test.metric.drakklord.teststudio;

import android.app.Activity;
import android.util.Log;

public class MainActivity extends Activity {

    public static String GLOBal = "global value";

    @Override
    protected void onPause() {
        Log.e("test", "log something on pause" + " useless string addition");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.e("test", "log something on pause" + " useless string addition");
        super.onResume();
    }
}
