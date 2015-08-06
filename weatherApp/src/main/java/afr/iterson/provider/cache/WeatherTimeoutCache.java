package afr.iterson.provider.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import afr.iterson.operations.City;
import afr.iterson.provider.WeatherContract;
import afr.iterson.provider.WeatherContract.WeatherConditionEntry;
import afr.iterson.provider.WeatherContract.WeatherDataEntry;
import afr.iterson.retrofitWeather.WeatherData;
import afr.iterson.retrofitWeather.WeatherData.Coord;
import afr.iterson.retrofitWeather.WeatherData.Main;
import afr.iterson.retrofitWeather.WeatherData.Sys;
import afr.iterson.retrofitWeather.WeatherData.Weather;
import afr.iterson.retrofitWeather.WeatherData.Wind;
import android.app.AlarmManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

/**
 * Timeout cache that uses a content provider to store data and the Alarm
 * manager and a broadcast receiver to remove expired cache entries
 */
public class WeatherTimeoutCache implements TimeoutCache<String, WeatherData>
{
	/**
	 * Default cache timeout in to 25 seconds (in nanoseconds).
	 */
	private static final long DEFAULT_TIMEOUT = Long.valueOf(5000000000L);

	/**
	 * Cache is to be cleaned up at regular intervals to remove expired
	 * WeatherData.
	 */
	public static final long CLEANUP_SCHEDULER_TIME_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

	/**
	 * AlarmManager provides access to the system alarm services. Used to
	 * schedule Cache cleanup at regular intervals to remove expired Weather
	 * Data.
	 */
	private static AlarmManager mAlarmManager;

	/**
	 * Defines the selection clause query for the weather data of a given
	 * location.
	 */
	private static final String LOCATION_SELECTION_CLAUSE = WeatherDataEntry.COLUMN_NAME + " = ?";

	/**
	 * Defines the selection clause query for the weather data of a given
	 * location.
	 */

	
	private static final String WEATHER_DATA_CITYID_SELECTION_CLAUSE= WeatherDataEntry.COLUMN_CITYID + " = ? ";
	
	/**
	 * Defines the selection clause used to query for weather data that has
	 * expired
	 */
	private static final String EXPIRATION_SELECTION = WeatherDataEntry.COLUMN_EXPIRATION_TIME + " <= ?";

	/**
	 * Defines the selection clause used to query for weather data that has a
	 * specific id
	 */
	private static final String WEATHER_DATA_ID_SELECTION = WeatherDataEntry._ID + " = ?";

	
	private static final String WEATHER_DATA_CITYID_SELECTION = WeatherDataEntry.COLUMN_CITYID + " > ?";

	
	

	/**
	 * LogCat tag.
	 */
	private static final String TAG = WeatherTimeoutCache.class.getCanonicalName();

	/**
	 * The timeout for an instance of this class in seconds.
	 */
	private long mDefaultTimeout;

	/**
	 * Context used to access the contentResolver
	 */
	private Context mContext;

	/**
	 * Constructor that sets the default timeout for the cache (in seconds)
	 */
	public WeatherTimeoutCache(Context context)
	{
		// Set the timeout in nanoseconds.
		mDefaultTimeout = DEFAULT_TIMEOUT;

		// Get the AlarmManager system service.
		mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		// Store the context.
		mContext = context;

		// If Cache Cleanup is not scheduled, then schedule it.
		scheduleCacheUpdater(context);
	}

	/**
	 * Helper method that creates a content values object that can be inserted
	 * into the db's WeatherDataEntry table from a given WeatherData object.
	 */
	private ContentValues createWeatherDataContentValues(WeatherData wd, long timeout)
	{
		ContentValues val = new ContentValues();

		val.put(WeatherContract.WeatherDataEntry.COLUMN_NAME, wd.getName());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_DATE, wd.getDate());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_COD, wd.getCod());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_SUNRISE, wd.getSys().getSunrise());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_SUNSET, wd.getSys().getSunset());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_COUNTRY, wd.getSys().getCountry());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_TEMP, wd.getMain().getTemp());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_HUMIDITY, wd.getMain().getHumidity());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_PRESSURE, wd.getMain().getPressure());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_SPEED, wd.getWind().getSpeed());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_DEG, wd.getWind().getDeg());

		val.put(WeatherContract.WeatherDataEntry.COLUMN_MAINICON, wd.getWeathers().get(0).getIcon());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_ICON1, wd.getIcons()[0]);
		val.put(WeatherContract.WeatherDataEntry.COLUMN_ICON2, wd.getIcons()[1]);
		val.put(WeatherContract.WeatherDataEntry.COLUMN_ICON3, wd.getIcons()[2]);
		val.put(WeatherContract.WeatherDataEntry.COLUMN_ICON4, wd.getIcons()[3]);
		val.put(WeatherContract.WeatherDataEntry.COLUMN_ICON5, wd.getIcons()[4]);
		val.put(WeatherContract.WeatherDataEntry.COLUMN_DATESTRING, wd.getDateString());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_SUNRISESTRING, wd.getSunriseString());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_SUNSETSTRING, wd.getSunsetString());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_GMTOFFSET, wd.getGmtOffset());
		
		val.put(WeatherContract.WeatherDataEntry.COLUMN_CITYID, wd.getmId());
		
		val.put(WeatherContract.WeatherDataEntry.COLUMN_LONGITUDE, wd.getCoord().getLon());
		val.put(WeatherContract.WeatherDataEntry.COLUMN_LATTITUDE, wd.getCoord().getLat());

		val.put(WeatherContract.WeatherDataEntry.COLUMN_EXPIRATION_TIME, System.nanoTime() + timeout);
		return val;
	}

	
	/**
	 * Helper method that places a weather data object into the db
	 */
	private void putImpl(String key, WeatherData wd, long timeout)
	{
		// Enter the main WeatherData. The result Uri can be used to
		// determine the row that it was placed in.
		final Uri uri = mContext.getContentResolver().insert(WeatherContract.WeatherDataEntry.WEATHER_DATA_CONTENT_URI,
				createWeatherDataContentValues(wd, timeout));

	}

	
	public void updateImpl(WeatherData wd, long timeout, long id)
	{
		
		final int updatedrow = mContext.getContentResolver().update(WeatherContract.WeatherDataEntry.WEATHER_DATA_CONTENT_URI,
				createWeatherDataContentValues(wd, timeout), 
				WEATHER_DATA_ID_SELECTION, 
				new String[]{String.valueOf(id)});
		Log.d(TAG, "updated " + updatedrow + " row: "  );
	}
	
	
	public void updateRow(WeatherData wd, long id)
	{
		
		final int updatedrow = mContext.getContentResolver().update(WeatherContract.WeatherDataEntry.WEATHER_DATA_CONTENT_URI,
				createWeatherDataContentValues(wd, getmDefaultTimeout()), 
				WEATHER_DATA_CITYID_SELECTION_CLAUSE, 
				new String[]{String.valueOf(id)});
		Log.d(TAG, "updated row: " + updatedrow);
		
	}
	
	
	/**
	 * Attempts to retrieve the given key's corresponding WeatherData object. If
	 * the key doesn't exist or has timed out, null is returned.
	 */
	@Override
	public WeatherData get(final String location)
	{
		// Attempt to retrieve the location's data from the content
		// provider.
		try (Cursor wdCursor = mContext.getContentResolver().query(WeatherContract.ACCESS_ALL_DATA_FOR_LOCATION_URI,
				null, LOCATION_SELECTION_CLAUSE, new String[] { location }, null))
		{
			// Check that the cursor isn't null and contains an item.
			if (wdCursor != null && wdCursor.moveToFirst())
			{
				Log.v(TAG, "Cursor not null and has first item");

				// If the cursor has a Weather Data object
				// corresponding to the location, check to see if it
				// has timed out. If it has, delete it, else return
				// the data.
				if (wdCursor.getLong(wdCursor.getColumnIndex(WeatherContract.WeatherDataEntry.COLUMN_EXPIRATION_TIME)) < System
						.nanoTime())
				{

					// Delete the stale data from the db in a new thread.
					new Thread(new Runnable()
					{
						public void run()
						{
							remove(location);
						}
					}).start();

					return null;
				} else
				{
					WeatherData newData = getWeatherData(wdCursor);
					return newData;
				}

			} else
				// Query was empty or returned null.
				return null;
		}
	}

	public Cursor getCursorWithCityIds()
	{
		Cursor cityids = mContext.getContentResolver().query(
				WeatherContract.WeatherDataEntry.WEATHER_DATA_CONTENT_URI, 
				new String[] {WeatherContract.WeatherDataEntry.COLUMN_CITYID }, 
				null, 
				null, 
				null);
		Log.d(TAG,"Returned rows:"+String.valueOf(cityids.getCount()));
		return cityids;

	}
	
	
	public Cursor getCursorForAdapter()
	{
		Cursor adapterData = mContext.getContentResolver().query(
				WeatherContract.WeatherDataEntry.WEATHER_DATA_CONTENT_URI, 
				new String[] {WeatherContract.WeatherDataEntry.COLUMN_NAME,WeatherContract.WeatherDataEntry.COLUMN_COUNTRY,WeatherContract.WeatherDataEntry.COLUMN_CITYID }, 
				WeatherContract.WeatherDataEntry._ID + " >? ", 
				new String[] {String.valueOf(0)}, 
				null);
		return adapterData;
	}
	
	
	public ArrayList<City> getCityList(Cursor data)
	{
		Log.d(TAG, " getCityList...");
		ArrayList<City> cityList = new ArrayList<City>();
		while(data.moveToNext())
		{
			cityList.add(new City(data.getString(data.getColumnIndex(WeatherContract.WeatherDataEntry.COLUMN_NAME)),
					data.getString(data.getColumnIndex(WeatherContract.WeatherDataEntry.COLUMN_COUNTRY)),
					data.getLong(data.getColumnIndex(WeatherContract.WeatherDataEntry.COLUMN_CITYID))
					));
		}
		if(data == null || !data.moveToFirst())
		{
			Log.d(TAG, " no data in cursor...");
			return cityList;
		}
		Log.d(TAG, "cities in list: " + cityList.toString());
		data.close();
		return cityList;
	}
	
	
	public ArrayList<WeatherData> getRowsForCityIds(String[] cityids)
	{
		Cursor data = null;
		ArrayList<WeatherData> list = null;
		try
		{
			data = mContext.getContentResolver().query(
					WeatherContract.ACCESS_ALL_DATA_FOR_CITYIDS, 
					null, 
					null, 
					cityids, 
					null);
			list = getWeatherDataList(data);
		}
		catch(Exception e)
		{
			
		}
	if(data != null)
		data.close();	
	return list;	
	}
	
	
	/**
	 * Constructor using a Cursor returned by the WeatherProvider. This Cursor
	 * must contain all the data for the object - i.e., it must contain a row
	 * for each Weather object corresponding to the Weather object.
	 */
	private WeatherData getWeatherData(Cursor data)
	{
		if (data == null || !data.moveToFirst())
			return null;
		else
		{
			// Obtain data from the first row. Once Weather is used,
			// loop through the cursor to get all the Weather Data.
			final String name = data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_NAME));
			final long date = data.getLong(data.getColumnIndex(WeatherDataEntry.COLUMN_DATE));
			final long cod = data.getLong(data.getColumnIndex(WeatherDataEntry.COLUMN_COD));
			final Sys sys = new Sys(data.getLong(data.getColumnIndex(WeatherDataEntry.COLUMN_SUNRISE)),
					data.getLong(data.getColumnIndex(WeatherDataEntry.COLUMN_SUNSET)), data.getString(data
							.getColumnIndex(WeatherDataEntry.COLUMN_COUNTRY)));
			final Main main = new Main(data.getDouble(data.getColumnIndex(WeatherDataEntry.COLUMN_TEMP)),
					data.getLong(data.getColumnIndex(WeatherDataEntry.COLUMN_HUMIDITY)), data.getDouble(data
							.getColumnIndex(WeatherDataEntry.COLUMN_PRESSURE)));
			final Wind wind = new Wind(data.getDouble(data.getColumnIndex(WeatherDataEntry.COLUMN_SPEED)),
					data.getDouble(data.getColumnIndex(WeatherDataEntry.COLUMN_DEG)));
			
			final Coord coords = new Coord(	data.getDouble(data.getColumnIndex(WeatherDataEntry.COLUMN_LONGITUDE)),
											data.getDouble(data.getColumnIndex(WeatherDataEntry.COLUMN_LATTITUDE))
											);
			final String[]icons = {	data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_ICON1)),
									data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_ICON2)),
									data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_ICON3)),
									data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_ICON4)),
									data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_ICON5))
									};
			
			final String mainicon = 	data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_MAINICON));
			final String sunrisestring= data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_SUNRISESTRING));
			final String sunsetstring = data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_SUNSETSTRING));
			final String datestring = 	data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_DATESTRING));
			final long cityid = 		data.getLong(data.getColumnIndex(WeatherDataEntry.COLUMN_CITYID));
			List<Weather> list = new ArrayList<Weather>();
			list.add(new Weather(
					cityid,
					"",
					"",
					mainicon
					));
			
			WeatherData returnvalue = new WeatherData(
					name,
					cityid,
					date,
					cod,
					sys,
					main,
					wind,
					coords,
					list
					);
			returnvalue.setIcons(icons);
			returnvalue.setSunriseString(sunrisestring);
			returnvalue.setSunsetString(sunsetstring);
			returnvalue.setDateString(datestring);
			returnvalue.setMainicon(mainicon);
			return returnvalue;
		}
	}

	private ArrayList<WeatherData> getWeatherDataList(Cursor data)
	{
		ArrayList<WeatherData> weatherData = new ArrayList<WeatherData>();
		
		if (data == null || !data.moveToFirst())
			return null;
		else
		{
			// Obtain data from the all the provided rows. 
			// loop through the cursor to get all the Weather Data.
			do
			{	
			final String name = data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_NAME));
			final long date = data.getLong(data.getColumnIndex(WeatherDataEntry.COLUMN_DATE));
			final long cod = data.getLong(data.getColumnIndex(WeatherDataEntry.COLUMN_COD));
			final Sys sys = new Sys(data.getLong(data.getColumnIndex(WeatherDataEntry.COLUMN_SUNRISE)),
					data.getLong(data.getColumnIndex(WeatherDataEntry.COLUMN_SUNSET)), data.getString(data
							.getColumnIndex(WeatherDataEntry.COLUMN_COUNTRY)));
			final Main main = new Main(data.getDouble(data.getColumnIndex(WeatherDataEntry.COLUMN_TEMP)),
					data.getLong(data.getColumnIndex(WeatherDataEntry.COLUMN_HUMIDITY)), data.getDouble(data
							.getColumnIndex(WeatherDataEntry.COLUMN_PRESSURE)));
			final Wind wind = new Wind(data.getDouble(data.getColumnIndex(WeatherDataEntry.COLUMN_SPEED)),
					data.getDouble(data.getColumnIndex(WeatherDataEntry.COLUMN_DEG)));
			
			final Coord coords = new Coord(	data.getDouble(data.getColumnIndex(WeatherDataEntry.COLUMN_LONGITUDE)),
											data.getDouble(data.getColumnIndex(WeatherDataEntry.COLUMN_LATTITUDE))
											);
			final String[]icons = {	data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_ICON1)),
									data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_ICON2)),
									data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_ICON3)),
									data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_ICON4)),
									data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_ICON5))
									};
			
			final String mainicon = 	data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_MAINICON));
			final String sunrisestring= data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_SUNRISESTRING));
			final String sunsetstring = data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_SUNSETSTRING));
			final String datestring = 	data.getString(data.getColumnIndex(WeatherDataEntry.COLUMN_DATESTRING));
			final double gmtOffset = data.getDouble(data.getColumnIndex(WeatherDataEntry.COLUMN_GMTOFFSET));
			final long cityid = 		data.getLong(data.getColumnIndex(WeatherDataEntry.COLUMN_CITYID));
			
			
			WeatherData returnvalue = new WeatherData(
					name,
					cityid,
					date,
					cod,
					sys,
					main,
					wind,
					coords,
					null
					);
			returnvalue.setIcons(icons);
			returnvalue.setSunriseString(sunrisestring);
			returnvalue.setSunsetString(sunsetstring);
			returnvalue.setDateString(datestring);
			returnvalue.setGmtOffset(gmtOffset);
			returnvalue.setMainicon(mainicon);
			weatherData.add(returnvalue);
			} while (data.moveToNext());
			data.close();
			return weatherData;
		}
	}

	
	
	
	
	/**
	 * Place the WeatherData object into the cache. It assumes that a get()
	 * method has already attempted to find this object's location in the cache,
	 * and returned null.
	 */
	@Override
	public void put(String key, WeatherData obj)
	{
		putImpl(key, obj, mDefaultTimeout);
	}

	/**
	 * Places the WeatherData object into the cache with a user specified
	 * timeout.
	 */
	@Override
	public void put(String key, WeatherData obj, int timeout)
	{
		putImpl(key, obj, timeout * 1000 * 1000 * 1000);
	}

	@Override
	public void remove(String key)
	{
		// Delete the WeatherDataEntries.
		mContext.getContentResolver().delete(WeatherContract.WeatherDataEntry.WEATHER_DATA_CONTENT_URI,
				LOCATION_SELECTION_CLAUSE, new String[] { key });
	}

	
	public int deleteCities(ArrayList<Long> cities)
	{
		int number =0;
		for(Long l : cities)
		{
			number += mContext.getContentResolver().delete(
					WeatherContract.WeatherDataEntry.WEATHER_DATA_CONTENT_URI, 
					WEATHER_DATA_CITYID_SELECTION_CLAUSE, 
					new String[]{String.valueOf(l)});
		}
		return number;
	}
	
	
	/**
	 * Return the current number of WeatherData objects in Database.
	 * 
	 * @return size
	 */
	@Override
	public int size()
	{
		// Query the db for all rows of the Weather Data table.
		Cursor cursor = mContext.getContentResolver().query(WeatherContract.WeatherDataEntry.WEATHER_DATA_CONTENT_URI,
				null, null, null, null);

		// Return the number of rows in the table, which is equivlent to the
		// number of objects
		int size = cursor.getCount();
		cursor.close();
		return size;
	}

	/**
	 * Helper method that uses AlarmManager to schedule Cache Cleanup at regular
	 * intervals.
	 * 
	 * @param context
	 */
	private void scheduleCacheUpdater(Context context)
	{
		Log.d(TAG, "Updater");
		// Only schedule the Alarm if it's not already scheduled.
		if (!isAlarmActive(context))
		{
			Log.d(TAG, "Updater : alarm is set...");

			// Schedule an alarm after a certain timeout to start a
			// service to delete expired data from Database.
			mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()

			+ DEFAULT_TIMEOUT/1000000,
					CLEANUP_SCHEDULER_TIME_INTERVAL, UpdateReceiver.makeReceiverPendingIntent(context));
		}
	}

	/**
	 * Helper method to check whether the Alarm is already active or not.
	 * 
	 * @param context
	 * @return boolean, whether the Alarm is already active or not
	 */
	private boolean isAlarmActive(Context context)
	{
		// Check whether the Pending Intent already exists or not.
		return UpdateReceiver.makeCheckAlarmPendingIntent(context) != null;
	}

	/**
	 * Update the expired WeatherData from Database.
	 */
	public void updateWeatherData()
	{
		// First query the db to find all expired Weather Data objects' ids
		try (Cursor expiredData = mContext.getContentResolver().query(WeatherDataEntry.WEATHER_DATA_CONTENT_URI,
				new String[] { WeatherDataEntry._ID }, EXPIRATION_SELECTION,
				new String[] { String.valueOf(System.nanoTime()) }, null))
		{

			// Use the expired data id's to delete the correct entries from
			// both tables
			if (expiredData != null && expiredData.moveToFirst())
			{
				do
				{
					String updatedId = expiredData.getString(expiredData.getColumnIndex(WeatherDataEntry._ID));

					mContext.getContentResolver().delete(WeatherDataEntry.WEATHER_DATA_CONTENT_URI,
							WEATHER_DATA_ID_SELECTION, new String[] { updatedId });

				} while (expiredData.moveToNext());
			}
		}
	}
	
	public void deleteWeatherData(long cityid)
	{
		// First query the db to find all expired Weather Data objects' ids
		try (Cursor deletedData = mContext.getContentResolver().query(WeatherDataEntry.WEATHER_DATA_CONTENT_URI,
				new String[] { WeatherDataEntry.COLUMN_CITYID }, WEATHER_DATA_CITYID_SELECTION_CLAUSE,
				new String[] { String.valueOf((int) cityid) }, null))
		{

			// Use the expired data id's to delete the correct entries from
			// both tables
			if (deletedData != null && deletedData.moveToFirst())
			{
				do
				{
					String deleteId = deletedData.getString(deletedData.getColumnIndex(WeatherDataEntry._ID));

					mContext.getContentResolver().delete(WeatherDataEntry.WEATHER_DATA_CONTENT_URI,
							WEATHER_DATA_ID_SELECTION, new String[] { deleteId });

				} while (deletedData.moveToNext());
			}
		}
	}

	public long getmDefaultTimeout()
	{
		return mDefaultTimeout;
	}

	
	
	
	
}
