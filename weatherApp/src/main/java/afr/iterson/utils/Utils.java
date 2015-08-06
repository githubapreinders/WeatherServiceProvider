package afr.iterson.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import afr.iterson.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Useful helper methods and fields.
 */
public class Utils
{
	/**
	 * Logging tag.
	 */
	private final static String TAG = Utils.class.getCanonicalName();

	/**
	 * URL to the weather api to use with the Retrofit service.
	 */
	public static final String sWeather_Service_URL_Retro = "http://api.openweathermap.org/data/2.5";

	public static final String Geo_Names_URL = "http://api.geonames.org";

	public static String USERNAME = "apreinders";

	public final static String Weather_Service_URL_IDS = "http://api.openweathermap.org/data/2.5/group?id=";

	public static int DAYS_AHEAD = 5;

	public final static String WEATHER_DATA = "weather_data5";
	public final static String WEATHER_DATASTRING = "weather_datastring2";
	public final static String KEY_CITY = "city";
	public final static String KEY_CITYID = "cityid";
	public final static String KEY_COUNTRY = "country";
	public final static String KEY_WINDSPEED = "windspeed";
	public final static String KEY_WINDANGLE = "windangle";
	public final static String KEY_TEMPERATURE = "temperature";
	public final static String KEY_HUM = "humidity";
	public final static String KEY_SUNRISE = "sunrise";
	public final static String KEY_SUNSET = "sunset";
	public final static String KEY_ICON = "icon";
	public final static String KEY_SUNSETSTRING = "sunsetstring";
	public final static String KEY_SUNRISESTRING = "sunrisestring";
	public final static String KEY_LOCALTIME = "localtime";
	public final static String KEY_GMTOFFSET = "gmtoffset";
	public final static String KEY_CITYLIST = "arraylist_of_cities";
	public final static String KEY_CHECKED_CITIES = "checked_cities";
	public static final String KEY_TIMECHEATER = "timecheater";

	public static final String Key_ICONS_FORECAST = "icons_forecast";

	/**
	 * This method is used to hide a keyboard after a user has finished typing
	 * the url.
	 */
	public static void hideKeyboard(Activity activity, IBinder windowToken)
	{
		InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(windowToken, 0);
	}

	/**
	 * Show a toast message.
	 */
	public static void showToast(Context context, String message)
	{
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Make Utils a utility class by preventing instantiation.
	 */
	private Utils()
	{
		throw new AssertionError();
	}

	/**
	 * Helper method to format the Temprature returned by the OpenWeatherMap
	 * call. The default is Celsius.
	 *
	 * @param Application
	 *            Context
	 * @param temprature
	 *            from OpenWeatherMap API response
	 * @param isFarhenheit
	 * @return formatted Temprature
	 */
	public static String formatTemperature(Context context, double temperature, boolean isCelsius)
	{
		if (isCelsius)
		{
			// Conversion of Kelvin to Celsius temperature.
			temperature = temperature - 273;
			return (String.valueOf((int)temperature) + context.getString(R.string.deg_celsius));
		}

		else
		{
			// Conversion of Kelvin to Fahrenheit temperature.
			temperature = 1.8 * (temperature - 273) + 32;
			return (String.valueOf((int)temperature) + context.getString(R.string.deg_fahrenhei));
		}

	}

	/**
	 * Helper method to format the Date returned by the OpenWeatherMap call.
	 * 
	 * @return formatted Current Date String
	 */
	public static String formatCurrentDate()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("MMM  dd ");
		Calendar c = Calendar.getInstance();
		return sdf.format(c.getTime());
	}

	/**
	 * Helper method to format the Sunsrise and Sunset time returned by the
	 * OpenWeatherMap call.
	 *
	 * @param Time
	 *            in Seconds.
	 * @return formatted Time String
	 */
	public static String formatTime(long time)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time * 1000);
		return sdf.format(c.getTime());
	}

	/**
	 * Helper method to format the Wind returned by the OpenWeatherMap call.
	 *
	 * @param Application
	 *            Context
	 * @param Wind
	 *            Speed from OpenWeatherMap API response
	 * @param Wind
	 *            Direction from OpenWeatherMap API response
	 * @return formatted String of Wind Data
	 */
	public static String getFormattedWind(Context context, double windSpeedStr, double windDirStr)
	{
		int windFormat;
		windFormat = R.string.format_wind_kmh;

		// From wind direction in degrees, determine compass direction
		// as a string (e.g., NW).
		String direction = "Unknown";
		if (windDirStr >= 337.5 || windDirStr < 22.5)
			direction = "N";
		else if (windDirStr >= 22.5 && windDirStr < 67.5)
			direction = "NE";
		else if (windDirStr >= 67.5 && windDirStr < 112.5)
			direction = "E";
		else if (windDirStr >= 112.5 && windDirStr < 157.5)
			direction = "SE";
		else if (windDirStr >= 157.5 && windDirStr < 202.5)
			direction = "S";
		else if (windDirStr >= 202.5 && windDirStr < 247.5)
			direction = "SW";
		else if (windDirStr >= 247.5 && windDirStr < 292.5)
			direction = "W";
		else if (windDirStr >= 292.5 && windDirStr < 337.5)
			direction = "NW";

		return String.format(context.getString(windFormat), windSpeedStr, direction);
	}

	public static String convertToBeaufort(double windspeed)
	{
		String returnvalue = "";
		if (windspeed > 0 && windspeed < 0.3)
		{
			returnvalue = "Calm";
		} else if (windspeed >= 0.3 && windspeed < 1.5)
		{
			returnvalue = "Light air";
		} else if (windspeed >= 0.3 && windspeed < 1.5)
		{
			returnvalue = "Light air";
		} else if (windspeed >= 1.5 && windspeed < 3.3)
		{
			returnvalue = "Light breeze";
		} else if (windspeed >= 3.3 && windspeed < 5.5)
		{
			returnvalue = "Gentle breeze";
		} else if (windspeed >= 5.5 && windspeed < 8.0)
		{
			returnvalue = "Moderate breeze";
		} else if (windspeed >= 8.0 && windspeed < 10.8)
		{
			returnvalue = "Fresh breeze";
		} else if (windspeed >= 10.8 && windspeed < 13.9)
		{
			returnvalue = "Strong breeze";
		} else if (windspeed >= 13.9 && windspeed < 17.2)
		{
			returnvalue = "Near gale";
		} else if (windspeed >= 17.2 && windspeed < 20.7)
		{
			returnvalue = "Fresh gale";
		} else if (windspeed >= 20.7 && windspeed < 24.5)
		{
			returnvalue = "Strong gale";
		} else if (windspeed >= 24.5 && windspeed < 28.4)
		{
			returnvalue = "Storm";
		} else if (windspeed >= 28.4 && windspeed < 32.6)
		{
			returnvalue = "Violent Storm";
		} else if (windspeed >= 32.6)
		{
			returnvalue = "Hurricane";
		}

		return returnvalue;
	}

	/**
	 * Converts the meteorological wind degrees to readable values source :
	 * http://climate.umn.edu/snow_fence/components/
	 * winddirectionanddegreeswithouttable3.htm Isn't used cause I later on
	 * preferred the image to the simpler letter combination.
	 */

	public static String convertToWindDirection(int winddirection)
	{
		String returnvalue = "";
		if (winddirection > 348 && winddirection < 361)
		{
			returnvalue = "N";
		} else if (winddirection > 0 && winddirection < 11)
		{
			returnvalue = "N";
		} else if (winddirection > 10 && winddirection < 34)
		{
			returnvalue = "NNE";
		} else if (winddirection > 33 && winddirection < 56)
		{
			returnvalue = "NE";
		} else if (winddirection > 55 && winddirection < 79)
		{
			returnvalue = "ENE";
		} else if (winddirection > 78 && winddirection < 102)
		{
			returnvalue = "E";
		} else if (winddirection > 101 && winddirection < 124)
		{
			returnvalue = "ESE";
		} else if (winddirection > 123 && winddirection < 147)
		{
			returnvalue = "SE";
		} else if (winddirection > 146 && winddirection < 169)
		{
			returnvalue = "SSE";
		} else if (winddirection > 168 && winddirection < 192)
		{
			returnvalue = "S";
		} else if (winddirection > 191 && winddirection < 214)
		{
			returnvalue = "SSW";
		} else if (winddirection > 213 && winddirection < 237)
		{
			returnvalue = "SW";
		} else if (winddirection > 236 && winddirection < 259)
		{
			returnvalue = "WSW";
		} else if (winddirection > 258 && winddirection < 282)
		{
			returnvalue = "W";
		} else if (winddirection > 281 && winddirection < 304)
		{
			returnvalue = "WNW";
		} else if (winddirection > 303 && winddirection < 327)
		{
			returnvalue = "NW";
		} else if (winddirection > 326 && winddirection < 349)
		{
			returnvalue = "NNW";
		}

		return returnvalue;

	}

	/**
	 * Winddirection is a double between 0 and 360, convert this here into the
	 * appropriate image.
	 */
	public static int convertToWinddirectionDrawable(int winddirection)
	{
		int returnvalue = afr.iterson.R.drawable.winddir_se;

		if (winddirection > 338 && winddirection < 361)
		{
			returnvalue = afr.iterson.R.drawable.winddir_n;
		}

		else if (winddirection > 0 && winddirection < 23)
		{
			returnvalue = afr.iterson.R.drawable.winddir_n;
		}

		else if (winddirection > 22 && winddirection < 68)
		{
			returnvalue = afr.iterson.R.drawable.winddir_ne;
		}

		else if (winddirection > 67 && winddirection < 113)
		{
			returnvalue = afr.iterson.R.drawable.winddir_e;
		}

		else if (winddirection > 112 && winddirection < 158)
		{
			returnvalue = afr.iterson.R.drawable.winddir_se;
		}

		else if (winddirection > 157 && winddirection < 203)
		{
			returnvalue = afr.iterson.R.drawable.winddir_s;
		}

		else if (winddirection > 202 && winddirection < 248)
		{
			returnvalue = afr.iterson.R.drawable.winddir_sw;
		}

		else if (winddirection > 247 && winddirection < 293)
		{
			returnvalue = afr.iterson.R.drawable.winddir_w;
		}

		else if (winddirection > 292 && winddirection < 339)
		{
			returnvalue = afr.iterson.R.drawable.winddir_nw;
		}

		return returnvalue;
	}

	/**
	 * Converting the icon string from the weather service to an identifier from
	 * the drawable folder.
	 */
	public static int getWeatherDrawableId(String identifier)
	{
		int returnvalue;
		Log.d(TAG, "Iconidentifier: " + identifier);
		switch (identifier)
		{
		case "01d":
			returnvalue = afr.iterson.R.drawable.bg_clear_sky_day;
			break;
		case "02d":
			returnvalue = afr.iterson.R.drawable.bg_few_clouds_day;
			break;
		case "03d":
			returnvalue = afr.iterson.R.drawable.bg_scattered_clouds_day;
			break;
		case "04d":
			returnvalue = afr.iterson.R.drawable.bg_only_clouds_day;
			break;
		case "09d":
			returnvalue = afr.iterson.R.drawable.bg_rain_day;
			break;
		case "10d":
			returnvalue = afr.iterson.R.drawable.bg_shower_rain_day;
			break;
		case "11d":
			returnvalue = afr.iterson.R.drawable.bg_thunderstorm_day;
			break;
		case "13d":
			returnvalue = afr.iterson.R.drawable.snow_day;
			break;
		case "50d":
			returnvalue = afr.iterson.R.drawable.bg_fog_day;
			break;

		case "01n":
			returnvalue = afr.iterson.R.drawable.bg_clear_sky_night;
			break;
		case "02n":
			returnvalue = afr.iterson.R.drawable.bg_few_clouds_night;
			break;
		case "03n":
			returnvalue = afr.iterson.R.drawable.bg_scattered_clouds_night;
			break;
		case "04n":
			returnvalue = afr.iterson.R.drawable.bg_only_clouds_night;
			break;
		case "09n":
			returnvalue = afr.iterson.R.drawable.bg_rain_night;
			break;
		case "10n":
			returnvalue = afr.iterson.R.drawable.bg_shower_rain_night;
			break;
		case "11n":
			returnvalue = afr.iterson.R.drawable.bg_thunderstorm_night;
			break;
		case "13n":
			returnvalue = afr.iterson.R.drawable.snow_night;
			break;
		case "50n":
			returnvalue = afr.iterson.R.drawable.bg_fog_day;
			break;
		default:
			returnvalue = afr.iterson.R.drawable.bg_fog_night;
		}
		return returnvalue;
	}

	/**
	 * The string for sunrise and sunset that return from geonames.org needs to
	 * be concatenated
	 */
	public static String convertStringToTime(String timestring)
	{
		String returnstring = "0";
		if (timestring != null && timestring.length() > 5)
		{
			returnstring = timestring.substring(timestring.length() - 5, timestring.length());
		}
		return returnstring;
	}

	/**
	 * The geonames api returns a local date but not in a parseable way, so it
	 * has to be modified a litte.
	 */
	public static String convertDate(String datestring)
	{
		if (datestring == null || datestring.length() < 5)
		{
			return "0";
		}
		StringBuilder sb = new StringBuilder(datestring);
		sb.delete(0, 5);
		sb.delete(sb.length()-5, sb.length());
		String month = digitToMonth(sb.substring(0, 2));
		sb.delete(0, 3);
		sb.insert(2, ',');
		return month + " " + sb.toString();
	}

	
	public static String digitToMonth(String digit)
	{
		String returnvalue = "0";
		switch (digit)
		{
		case "01":
			returnvalue = "Jan";
			break;
		case "02":
			returnvalue = "Feb";
			break;
		case "03":
			returnvalue = "Mar";
			break;
		case "04":
			returnvalue = "Apr";
			break;
		case "05":
			returnvalue = "May";
			break;
		case "06":
			returnvalue = "Jun";
			break;
		case "07":
			returnvalue = "Jul";
			break;
		case "08":
			returnvalue = "Aug";
			break;
		case "09":
			returnvalue = "Sep";
			break;
		case "10":
			returnvalue = "Oct";
			break;
		case "11":
			returnvalue = "Nov";
			break;
		case "12":
			returnvalue = "Dec";
			break;
		}

		return returnvalue;
	}

	
}
