package afr.iterson.provider.cache;

import retrofit.RestAdapter;
import afr.iterson.operations.WeatherOps;
import afr.iterson.provider.WeatherContract.WeatherDataEntry;
import afr.iterson.provider.WeatherProvider;
import afr.iterson.retrofitWeather.GeoNamesServiceProxy;
import afr.iterson.retrofitWeather.TimeModifier;
import afr.iterson.retrofitWeather.WeatherData;
import afr.iterson.retrofitWeather.WeatherForecast;
import afr.iterson.retrofitWeather.WeatherWebServiceProxy;
import afr.iterson.service.WeatherService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Broadcast receiver that will remove all the expired Weather data at regular
 * intervals.
 */
public class UpdateReceiver extends BroadcastReceiver
{
	/**
	 * Private request code for sender that is passed in an Intent.
	 */
	public static final int REQUEST_CODE = 102;

	private static String TAG = UpdateReceiver.class.getSimpleName();

	public static final String REQUEST_SERVICE = "request_service";
	
	private static final String ACTION_RECEIVER = "afr.iterson.provider.cache.UpdateReceiver";
	
	public Context mContext;
	
	WeatherTimeoutCache mCache;

	/**
	 * Factory method to make a Pending Intent that is used by AlarmManager to
	 * schedule Cache cleanup at regular intervals.
	 * 
	 * @param context
	 * @return
	 */

	public static PendingIntent makeReceiverPendingIntent(Context context)
	{
		// Use the Pending Intent that will send Intent to Receiver to
		// delete expired data from Database.
		return PendingIntent.getBroadcast(context, REQUEST_CODE, 
				new Intent(ACTION_RECEIVER), 
				PendingIntent.FLAG_CANCEL_CURRENT);
	}

	
	/**
	 * Factory method to make a Pending Intent that is used to check whether the
	 * Alarm is already active or not.
	 * 
	 * @param context
	 * @return
	 */
	
	public static PendingIntent makeCheckAlarmPendingIntent(Context context)
	{
		// Check whether the Pending Intent already exists or not.
		return PendingIntent.getBroadcast(context, 
				REQUEST_CODE, 
				new Intent(ACTION_RECEIVER), 
				PendingIntent.FLAG_NO_CREATE);
	}
	
	

	public static Intent makeIntent(Context context)
	{
		return new Intent(WeatherService.ACTION_WEATHER_SERVICE_RESPONSE2);
	}
	
	
	/**
	 * Hook method that is called when the BroadcastReceiver is receiving an
	 * Intent broadcast.
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//Delegate the updating to an intent service that updates in the background;
		//A broadcast is sent back to the activity to update the Views.
		Log.i(TAG, " broadcast received");
		context.startService(WeatherService.makeIntent(context, REQUEST_SERVICE));
		
	}

}
