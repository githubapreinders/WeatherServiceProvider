package afr.iterson.activities;

import java.util.ArrayList;

import afr.iterson.R;
import afr.iterson.activities.WeatherFragment.UpdaterCache;
import afr.iterson.operations.Display2Ops;
import afr.iterson.retrofitWeather.WeatherData;
import afr.iterson.utils.GenericFragmentActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class Display2Activity extends GenericFragmentActivity<Display2Ops.View, Display2Ops> implements
		Display2Ops.View, UpdaterCache
{
	public static final String TAG = Display2Activity.class.getSimpleName();

	public static final String CITIES = "cities";

	public WeatherFragment fragment1;

	public static final String FRAGMENT1_TAG = "fragment1_tag";

	public WeatherFragment fragment2;

	public static final String FRAGMENT2_TAG = "fragment2_tag";

	public static Intent makeIntent(Context context, long[] cityids)
	{
		return new Intent(context, Display2Activity.class).putExtra(CITIES, cityids);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState, Display2Ops.class, this);
		setContentView(R.layout.two_cities);
		Log.d(TAG, "In new activity");
		FragmentManager manager = getSupportFragmentManager();
		fragment1 = (WeatherFragment) manager.findFragmentByTag(FRAGMENT1_TAG);
		fragment2 = (WeatherFragment) manager.findFragmentByTag(FRAGMENT2_TAG);
		if (fragment1 == null || fragment2 == null)
		{
			Log.d(TAG, "Creating new fragments");
			getOps().createFragments(getIntent().getLongArrayExtra(CITIES));
		}
	}

	@Override
	public void makeFragments(ArrayList<WeatherData> mCurrentWeatherData)
	{
		Log.d(TAG, "displayresults");
		if (mCurrentWeatherData.size() == 2)
		{
			fragment1 = WeatherFragment.newInstance(mCurrentWeatherData.get(0));
			getSupportFragmentManager().beginTransaction().add(R.id.fragment_container1, fragment1, FRAGMENT1_TAG)
					.commit();
			fragment2 = WeatherFragment.newInstance(mCurrentWeatherData.get(1));
			getSupportFragmentManager().beginTransaction().add(R.id.fragment_container2, fragment2, FRAGMENT2_TAG)
					.commit();
		}
	}

	
	/**
	 * Called from a weatherfragment to get the cache instance; the fragment then can update itself with the given values.
	 * The weatherfragment maintains a broadcastreceiver that triggers when the WeatherService is finished. This will happen
	 * every 15 minutes.
	 */
	@Override
	public WeatherData getFromCache(long cityid)
	{
		return getOps().getFromCache(cityid);
	}

}
