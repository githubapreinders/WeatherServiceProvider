package afr.iterson.activities;

import java.util.ArrayList;

import afr.iterson.R;
import afr.iterson.activities.WeatherFragment.UpdaterCache;
import afr.iterson.operations.DisplayPagerOps;
import afr.iterson.operations.MyPagerAdapter;
import afr.iterson.retrofitWeather.WeatherData;
import afr.iterson.utils.GenericFragmentActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class DisplayPagerActivity extends GenericFragmentActivity<DisplayPagerOps.View, DisplayPagerOps> implements
		DisplayPagerOps.View, UpdaterCache
{
	public static final String TAG = DisplayPagerActivity.class.getSimpleName();

	public static final String CITIES = "cities";

	public WeatherFragment fragment1;

	public static final String FRAGMENT1_TAG = "fragment1_tag";

	public WeatherFragment fragment2;

	public static final String FRAGMENT2_TAG = "fragment2_tag";
	
	private ViewPager mPager;
	
	private MyPagerAdapter mAdapter;
	
	PagerFragment mPagerFragment;
	
	private ArrayList<WeatherFragment> fragments;
	

	public static Intent makeIntent(Context context, long[] cityids)
	{
		return new Intent(context, DisplayPagerActivity.class).putExtra(CITIES, cityids);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "In new activity");
		super.onCreate(savedInstanceState, DisplayPagerOps.class, this);
		setContentView(R.layout.fragment_pager);
		fragments = new ArrayList<WeatherFragment>();
		mAdapter = new MyPagerAdapter(getSupportFragmentManager(), fragments);
		mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		getOps().createFragments(getIntent().getLongArrayExtra(CITIES));
	}

	@Override
	public void makeFragments(ArrayList<WeatherData> mCurrentWeatherData)
	{
		Log.d(TAG, "Adding to the pageradapter");
		
		for(WeatherData w : mCurrentWeatherData)
		{
			WeatherFragment f = WeatherFragment.newInstance(w);
			fragments.add(f);
			mAdapter.notifyDataSetChanged();
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
