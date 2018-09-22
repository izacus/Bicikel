package si.virag.bicikelj;

import android.app.Application;

import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public class BicikeljApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new CrashlyticsCore());
    }
}

