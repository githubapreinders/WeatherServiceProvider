package afr.iterson.utils;

import afr.iterson.retrofitWeather.TimeModifier;
import afr.iterson.retrofitWeather.WeatherData;
import afr.iterson.retrofitWeather.WeatherForecast;
import android.util.Log;


/**
 * Utility class to convert the results of three 
 * Retrofit calls to a parseable WeatherData object. 
 * The WeatherData object is the standard call to openweather.org.
 * The WeatherForecast is a openweather api call for prediction icons.
 * The Timemodifier gets corrected time values from the Geonames api.
 * @author ap
 *
 */

public class DataMediator
{
public final static String TAG = DataMediator.class.getSimpleName();

	public static WeatherData modifyWeatherData(WeatherForecast forecast, TimeModifier modifier, WeatherData weatherData)
	{

		weatherData.setIcons(new String[] { forecast.getOnedayForecasts().get(0).getmWeather().get(0).getIcon(),
				forecast.getOnedayForecasts().get(1).getmWeather().get(0).getIcon(),
				forecast.getOnedayForecasts().get(2).getmWeather().get(0).getIcon(),
				forecast.getOnedayForecasts().get(3).getmWeather().get(0).getIcon(),
				forecast.getOnedayForecasts().get(4).getmWeather().get(0).getIcon(), });
		weatherData.setDateString(modifier.getmTime());
		weatherData.setSunriseString(modifier.getSunrisestring());
		weatherData.setSunsetString(modifier.getSunsetstring());
		weatherData.setGmtOffset(modifier.getmGmtOffset());
		weatherData.setMainicon(weatherData.getWeathers().get(0).getIcon());
		
		WeatherData.Sys sys = weatherData.getmSys();
		sys.setmCountry(modifier.getmCountryname());
		weatherData.setmSys(sys);

		return weatherData;
	}
	
	public static String[] getStringArray(long[] ids)
	{
		int lenght = ids.length;
		String[] returnarray = new String[lenght];
		int counter = 0;
		for (long entry : ids)
		{
			returnarray[counter] = String.valueOf(entry);
			Log.v(TAG, String.valueOf(returnarray[counter]));
			counter++;
		}
		return returnarray;
	}

	
}
