package si.virag.bicikel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import si.virag.bicikel.data.Station;
import si.virag.bicikel.data.StationInfo;
import si.virag.bicikel.util.AsyncLoader;
import android.content.Context;
import android.util.Log;

public class DirectXMLInformationDataLoader extends AsyncLoader<StationInfo>
{
	private static final String STATION_LIST_URL = "http://www.bicikelj.si/service/carto";
	private static final String STATION_DETAIL_URL = "http://www.bicikelj.si/service/stationdetails/";
	
	
	public DirectXMLInformationDataLoader(Context context)
	{
		super(context);
	}

	@Override
	public StationInfo loadInBackground()
	{
		Log.i(this.toString(), "Loading station data from server...");
		
		try
		{
			URL url = new URL(STATION_LIST_URL);
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(url.openStream());
			
			// Prepare station information
			StationInfo info = new StationInfo();
			
			// Get marker nodes
			NodeList nodes = document.getElementsByTagName("marker");
			
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Element stationElement = (Element)nodes.item(i);
				String name = stationElement.getAttribute("name");
				name = name.replace("-", "\n");
				
				int id = Integer.valueOf(stationElement.getAttribute("number"));
				
				// Load individual station details
				Log.i(this.toString(), "Loading details for " + name);
				
				Station station = new Station(id,
											  name,
											  stationElement.getAttribute("address"),
											  stationElement.getAttribute("fullAddress"),
											  Double.parseDouble(stationElement.getAttribute("lat")),
											  Double.parseDouble(stationElement.getAttribute("lng")),
											  stationElement.getAttribute("open").equalsIgnoreCase("1"));

				URL detailURL = new URL(STATION_DETAIL_URL + id);
				Document docDetail = builder.parse(detailURL.openStream());
				
				Element freeSpacesElement = (Element) docDetail.getElementsByTagName("free").item(0);
				Element availableBikesElement = (Element) docDetail.getElementsByTagName("available").item(0);
				Element totalSpacesElement = (Element) docDetail.getElementsByTagName("total").item(0);
				
				String freeSpaces = freeSpacesElement.getChildNodes().item(0).getNodeValue();
				String availableBikes = availableBikesElement.getChildNodes().item(0).getNodeValue();
				String totalSpaces = totalSpacesElement.getChildNodes().item(0).getNodeValue();
				
				try
				{
					station.setAvailableBikes(Integer.valueOf(freeSpaces));
					station.setFreeSpaces(Integer.valueOf(availableBikes));
					station.setTotalSpaces(Integer.valueOf(totalSpaces));
				}
				catch (NumberFormatException e)
				{
					// If there's an error with details, skip the station
					continue;
				}  
				
				Log.i(this.toString(), "A: " + station.getAvailableBikes() + " F: " + station.getFreeSpaces() + " T: " + station.getTotalSpaces());
				
				info.addStation(station);
			}
			
			return info;
		}
		catch (MalformedURLException e)
		{
			Log.e(this.toString(), "Failed to open service url!", e);
		} 
		catch (ParserConfigurationException e) 
		{
			Log.e(this.toString(), "Failed to configure DocumentBuilderFactory!", e);
		} 
		catch (SAXException e)
		{
			Log.e(this.toString(), "Failed to parse received XML document!", e);
		} 
		catch (IOException e)
		{
			Log.e(this.toString(), "Failed to connect to service.", e);
		}
	
		return null;
	}

	
	
}
