package si.virag.bicikelj.stations.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import si.virag.bicikelj.util.ISO8601;

public class CityBikesApiClient {

    public static CityBikesApi getBicikeljApi() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .registerTypeAdapter(Calendar.class, new CalendarTypeAdapter())
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://api.citybik.es")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
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
