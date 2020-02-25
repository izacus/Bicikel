package si.virag.bicikelj;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public class BicikeljApplication extends Application {

    public static final String BICIKELJ_PRIVACY_URL = "https://izacus.github.io/Bicikel/privacy-en.html";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics.Builder().core(
                new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());
        BicikeljComponent component = DaggerBicikeljComponent.create();
    }
}

