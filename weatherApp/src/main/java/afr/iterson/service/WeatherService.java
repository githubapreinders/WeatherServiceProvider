package afr.iterson.service;

import java.net.ConnectException;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import afr.iterson.activities.WeatherFragment;
import afr.iterson.provider.WeatherContract;
import afr.iterson.provider.cache.UpdateReceiver;
import afr.iterson.provider.cache.WeatherTimeoutCache;
import afr.iterson.retrofitWeather.GeoNamesServiceProxy;
import afr.iterson.retrofitWeather.TimeModifier;
import afr.iterson.retrofitWeather.WeatherData;
import afr.iterson.retrofitWeather.WeatherForecast;
import afr.iterson.retrofitWeather.WeatherWebServiceProxy;
import afr.iterson.utils.DataMediator;
import afr.iterson.utils.Utils;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Intent Service that runs in background and uploads the Video with a given Id.
 * After the operation, it broadcasts the Intent to send the result of the
 * upload to the VideoListActivity.
 */
public class WeatherService extends IntentService
{

	private static final String TAG = WeatherService.class.getSimpleName();

	/**
	 * Custom Action that will be used to send Broadcast to the
	 * VideoListActivity.
	 */
	public static final String ACTION_WEATHER_SERVICE_RESPONSE = "afr.iterson.service.RESPONSE";
	
	public static final String ACTION_WEATHER_SERVICE_RESPONSE2 = "afr.iterson.service.RESPONSE2";


	public static final String REQUEST_STRING = "request_multiple_cities";

	/**
	 * It is used by Notification Manager to send Notifications.
	 */
	private static final int NOTIFICATION_ID = 1;

	private WeatherTimeoutCache mCache;

	/**
	 * Manages the Notification displayed in System UI.
	 */
	private NotificationManager mNotifyManager;

	/**
	 * Builder used to build the Notification.
	 */
	private Builder mBuilder;

	private WeatherWebServiceProxy mWeatherWebServiceProxy;

	private GeoNamesServiceProxy mGeoNamesServiceProxy;

	/**
	 * Constructor for UploadVideoService.
	 * 
	 * @param name
	 */
	public WeatherService(String name)
	{
		super("UploadVideoService");
	}

	/**
	 * Constructor for UploadVideoService.
	 * 
	 * @param name
	 */
	public WeatherService()
	{
		super("UploadVideoService");
	}

	/**
	 * Factory method that makes the explicit intent another Activity uses to
	 * call this Service.
	 * 
	 * @param context
	 * @param videoId
	 * @return
	 */
	public static Intent makeIntent(Context context, String requeststring)
	{
		return new Intent(context, WeatherService.class).putExtra(REQUEST_STRING, requeststring);
	}

	/**
	 * This method gets the cityids from the cache, queries the services
	 * and updates the cache. A broadcast is sent to schedule the next
	 * call to this service. Another broadcast is sent to listening fragments. 
	 * @param intent
	 */
	@Override
	protected void onHandleIntent(Intent intent)
	{

		Log.v(TAG, ": onHandleIntent");
		mWeatherWebServiceProxy = new RestAdapter.Builder().setEndpoint(Utils.sWeather_Service_URL_Retro).build()
				.create(WeatherWebServiceProxy.class);

		mGeoNamesServiceProxy = new RestAdapter.Builder().setEndpoint(Utils.Geo_Names_URL).build()
				.create(GeoNamesServiceProxy.class);
		mCache = new WeatherTimeoutCache(getApplicationContext());
		Cursor cursor = mCache.getCursorWithCityIds();
		WeatherData weatherData;
		if (cursor.moveToFirst())
		{
			do
			{
				try
				{
					long cityid = cursor.getLong(cursor.getColumnIndex(WeatherContract.WeatherDataEntry.COLUMN_CITYID));
					Log.v(TAG, ": getting values for " + cityid);
					weatherData = mWeatherWebServiceProxy.getWeatherDataById(cityid);
					Log.d(TAG, weatherData.getName());
					if (weatherData.getName() != null)
					{
						WeatherForecast forecast = mWeatherWebServiceProxy.getWeatherForecast(weatherData.getName(),
								Utils.DAYS_AHEAD);
						TimeModifier modifier = mGeoNamesServiceProxy.getModifiers(weatherData.getCoord().getLon(),
								weatherData.getCoord().getLat(), Utils.USERNAME);
						weatherData = DataMediator.modifyWeatherData(forecast, modifier, weatherData);

						Log.d(TAG, weatherData.getDateString());
						mCache.updateRow(weatherData, cityid);
					}
				} catch (Exception e)
				{
					//Schedule an alarm to try again after 1 minute
					e.printStackTrace();
					Log.e(TAG, "Error retrieving network data");
					PendingIntent intent3 = UpdateReceiver.makeReceiverPendingIntent(getApplicationContext());
					AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
					manager.set(AlarmManager.RTC, 1000*60, intent3);
				}
			} while (cursor.moveToNext());
		}
		cursor.close();

		sendBroadcasts();
		
		

	}

	/**
	 * Send the Broadcast to Activity that the Video Upload is completed.
	 */
	private void sendBroadcasts()
	{
		// Use a LocalBroadcastManager to restrict the scope of this
		// Intent to the VideoUploadClient application.
		Log.e(TAG, "Updating fragment from intent service...");
		LocalBroadcastManager.getInstance(this).sendBroadcast(
				new Intent(ACTION_WEATHER_SERVICE_RESPONSE).addCategory(Intent.CATEGORY_DEFAULT));
	    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(WeatherFragment.ACTION_UPDATE_FRAGMENT));
	}

}
