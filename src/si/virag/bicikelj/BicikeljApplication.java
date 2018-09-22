package si.virag.bicikelj;

import android.app.Application;

import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public class BicikeljApplication extends Application {

    public static final String BICIKELJ_PRIVACY_URL = "https://mavrik.bitbucket.io/bicikel-privacy-en.html";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new CrashlyticsCore());
    }
}

