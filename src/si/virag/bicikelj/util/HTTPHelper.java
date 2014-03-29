package si.virag.bicikelj.util;

import android.os.Build;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class HTTPHelper
{
	static
	{
	    // HTTP connection reuse which was buggy pre-froyo
	    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
	        System.setProperty("http.keepAlive", "false");
	    }
	}
	
	public static Date parseISO8601(String date) throws ParseException
	{
		// There's a bug in SimpleDateFormatter library so we're parsing dates
		// manually
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssz");
		return formatter.parse(date);
	}

	/**
	 * Builds a HTTP parameter string for URL inclusion
	 * 
	 * @param params
	 *            Parameter key/value pairs
	 * @return built parameter string
	 */
	public static String buildGetParams(Map<String, String> params)
	{
		if (params == null)
			return "";

		StringBuilder paramString = new StringBuilder();
		paramString.append("?");

		for (String key : params.keySet())
		{
            try
            {
                paramString.append(URLEncoder.encode(key, "UTF-8"));
                paramString.append("=");
                paramString.append(URLEncoder.encode(params.get(key), "UTF-8"));
                paramString.append("&");

            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException("No UTF-8 support on device!");
            }
		}

		// Delete the last amperstand
		paramString.deleteCharAt(paramString.length() - 1);

		Log.d("Utilities", paramString.toString());

		return paramString.toString();
	}

	/**
	 * Reads data from input stream to the end and returns it as single string
	 * 
	 * @param is
	 *            Input stream to read
	 * @return data from stream
	 */
	private static String convertStreamToString(InputStream is)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line;
		try
		{
			while ((line = reader.readLine()) != null)
			{
				sb.append(line);
                sb.append("\n");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	public static String httpGet(String url) throws IOException
	{
		return httpGet(url, null);
	}

	/**
	 * Makes a GET request to a server and reads response
	 * 
	 * @param urlString
	 *            URL of the resource on server
	 * @param params
	 *            Parameters to be appended to URL
	 * @return Server response or <b>null</b> if the request failed
	 * @throws IOException
	 */
	public static String httpGet(String urlString, String params) throws IOException
	{
		URL url = new URL(urlString + (params != null ? params : ""));
		
		URLConnection connection;
		if (urlString.startsWith("https"))
		{
			Log.d("HTTPHelper", "[HTTPS] Getting " + urlString);
			connection = (HttpsURLConnection)url.openConnection();
		}
		else
		{
			Log.d("HTTPHelper", "[HTTP] Getting " + urlString);
			connection = (HttpURLConnection)url.openConnection();
		}
		
		String responseString = null;
		try
		{
			InputStream instream = new BufferedInputStream(connection.getInputStream());
			responseString = HTTPHelper.convertStreamToString(instream);
			instream.close();
		}
		finally
		{
			if (connection instanceof HttpsURLConnection)
			{
				((HttpsURLConnection)connection).disconnect();
			}
			else
			{
				((HttpURLConnection)connection).disconnect();
			}
		}
		
		return responseString;
	}

	public static String httpPost(String url) throws IOException
	{
		return httpPost(url, null);
	}
	
	public static String httpPost(String url, Map<String, String> parameters) throws IOException
	{
		// TODO: update library
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		
		// Prepare POST parameters
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		
		if (parameters != null)
		{
			for (String param : parameters.keySet())
			{
				nameValuePairs.add(new BasicNameValuePair(param, parameters.get(param)));
			}
		}
		
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		// Do call
		HttpResponse response = client.execute(post);
		HttpEntity entity = response.getEntity();

		if (entity != null)
		{
			InputStream instream = entity.getContent();
			String responseString = HTTPHelper.convertStreamToString(instream);
			instream.close();

			return responseString;
		}

		return null;
	}
}
