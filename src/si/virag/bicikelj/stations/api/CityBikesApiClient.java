package si.virag.bicikelj.stations.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import si.virag.bicikelj.BuildConfig;

public class CityBikesApiClient {

    public static CityBikesApi getBicikeljApi() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .registerTypeAdapter(Calendar.class, new CalendarTypeAdapter())
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl("https://api.citybik.es")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return restAdapter.create(CityBikesApi.class);
    }

    private static class CalendarTypeAdapter extends TypeAdapter<Calendar> {
        @Override
        public void write(JsonWriter out, Calendar value) throws IOException {
            throw new RuntimeException("Not implemented.");
        }

        @Override
        public Calendar read(JsonReader in) throws IOException {
            long epochTime = in.nextLong();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(epochTime);
            return c;
        }
    }
}
