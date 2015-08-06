package afr.iterson.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import afr.iterson.R;
import afr.iterson.operations.MyPagerAdapter;
import afr.iterson.retrofitWeather.WeatherData;
import afr.iterson.utils.DepthPageTransformer;
import afr.iterson.utils.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PagerFragment extends android.support.v4.app.Fragment
{

	public static final String TAG = PagerFragment.class.getSimpleName();

	ViewPager pager;
	
	MyPagerAdapter mPagerAdapter;
	
	private WeakReference<DisplayPagerActivity> mActivity;
	
	private ArrayList<WeatherFragment> adapterlist;
	
	public interface Datatransfer
	{
		public ArrayList<WeatherFragment> getAdapterList();
		public  MyPagerAdapter getAdapter();
	}
	public Datatransfer datatransfer;

	public View mainview;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		adapterlist = datatransfer.getAdapterList();
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		mainview = inflater.inflate(R.layout.fragment_pager, container, false);
		pager = (ViewPager) mainview.findViewById(R.id.pager);
		mPagerAdapter = datatransfer.getAdapter();
		pager.setAdapter(mPagerAdapter);
		pager.setPageTransformer(true, new DepthPageTransformer());
		return mainview;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		Log.d(TAG, "PagerFragment on attach");
		datatransfer = (Datatransfer)activity;
		mActivity = new WeakReference<>((DisplayPagerActivity)activity);
	}

	public void displayResults(List<WeatherData> results, DisplayPagerActivity activity)
	{
		Log.d(TAG, " displayResults...");
		if (results == null || results.size() == 0)
		{
			return;
		}
		for (WeatherData weatherdata : results)
		{
			if (!(null == weatherdata.getmName()) && !(weatherdata.getmName().equals("0")))
			{
				String helper = weatherdata.getmName() + "#" + weatherdata.getSys().getCountry();
				int pos = fragmentExists(helper);
				Log.i(TAG, "Position in which pager is set: " + String.valueOf(pos));
				if (pos == -1)
				{
					WeatherFragment weatherfragment = WeatherFragment.newInstance(weatherdata);
					adapterlist.add(weatherfragment);
					mPagerAdapter.notifyDataSetChanged();
				} else
				{
					pager.setCurrentItem(pos);
				}
				for (int i = 0; i < adapterlist.size(); i++)
				{
					Log.d(TAG, adapterlist.get(i).getIdentifier());
				}
			} else
			{
			Utils.showToast(mActivity.get(), "Check Wifi or disable VPN...");
			}
		}
	}
	

	private int fragmentExists(String identifier)
	{
		int position = -1;
		for (WeatherFragment fragment : adapterlist)
		{
			if (fragment.getIdentifier().equals(identifier))
			{
				position = adapterlist.indexOf(fragment);
			}
		}
		Log.d(TAG, "checks if " + identifier + " fragment exists: " + position);
		return position;
	}
	
}
