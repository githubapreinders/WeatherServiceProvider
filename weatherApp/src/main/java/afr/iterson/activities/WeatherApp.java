package afr.iterson.activities;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class WeatherApp extends Application
{
	public static final String PREFERENCES = "afr.iterson.preferences";
	public static final String KEY_METRIC_SYSTEM = "key_metric_system";
	private boolean metric;

	SharedPreferences prefs;

	@Override
	public void onCreate()
	{
		super.onCreate();
		prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		metric = prefs.getBoolean(KEY_METRIC_SYSTEM, true);
	}

	
	public boolean isMetric()
	{
		return metric;
	}

	//Setting the SharedPreference after a click on the temperature field, changes to Fahrenheit(metric = false) or Celsius(metric = true).
	public void setMetric(boolean metric)
	{
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(KEY_METRIC_SYSTEM, metric);
		editor.commit();
		this.metric = metric;
	}

	
	
	
	
	
}
