package si.virag.bicikelj;

import android.app.Application;
import android.content.Context;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class BicikeljApplication extends Application {

    public static final String BICIKELJ_PRIVACY_URL = "https://izacus.github.io/Bicikel/privacy-en.html";
    private BicikeljComponent component;

    public static BicikeljComponent component(Context context) {
        return ((BicikeljApplication) context.getApplicationContext()).component;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
        component = DaggerBicikeljComponent.factory().create(this);
    }
}

