package si.virag.bicikelj.stations.api;

import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import si.virag.bicikelj.BuildConfig;

public class CityBikesApiClient {

    public static CityBikesApi getBicikeljApi() {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .registerTypeAdapter(Calendar.class, new CalendarTypeAdapter())
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(
                BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit restAdapter = new Retrofit.Builder().baseUrl("https://api.citybik.es")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return restAdapter.create(CityBikesApi.class);
    }

    private static class CalendarTypeAdapter extends TypeAdapter<Calendar> {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        private final TimeZone tz = TimeZone.getTimeZone("UTC");

        public CalendarTypeAdapter() {
            dateFormat.setTimeZone(tz);
        }

        @Override
        public void write(JsonWriter out, Calendar value) throws IOException {
            throw new RuntimeException("Not implemented.");
        }

        @Override
        public Calendar read(JsonReader in) throws IOException {
            String timeStamp = in.nextString();
            try {
                Date date = dateFormat.parse(timeStamp);
                if (date == null) {
                    throw new IOException("Date is null");
                }

                Calendar c = Calendar.getInstance();
                // This converts from UTC to local timezone.
                c.setTimeZone(tz);
                c.setTime(date);
                c.setTimeZone(TimeZone.getDefault());
                return c;
            } catch (ParseException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e("ApiClient", "Failed to parse date", e);
                throw new IOException("Failed to parse date");
            }
        }
    }
}
